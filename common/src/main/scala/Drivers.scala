package zynq

import chisel3._
import chisel3.util._
import junctions._
import config.Parameters
import _root_.util._
import testchipip._

class InFIFODriver(addr: BigInt, maxSpace: Int)(implicit p: Parameters)
    extends NastiModule {
  val w = nastiXDataBits
  val io = IO(new Bundle {
    val axi = new NastiIO
    val in = Flipped(Decoupled(UInt(w.W)))
  })

  val timeout = 64
  val timer = RegInit(0.U(log2Ceil(timeout).W))
  val space = RegInit(0.U(log2Ceil(maxSpace + 1).W))
  val (s_start :: s_raddr :: s_rdata ::
       s_req :: s_waddr :: s_wdata :: s_wresp :: Nil) = Enum(7)
  val state = RegInit(s_start)
  val data = Reg(UInt(w.W))

  io.axi.ar.valid := state === s_raddr
  io.axi.ar.bits := NastiReadAddressChannel(
    id = 0.U,
    addr = (addr + 4).U,
    size = log2Ceil(w/8).U)
  io.axi.r.ready := state === s_rdata
  io.axi.aw.valid := state === s_waddr
  io.axi.aw.bits := NastiWriteAddressChannel(
    id = 0.U,
    addr = addr.U,
    size = log2Ceil(w/8).U)
  io.axi.w.valid := state === s_wdata
  io.axi.w.bits := NastiWriteDataChannel(data = data)
  io.axi.b.ready := state === s_wresp
  io.in.ready := state === s_req

  when (state === s_start) {
    when (space =/= 0.U) {
      state := s_req
    } .elsewhen (timer === 0.U) {
      timer := (timeout - 1).U
      state := s_raddr
    } .otherwise {
      timer := timer - 1.U
    }
  }

  when (io.axi.ar.fire()) { state := s_rdata }
  when (io.axi.r.fire()) {
    space := io.axi.r.bits.data
    state := s_start
  }

  when (io.in.fire()) {
    data := io.in.bits
    space := space - 1.U
    state := s_waddr
  }

  when (io.axi.aw.fire()) { state := s_wdata }
  when (io.axi.w.fire()) { state := s_wresp }
  when (io.axi.b.fire()) { state := s_start }
}

class OutFIFODriver(addr: BigInt, maxCount: Int)(implicit p: Parameters)
    extends NastiModule {
  val w = nastiXDataBits
  val io = IO(new Bundle {
    val axi = new NastiIO
    val out = Decoupled(UInt(w.W))
  })

  val timeout = 64
  val timer = RegInit(0.U(log2Ceil(timeout).W))
  val count = RegInit(0.U(log2Ceil(maxCount + 1).W))
  val (s_start :: s_raddr_count :: s_rdata_count ::
       s_raddr_fifo :: s_rdata_fifo :: Nil) = Enum(5)
  val state = RegInit(s_start)

  io.axi.ar.valid := state.isOneOf(s_raddr_count, s_raddr_fifo)
  io.axi.ar.bits := NastiReadAddressChannel(
    id = 0.U,
    addr = Mux(state === s_raddr_count, (addr + 4).U, addr.U),
    size = log2Ceil(w/8).U)

  io.axi.r.ready :=
    (state === s_rdata_count) ||
    (state === s_rdata_fifo && io.out.ready)
  io.out.valid := state === s_rdata_fifo && io.axi.r.valid
  io.out.bits := io.axi.r.bits.data

  io.axi.aw.valid := false.B
  io.axi.w.valid := false.B
  io.axi.b.ready := false.B

  when (state === s_start) {
    when (count =/= 0.U) {
      state := s_raddr_fifo
    } .elsewhen (timer === 0.U) {
      timer := (timeout - 1).U
      state := s_raddr_count
    } .otherwise {
      timer := timer - 1.U
    }
  }

  when (io.axi.ar.fire()) {
    state := Mux(state === s_raddr_count, s_rdata_count, s_rdata_fifo)
  }

  when (io.axi.r.fire()) {
    count := Mux(state === s_rdata_count, io.axi.r.bits.data, count - 1.U)
    state := s_start
  }
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

  val base = p(ZynqAdapterBase)
  val depth = p(SerialFIFODepth)
  val outdrv = Module(new OutFIFODriver(base, depth))
  val indrv = Module(new InFIFODriver(base + BigInt(8), depth))
  val arb = Module(new NastiArbiter(2))

  arb.io.master <> Seq(outdrv.io.axi, indrv.io.axi)
  io.axi <> arb.io.slave
  indrv.io.in <> io.serial.in
  io.serial.out <> outdrv.io.out
}

class ResetDriver(implicit p: Parameters) extends NastiModule {
  val io = IO(new Bundle {
    val axi = new NastiIO
  })

  val base = p(ZynqAdapterBase)
  val driver = Module(new SetRegisterDriver(base + BigInt(0x10)))
  io.axi <> driver.io.axi
  driver.io.value := 0.U
}

class BlockDeviceDriver(implicit p: Parameters) extends NastiModule {
  val io = IO(new Bundle {
    val axi = new NastiIO
    val bdev = new BlockDeviceIO
  })

  val w = nastiXDataBits
  val base = p(ZynqAdapterBase)
  val depth = p(BlockDeviceFIFODepth)
  val desser  = Module(new BlockDeviceDesser(w))
  val reqdrv  = Module(new OutFIFODriver(base + BigInt(0x20), depth))
  val datadrv = Module(new OutFIFODriver(base + BigInt(0x28), depth))
  val respdrv = Module(new InFIFODriver(base + BigInt(0x30), depth))
  val infodrv = Module(new SetRegisterDriver(base + BigInt(0x38)))

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
