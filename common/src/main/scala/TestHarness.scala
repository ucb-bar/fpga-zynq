package zynq

import chisel3._
import chisel3.util.Queue
import config.Parameters
import diplomacy.LazyModule
import rocketchip._
import uncore.coherence.ClientMetadata
import junctions.SerialIO

class TestHarness(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val dut = LazyModule(new FPGAZynqTop()(p)).module
  val ser = p(BuildSerialDriver)(p)

  val nMemChannels = p(coreplex.BankedL2Config).nMemoryChannels
  val mem = Module(LazyModule(new SimAXIMem(nMemChannels)).module)
  mem.io.axi4 <> dut.io.mem_axi4
  ser.io.serial <> dut.io.serial
  io.success := ser.io.exit
}
