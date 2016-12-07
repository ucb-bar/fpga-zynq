
package zynq

import Chisel._
import config.{Parameters, Field}
import diplomacy._
import regmapper._
import junctions._
import junctions.NastiConstants._
import rocketchip._
import uncore.axi4._
import uncore.tilelink2._
import uncore.devices._
import coreplex._
import util.ConfigStringOutput
import testchipip._

import java.io.File

case object SerialFIFODepth extends Field[Int]
case object ResetCycles extends Field[Int]

class Top(implicit val p: Parameters) extends Module {
  val target = Module(LazyModule(new FPGAZynqTop).module)
  require(target.io.mem_axi4.head.size == 1)
  val toMem = (target.io.mem_axi4.head)(0)

  val io = new Bundle {
    val ps_axi_slave = new NastiIO()(AdapterParams(p)).flip
    val mem_axi = toMem.cloneType
  }

  val slave = Module(new ZynqAXISlave(1)(AdapterParams(p)))
  io.mem_axi <> toMem
  slave.io.nasti.head <> io.ps_axi_slave
  slave.io.serial <> target.io.serial
  target.reset := slave.io.sys_reset
}

class ZynqAXISlave(nPorts: Int)(implicit p: Parameters) extends Module {
  val io = new Bundle {
    val nasti = Vec(nPorts, new NastiIO()).flip
    val sys_reset = Bool(OUTPUT)
    val serial = new SerialIO(p(SerialInterfaceWidth)).flip
  }

  def routeSel(addr: UInt): UInt = {
    // 0x00 - 0x0F go to FIFO
    // 0x10 goes to Reset generator
    UIntToOH(addr(4))
  }

  val xbar = Module(new NastiCrossbar(nPorts, 2, routeSel _))
  val fifo = Module(new NastiFIFO)
  val resetter = Module(new ResetController)

  xbar.io.masters <> io.nasti
  fifo.io.nasti <> xbar.io.slaves(0)
  fifo.io.serial <> io.serial
  resetter.io.nasti <> xbar.io.slaves(1)
  io.sys_reset := resetter.io.sys_reset
}

class ResetController(implicit p: Parameters) extends NastiModule()(p) {
  val io = new Bundle {
    val nasti = new NastiIO().flip
    val sys_reset = Bool(OUTPUT)
  }

  val reg_reset = Reg(init = Bool(true))

  val readId = Reg(UInt(width = nastiXIdBits))

  val r_addr :: r_data :: Nil = Enum(Bits(), 2)
  val r_state = Reg(init = r_addr)

  io.nasti.ar.ready := r_state === r_addr
  io.nasti.r.valid := r_state === r_data
  io.nasti.r.bits := NastiReadDataChannel(
    id = readId,
    data = reg_reset)

  when (io.nasti.ar.fire()) {
    readId := io.nasti.ar.bits.id
    r_state := r_data
  }

  when (io.nasti.r.fire()) {
    r_state := r_addr
  }

  val writeId = Reg(UInt(width = nastiXIdBits))

  val w_addr :: w_data :: w_resp :: Nil = Enum(Bits(), 3)
  val w_state = Reg(init = w_addr)
  val timer = Reg(init = UInt(p(ResetCycles) - 1))

  // Make sure reset period lasts for a certain number of cycles
  when (timer =/= UInt(0)) { timer := timer - UInt(1) }

  when (io.nasti.aw.fire()) {
    writeId := io.nasti.aw.bits.id
    w_state := w_data
  }

  when (io.nasti.w.fire()) {
    timer := UInt(p(ResetCycles) - 1)
    reg_reset := io.nasti.w.bits.data(0)
    w_state := w_resp
  }

  when (io.nasti.b.fire()) {
    w_state := w_addr
  }

  io.nasti.aw.ready := w_state === w_addr
  io.nasti.w.ready := w_state === w_data
  io.nasti.b.valid := w_state === w_resp && timer === UInt(0)
  io.nasti.b.bits := NastiWriteResponseChannel(id = writeId)

  io.sys_reset := reg_reset
}

class NastiFIFO(implicit p: Parameters) extends NastiModule()(p) {
  val w = p(SerialInterfaceWidth)
  val depth = p(SerialFIFODepth)

  val io = new Bundle {
    val nasti = new NastiIO().flip
    val serial = new SerialIO(w).flip
  }

  require(nastiXDataBits == 32)
  require(nastiXDataBits == w)

  val outq = Module(new Queue(UInt(width = w), depth))
  val inq  = Module(new Queue(UInt(width = w), depth))
  val writing = Reg(init = Bool(false))
  val reading = Reg(init = Bool(false))
  val responding = Reg(init = Bool(false))
  val len = Reg(UInt(width = nastiXLenBits))
  val bid = Reg(UInt(width = nastiXIdBits))
  val rid = Reg(UInt(width = nastiXIdBits))

  io.serial.in <> inq.io.deq
  outq.io.enq <> io.serial.out

  val nRegisters = 3
  val addrLSB = log2Up(w / 8)
  val addrMSB = addrLSB + log2Up(nRegisters) - 1
  val araddr = io.nasti.ar.bits.addr(addrMSB, addrLSB)
  val awaddr = io.nasti.aw.bits.addr(addrMSB, addrLSB)
  val raddr = Reg(araddr)
  val waddr = Reg(awaddr)

  inq.io.enq.valid := io.nasti.w.valid && writing && (waddr === UInt(2))
  io.nasti.w.ready := (inq.io.enq.ready || waddr =/= UInt(2)) && writing
  inq.io.enq.bits  := io.nasti.w.bits.data

