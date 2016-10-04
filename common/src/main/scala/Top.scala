
package zynq

import Chisel._
import junctions._
import junctions.NastiConstants._
import cde.{Parameters, Config, CDEMatchError}
import rocketchip._
import uncore.devices.{DebugBusIO}
import uncore.tilelink2.{LazyModule, LazyModuleImp}
import coreplex.BaseCoreplexBundle

import java.io.File

class Top(implicit val p: Parameters) extends Module {
  val io = new Bundle {
    val ps_axi_slave = new NastiIO()(AdapterParams(p)).flip
    val mem_axi = new NastiIO
  }

  val target = LazyModule(new FPGAZynqTop(p)).module
  val fifo = Module(new NastiFIFO)

  require(target.io.mem_axi.size == 1)
  require(target.io.mem_ahb.isEmpty)
  require(target.io.mem_tl.isEmpty)
  require(target.io.mem_clk.isEmpty)
  require(target.io.mem_rst.isEmpty)

  io.mem_axi <> target.io.mem_axi.head
  fifo.io.nasti <> io.ps_axi_slave
  fifo.io.serial <> target.io.serial
}

class NastiFIFO(implicit p: Parameters) extends NastiModule()(p) {
  val w = p(SerialInterfaceWidth)
  val depth = p(SerialFIFODepth)

  val io = new Bundle {
    val nasti = new NastiIO().flip
    val serial = new SerialIO(w).flip
  }

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

  inq.io.enq.valid := io.nasti.w.valid && writing
  io.nasti.w.ready := inq.io.enq.ready && writing
  inq.io.enq.bits  := io.nasti.w.bits.data

  val nRegisters = 3
  val addrLSB = log2Up(w / 8)
  val addrMSB = addrLSB + log2Up(nRegisters) - 1
  val araddr = io.nasti.ar.bits.addr(addrMSB, addrLSB)
  val awaddr = io.nasti.aw.bits.addr(addrMSB, addrLSB)
  val raddr = Reg(araddr)

  io.nasti.r.valid := reading && (raddr =/= UInt(0) || outq.io.deq.valid)
  outq.io.deq.ready := reading && (raddr =/= UInt(0) || io.nasti.r.ready)
  io.nasti.r.bits := NastiReadDataChannel(
    id = rid,
    data = MuxLookup(raddr, UInt(0), Seq(
      UInt(0) -> outq.io.deq.bits,
      UInt(1) -> outq.io.count, // available output FIFO data
      UInt(2) -> (UInt(depth) - inq.io.count)))) // free input FIFO space

  io.nasti.aw.ready := !writing && !responding
  io.nasti.ar.ready := !reading
  io.nasti.b.valid := responding
  io.nasti.b.bits := NastiWriteResponseChannel(id = bid)

  when (io.nasti.aw.fire()) {
    writing := Bool(true)
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
  when (io.nasti.r.fire() && io.nasti.r.bits.last) { reading := Bool(false) }

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

trait NoDebug {
  val coreplexIO: BaseCoreplexBundle
  coreplexIO.debug.req.valid := Bool(false)
  coreplexIO.debug.resp.ready := Bool(false)
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
