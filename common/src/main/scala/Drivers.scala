package zynq

import chisel3._
import chisel3.util._
import junctions._
import config.Parameters
import _root_.util._
import testchipip._

class InFIFODriver(addr: BigInt)(implicit p: Parameters) extends NastiModule {
  val w = nastiXDataBits
  val io = IO(new Bundle {
    val axi = new NastiIO
    val in = Flipped(Decoupled(UInt(w.W)))
  })

  val s_idle :: s_waddr :: s_wdata :: s_wresp :: Nil = Enum(4)
  val state = RegInit(s_idle)
  val data = Reg(UInt(w.W))

  io.axi.ar.valid := false.B
  io.axi.r.ready := false.B
  io.axi.aw.valid := state === s_waddr
  io.axi.aw.bits := NastiWriteAddressChannel(
    id = 0.U,
    addr = addr.U,
    size = log2Ceil(w/8).U)
  io.axi.w.valid := state === s_wdata
  io.axi.w.bits := NastiWriteDataChannel(data = data)
  io.axi.b.ready := state === s_wresp
  io.in.ready := state === s_idle

  when (io.in.fire()) {
    data := io.in.bits
    state := s_waddr
  }

  when (io.axi.aw.fire()) {
    state := s_wdata
  }

  when (io.axi.w.fire()) {
    state := s_wresp
  }

  when (io.axi.b.fire()) {
    state := s_idle
  }
}

class OutFIFODriver(addr: BigInt)(implicit p: Parameters) extends NastiModule {
  val w = nastiXDataBits
  val io = IO(new Bundle {
    val axi = new NastiIO
    val out = Decoupled(UInt(w.W))
  })

  val started = RegNext(true.B, false.B)
  val busy = RegInit(false.B)

  io.axi.ar.valid := !busy && started
  io.axi.ar.bits := NastiReadAddressChannel(
    id = 0.U,
    addr = addr.U,
    size = log2Ceil(w/8).U)
  io.axi.r.ready := io.out.ready
  io.axi.aw.valid := false.B
  io.axi.w.valid := false.B
  io.axi.b.ready := false.B
  io.out.valid := io.axi.r.valid
  io.out.bits := io.axi.r.bits.data

  when (io.axi.ar.fire()) { busy := true.B }
  when (io.axi.r.fire()) { busy := false.B }
}

class SetRegisterDriver(addr: BigInt)(implicit p: Parameters) extends NastiModule {
  val io = IO(new Bundle {
    val axi = new NastiIO
    val value = Input(UInt(nastiXDataBits.W))
  })

  val (s_start :: s_write_addr :: s_write_data ::
       s_write_resp :: s_wait :: Nil) = Enum(5)
  val state = RegInit(s_start)
  val value = Reg(UInt(nastiXDataBits.W))

  when (state === s_start) { state := s_write_addr; value := io.value }
  when (io.axi.aw.fire()) { state := s_write_data }
  when (io.axi.w.fire()) { state := s_write_resp }
  when (io.axi.b.fire()) { state := s_wait }
  when (state === s_wait && io.value =/= value) { state := s_start }

  io.axi.aw.valid := state === s_write_addr
  io.axi.aw.bits := NastiWriteAddressChannel(
    id = 0.U,
    addr = addr.U,
    size = 2.U)

  io.axi.w.valid := state === s_write_data
  io.axi.w.bits := NastiWriteDataChannel(data = value)

  io.axi.b.ready := (state === s_write_resp)
  io.axi.ar.valid := false.B
  io.axi.r.ready := false.B
}

class SerialDriver(implicit p: Parameters) extends NastiModule {
  val w = p(SerialInterfaceWidth)
  val io = IO(new Bundle {
    val axi = new NastiIO
    val serial = new SerialIO(w)
  })

  require(w == nastiXDataBits)

  val outdrv = Module(new OutFIFODriver(0x43C00000L))
  val indrv = Module(new InFIFODriver(0x43C00008L))

  io.axi.ar <> outdrv.io.axi.ar
  io.axi.aw <> indrv.io.axi.aw
  io.axi.w  <> indrv.io.axi.w
  outdrv.io.axi.r <> io.axi.r
  indrv.io.axi.b  <> io.axi.b
  indrv.io.in <> io.serial.in
  io.serial.out <> outdrv.io.out
}

class ResetDriver(implicit p: Parameters) extends NastiModule {
  val io = IO(new Bundle {
    val axi = new NastiIO
  })

  val driver = Module(new SetRegisterDriver(0x43C00010L))
  io.axi <> driver.io.axi
  driver.io.value := 0.U
}

class BlockDeviceDriver(implicit p: Parameters) extends NastiModule {
  val io = IO(new Bundle {
    val axi = new NastiIO
    val bdev = new BlockDeviceIO
  })

  val w = nastiXDataBits
  val desser  = Module(new BlockDeviceDesser(w))
  val reqdrv  = Module(new OutFIFODriver(0x43C00020L))
  val datadrv = Module(new OutFIFODriver(0x43C00028L))
  val respdrv = Module(new InFIFODriver(0x43C00030L))
  val infodrv = Module(new SetRegisterDriver(0x43C00038L))

  io.bdev <> desser.io.bdev
  desser.io.ser.req <> reqdrv.io.out
  desser.io.ser.data <> datadrv.io.out
  respdrv.io.in <> desser.io.ser.resp
  infodrv.io.value := io.bdev.info.nsectors

  val arb = Module(new NastiArbiter(4))
  arb.io.master <> Seq(
    reqdrv.io.axi, datadrv.io.axi, respdrv.io.axi, infodrv.io.axi)
  io.axi <> arb.io.slave
}
