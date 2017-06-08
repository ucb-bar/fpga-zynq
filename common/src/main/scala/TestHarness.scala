package zynq

import chisel3._
import chisel3.util.Queue
import config.Parameters
import diplomacy.LazyModule
import rocketchip._
import uncore.coherence.ClientMetadata
import junctions.SerialIO
import testchipip._

class TestHarness(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val driver = Module(new TestHarnessDriver()(AdapterParams(p)))
  val dut = LazyModule(new FPGAZynqTop).module
  dut.reset := driver.io.sys_reset
  driver.io.serial <> dut.io.serial
  driver.io.bdev <> dut.io.bdev
  io.success := driver.io.success

  val nMemChannels = p(coreplex.BankedL2Config).nMemoryChannels
  val mem = Module(LazyModule(new SimAXIMem(nMemChannels)).module)
  mem.io.axi4 <> dut.io.mem_axi4
}

class TestHarnessDriver(implicit p: Parameters) extends Module {
  val serialWidth = p(SerialInterfaceWidth)
  val io = IO(new Bundle {
    val serial = Flipped(new SerialIO(serialWidth))
    val bdev = Flipped(new BlockDeviceIO)
    val sys_reset = Output(Bool())
    val success = Output(Bool())
  })

  val zynq = Module(new ZynqAdapter(2))
  val serDriver = Module(new SerialDriver)
  val resetDriver = Module(new ResetDriver)
  val simSerial = Module(new SimSerialWrapper(serialWidth))
  val simBlockDev = Module(new SimBlockDevice)
  simBlockDev.io.clock := clock
  simBlockDev.io.reset := reset

  zynq.io.axi(0) <> serDriver.io.axi
  zynq.io.axi(1) <> resetDriver.io.axi

  zynq.io.serial <> io.serial
  simSerial.io.serial <> serDriver.io.serial
  simBlockDev.io.bdev <> io.bdev

  io.sys_reset := zynq.io.sys_reset
  io.success := simSerial.io.exit
}