  /**
   * Address Map
   * 0x00 - out FIFO data
   * 0x04 - out FIFO data available (words)
   * 0x08 - in  FIFO data
   * 0x0C - in  FIFO space available (words)
   */
  io.nasti.r.valid := reading && (raddr =/= UInt(0) || outq.io.deq.valid)
  outq.io.deq.ready := reading && raddr === UInt(0) && io.nasti.r.ready
  io.nasti.r.bits := NastiReadDataChannel(
    id = rid,
    data = MuxLookup(raddr, UInt(0), Seq(
      UInt(0) -> outq.io.deq.bits,
      UInt(1) -> outq.io.count,
      UInt(3) -> (UInt(depth) - inq.io.count))),
    last = len === UInt(0))

  io.nasti.aw.ready := !writing && !responding
  io.nasti.ar.ready := !reading
  io.nasti.b.valid := responding
  io.nasti.b.bits := NastiWriteResponseChannel(
    id = bid,
    // writing to anything other that the in FIFO is an error
    resp = Mux(waddr === UInt(2), RESP_OKAY, RESP_SLVERR))

  when (io.nasti.aw.fire()) {
    writing := Bool(true)
    waddr := awaddr
    bid := io.nasti.aw.bits.id
  }
  when (io.nasti.w.fire() && io.nasti.w.bits.last) {
    writing := Bool(false)
    responding := Bool(true)
  }
  when (io.nasti.b.fire()) { responding := Bool(false) }
  when (io.nasti.ar.fire()) {
    len := io.nasti.ar.bits.len
    rid := io.nasti.ar.bits.id
    raddr := araddr
    reading := Bool(true)
  }
  when (io.nasti.r.fire()) {
    len := len - UInt(1)
    when (len === UInt(0)) { reading := Bool(false) }
  }

  def addressOK(chan: NastiAddressChannel): Bool =
    (chan.len === UInt(0) || chan.burst === BURST_FIXED) &&
    chan.size === UInt(log2Up(w/8)) &&
    chan.addr(log2Up(nastiWStrobeBits)-1, 0) === UInt(0)

  def dataOK(chan: NastiWriteDataChannel): Bool =
    chan.strb(w/8-1, 0).andR

  assert(!io.nasti.aw.valid || addressOK(io.nasti.aw.bits),
    s"NastiFIFO aw can only accept aligned fixed bursts of size $w")

  assert(!io.nasti.ar.valid || addressOK(io.nasti.ar.bits),
    s"NastiFIFO ar can only accept aligned fixed bursts of size $w")

  assert(!io.nasti.w.valid || dataOK(io.nasti.w.bits),
    s"NastiFIFO w cannot accept partial writes")
}

trait PeripheryBootROMWithHack {
  this: TopNetwork =>
  val coreplex: CoreplexRISCVPlatform

  private val bootrom_address = 0x1000
  private val bootrom_size = 0x1000

  // Currently RC emits a config string that uses a name for offchip memory
  // that does not comform to PK's expectation. So we prepend what we want.
  private lazy val hackedConfigString = {
    val sb = new StringBuilder
    sb append "ram {\n"
    sb append "  0 {\n"
    sb append "  addr 0x%x;\n".format(p(ExtMem).base)
    sb append "  size 0x%x;\n".format(p(ExtMem).size)
    sb append "  };\n"
    sb append "};\n"
    sb append coreplex.configString
    val configString = sb.toString

    println(s"\nBIANCOLIN'S HACK: Generated Configuration String\n${configString}")
    ConfigStringOutput.contents = Some(configString)
    configString
  }

  private lazy val bootrom_contents = GenerateBootROM(p, bootrom_address, hackedConfigString)
  val bootrom = LazyModule(new TLROM(bootrom_address, bootrom_size, bootrom_contents, true, peripheryBusConfig.beatBytes))
  bootrom.node := TLFragmenter(peripheryBusConfig.beatBytes, cacheBlockBytes)(peripheryBus.node)
}

trait PeripheryBootROMWithHackBundle {
  this: TopNetworkBundle {
    val outer: PeripheryBootROMWithHack
  } =>
}

trait PeripheryBootROMWithHackModule {
  this: TopNetworkModule {
    val outer: PeripheryBootROMWithHack
    val io: PeripheryBootROMWithHackBundle
  } =>
}

class FPGAZynqTop(implicit p: Parameters) extends BaseTop
    with PeripheryMasterAXI4Mem
    with PeripherySerial
    with PeripheryBootROMWithHack
    with PeripheryDTM
    with PeripheryCounter
    with HardwiredResetVector
    with RocketPlexMaster {
  override lazy val module = new FPGAZynqTopModule(this,
    () => new FPGAZynqTopBundle(this))
}

class FPGAZynqTopBundle[+L <: FPGAZynqTop](_outer: L) extends BaseTopBundle(_outer)
    with PeripheryMasterAXI4MemBundle
    with PeripherySerialBundle
    with PeripheryBootROMWithHackBundle
    with PeripheryDTMBundle
    with PeripheryCounterBundle
    with HardwiredResetVectorBundle
    with RocketPlexMasterBundle

class FPGAZynqTopModule[+L <: FPGAZynqTop, +B <: FPGAZynqTopBundle[L]](_outer: L, _io: () => B)
    extends BaseTopModule(_outer, _io)
    with PeripheryMasterAXI4MemModule
    with PeripherySerialModule
    with PeripheryBootROMWithHackModule
    with PeripheryDTMModule
    with PeripheryCounterModule
    with HardwiredResetVectorModule
    with RocketPlexMasterModule
