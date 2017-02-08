
package zynq

import chisel3._
import chisel3.util._
import diplomacy.{LazyModule, LazyModuleImp}
import junctions._
import junctions.NastiConstants._
import cde.{Parameters, Field}
import rocketchip._
import uncore.devices.{DebugBusIO}
import uncore.tilelink.ClientUncachedTileLinkIOCrossbar
import testchipip._
import coreplex.BaseCoreplexBundle

import java.io.File

case object SerialFIFODepth extends Field[Int]
case object ResetCycles extends Field[Int]

class Top(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val ps_axi_slave = Flipped(new NastiIO()(AdapterParams(p)))
    val mem_axi = new NastiIO
  })

  val target = LazyModule(new FPGAZynqTop(p)).module
  val slave = Module(new ZynqAXISlave(1)(AdapterParams(p)))

  require(target.io.mem_axi.size == 1)
  require(target.io.mem_ahb.isEmpty)
  require(target.io.mem_tl.isEmpty)
  require(target.io.mem_clk.isEmpty)
  require(target.io.mem_rst.isEmpty)

  io.mem_axi <> target.io.mem_axi.head

  slave.io.nasti.head <> io.ps_axi_slave
  slave.io.serial <> target.io.serial
  target.reset := slave.io.sys_reset
}

class ZynqAXISlave(nPorts: Int)(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val nasti = Flipped(Vec(nPorts, new NastiIO()))
    val sys_reset = Output(Bool())
    val serial = Flipped(new SerialIO(p(SerialInterfaceWidth)))
  })

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
  val io = IO(new Bundle {
    val nasti = Flipped(new NastiIO())
    val sys_reset = Output(Bool())
  })

  val reg_reset = Reg(init = true.B)

  val readId = Reg(UInt(nastiXIdBits.W))

  val r_addr :: r_data :: Nil = Enum(2)
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

  val writeId = Reg(UInt(nastiXIdBits.W))

  val w_addr :: w_data :: w_resp :: Nil = Enum(3)
  val w_state = Reg(init = w_addr)
  val timer = Reg(init = (p(ResetCycles) - 1).U)

  // Make sure reset period lasts for a certain number of cycles
  when (timer =/= 0.U) { timer := timer - 1.U }

  when (io.nasti.aw.fire()) {
    writeId := io.nasti.aw.bits.id
    w_state := w_data
  }

  when (io.nasti.w.fire()) {
    timer := (p(ResetCycles) - 1).U
    reg_reset := io.nasti.w.bits.data(0)
    w_state := w_resp
  }

  when (io.nasti.b.fire()) {
    w_state := w_addr
  }

  io.nasti.aw.ready := w_state === w_addr
  io.nasti.w.ready := w_state === w_data
  io.nasti.b.valid := w_state === w_resp && timer === 0.U
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

  val outq = Module(new Queue(UInt(w.W), depth))
  val inq  = Module(new Queue(UInt(w.W), depth))
  val writing = Reg(init = false.B)
  val reading = Reg(init = false.B)
  val responding = Reg(init = false.B)
  val len = Reg(UInt(nastiXLenBits.W))
  val bid = Reg(UInt(nastiXIdBits.W))
  val rid = Reg(UInt(nastiXIdBits.W))

  io.serial.in <> inq.io.deq
  outq.io.enq <> io.serial.out

  val nRegisters = 3
  val addrLSB = log2Up(w / 8)
  val addrMSB = addrLSB + log2Up(nRegisters) - 1
  val araddr = io.nasti.ar.bits.addr(addrMSB, addrLSB)
  val awaddr = io.nasti.aw.bits.addr(addrMSB, addrLSB)
  val raddr = Reg(araddr)
  val waddr = Reg(awaddr)

  inq.io.enq.valid := io.nasti.w.valid && writing && (waddr === 2.U)
  io.nasti.w.ready := (inq.io.enq.ready || waddr =/= 2.U) && writing
  inq.io.enq.bits  := io.nasti.w.bits.data

  /**
   * Address Map
   * 0x00 - out FIFO data
   * 0x04 - out FIFO data available (words)
   * 0x08 - in  FIFO data
   * 0x0C - in  FIFO space available (words)
   */
  io.nasti.r.valid := reading && (raddr =/= 0.U || outq.io.deq.valid)
  outq.io.deq.ready := reading && raddr === 0.U && io.nasti.r.ready
  io.nasti.r.bits := NastiReadDataChannel(
    id = rid,
    data = MuxLookup(raddr, 0.U, Seq(
      0.U -> outq.io.deq.bits,
      1.U -> outq.io.count,
      3.U -> (depth.U - inq.io.count))),
    last = len === 0.U)

  io.nasti.aw.ready := !writing && !responding
  io.nasti.ar.ready := !reading
  io.nasti.b.valid := responding
  io.nasti.b.bits := NastiWriteResponseChannel(
    id = bid,
    // writing to anything other that the in FIFO is an error
    resp = Mux(waddr === 2.U, RESP_OKAY, RESP_SLVERR))

  when (io.nasti.aw.fire()) {
    writing := true.B
    waddr := awaddr
    bid := io.nasti.aw.bits.id
  }
  when (io.nasti.w.fire() && io.nasti.w.bits.last) {
    writing := false.B
    responding := true.B
  }
  when (io.nasti.b.fire()) { responding := false.B }
  when (io.nasti.ar.fire()) {
    len := io.nasti.ar.bits.len
    rid := io.nasti.ar.bits.id
    raddr := araddr
    reading := true.B
  }
  when (io.nasti.r.fire()) {
    len := len - 1.U
    when (len === 0.U) { reading := false.B }
  }

  def addressOK(chan: NastiAddressChannel): Bool =
    (chan.len === 0.U || chan.burst === BURST_FIXED) &&
    chan.size === log2Up(w/8).U &&
    chan.addr(log2Up(nastiWStrobeBits)-1, 0) === 0.U

  def dataOK(chan: NastiWriteDataChannel): Bool =
    chan.strb(w/8-1, 0).andR

  assert(!io.nasti.aw.valid || addressOK(io.nasti.aw.bits),
    s"NastiFIFO aw can only accept aligned fixed bursts of size $w")

  assert(!io.nasti.ar.valid || addressOK(io.nasti.ar.bits),
    s"NastiFIFO ar can only accept aligned fixed bursts of size $w")

  assert(!io.nasti.w.valid || dataOK(io.nasti.w.bits),
    s"NastiFIFO w cannot accept partial writes")
}

class FPGAZynqTop(q: Parameters) extends BaseTop(q)
    with PeripheryBootROM with PeripheryCoreplexLocalInterrupter
    with PeripherySerial with PeripheryMasterMem {
  override lazy val module = Module(
    new FPGAZynqTopModule(p, this, new FPGAZynqTopBundle(p)))
}

class FPGAZynqTopBundle(p: Parameters) extends BaseTopBundle(p)
  with PeripheryBootROMBundle with PeripheryCoreplexLocalInterrupterBundle
  with PeripheryMasterMemBundle with PeripherySerialBundle

class FPGAZynqTopModule(p: Parameters, l: FPGAZynqTop, b: => FPGAZynqTopBundle)
  extends BaseTopModule(p, l, b)
  with PeripheryBootROMModule with PeripheryCoreplexLocalInterrupterModule
  with PeripheryMasterMemModule with PeripherySerialModule
  with HardwiredResetVector with DirectConnection with NoDebug
