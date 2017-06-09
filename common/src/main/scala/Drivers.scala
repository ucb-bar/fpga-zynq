package zynq

import chisel3._
import chisel3.util._
import junctions._
import config.Parameters
import _root_.util._
import testchipip._

class SerialDriver(implicit p: Parameters) extends NastiModule {
  val w = p(SerialInterfaceWidth)
  val io = IO(new Bundle {
    val axi = new NastiIO
    val serial = new SerialIO(w)
  })

  val indrv = Module(new SerialInDriver)
  val outdrv = Module(new SerialOutDriver)

  io.axi.ar <> outdrv.io.axi.ar
  io.axi.aw <> indrv.io.axi.aw
  io.axi.w  <> indrv.io.axi.w
  outdrv.io.axi.r <> io.axi.r
  indrv.io.axi.b  <> io.axi.b
  indrv.io.serial.in <> io.serial.in
  io.serial.out <> outdrv.io.serial.out
}

class SerialInDriver(implicit p: Parameters) extends NastiModule {
  val w = p(SerialInterfaceWidth)
  val io = IO(new Bundle {
    val axi = new NastiIO
    val serial = new SerialIO(w)
  })

  val s_idle :: s_waddr :: s_wdata :: s_wresp :: Nil = Enum(4)
  val state = RegInit(s_idle)
  val data = Reg(UInt(w.W))

  io.axi.aw.valid := state === s_waddr
  io.axi.aw.bits := NastiWriteAddressChannel(
    id = 0.U,
    addr = 0x43C00008L.U,
    size = log2Ceil(w/8).U)
  io.axi.w.valid := state === s_wdata
  io.axi.w.bits := NastiWriteDataChannel(data = data)
  io.axi.b.ready := state === s_wresp
  io.serial.in.ready := state === s_idle

  when (io.serial.in.fire()) {
    data := io.serial.in.bits
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

class SerialOutDriver(implicit p: Parameters) extends NastiModule {
  val w = p(SerialInterfaceWidth)
  val io = IO(new Bundle {
    val axi = new NastiIO
    val serial = new SerialIO(w)
  })

  val busy = RegInit(false.B)

  io.axi.ar.valid := !busy
  io.axi.ar.bits := NastiReadAddressChannel(
    id = 0.U,
    addr = 0x43C00000L.U,
    size = log2Ceil(w/8).U)
  io.axi.r.ready := io.serial.out.ready
  io.serial.out.valid := io.axi.r.valid
  io.serial.out.bits := io.axi.r.bits.data

  when (io.axi.ar.fire()) { busy := true.B }
  when (io.axi.r.fire()) { busy := false.B }
}

class ResetDriver(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val axi = new NastiIO
  })

  val (s_idle :: s_write_addr :: s_write_data :: s_done :: Nil) = Enum(4)
  val state = RegInit(s_idle)

  when (state === s_idle) { state := s_write_addr }
  when (io.axi.aw.fire()) { state := s_write_data }
  when (io.axi.w.fire()) { state := s_done }

  io.axi.aw.valid := state === s_write_addr
  io.axi.aw.bits := NastiWriteAddressChannel(
    id = 0.U,
    addr = 0x43C00010L.U,
    size = 2.U)

  io.axi.w.valid := state === s_write_data
  io.axi.w.bits := NastiWriteDataChannel(data = 0.U)

  io.axi.b.ready := (state === s_done)
  io.axi.ar.valid := false.B
  io.axi.r.ready := false.B
}
