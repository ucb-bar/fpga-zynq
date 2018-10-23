package zynq

import chisel3._
import chisel3.util.Queue
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.subsystem.ExtIn
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp}
import freechips.rocketchip.tilelink._
import testchipip._
import testchipip.SerialAdapter._

class TestHarness(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val config = p(ExtIn)
  val driver = Module(LazyModule(new TestHarnessDriver).module)
  val dut = Module(LazyModule(new FPGAZynqTop).module)

  dut.reset := driver.io.sys_reset
  dut.debug := DontCare
  dut.tieOffInterrupts()
  dut.dontTouchPorts()
  dut.connectSimAXIMem()

  driver.io.serial <> dut.serial
  driver.io.bdev <> dut.bdev
  io.success := driver.io.success
}

class TestHarnessDriver(implicit p: Parameters) extends LazyModule {
  val xbar = LazyModule(new TLXbar)
  val config = p(ExtIn)
  val base = p(ZynqAdapterBase)

  val zynq = LazyModule(new ZynqAdapterCore(base, config.get.beatBytes))
  val converter = LazyModule(new TLToAXI4)

  val serDriver = LazyModule(new SerialDriver)
  val resetDriver = LazyModule(new ResetDriver)
  val blkdevDriver = LazyModule(new BlockDeviceDriver)

  xbar.node := serDriver.node
  xbar.node := resetDriver.node
  xbar.node := blkdevDriver.node
  converter.node := xbar.node
  zynq.node := converter.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serial = Flipped(new SerialIO(SERIAL_IF_WIDTH))
      val bdev = Flipped(new BlockDeviceIO)
      val sys_reset = Output(Bool())
      val success = Output(Bool())
    })

    val simSerial = Module(new SimSerial(SERIAL_IF_WIDTH))
    val simBlockDev = Module(new SimBlockDevice)
    simSerial.io.clock := clock
    simSerial.io.reset := reset
    simBlockDev.io.clock := clock
    simBlockDev.io.reset := reset
    serDriver.module.reset := zynq.module.io.sys_reset
    blkdevDriver.module.reset := zynq.module.io.sys_reset

    zynq.module.io.serial <> io.serial
    simSerial.io.serial <> serDriver.module.io.serial
    zynq.module.io.bdev <> io.bdev
    simBlockDev.io.bdev <> blkdevDriver.module.io.bdev

    io.sys_reset := zynq.module.io.sys_reset
    io.success := simSerial.io.exit
  }
}
