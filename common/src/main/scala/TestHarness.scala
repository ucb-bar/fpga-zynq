package zynq

import chisel3._
import chisel3.util.Queue
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.chip._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp}
import freechips.rocketchip.rocket.PAddrBits
import freechips.rocketchip.tilelink._
import testchipip._
import testchipip.SerialAdapter._
import testchipip.SimpleNIC._

class TestHarness(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val config = p(ExtIn)
  val driver = Module(LazyModule(new TestHarnessDriver).module)
  val dut = Module(LazyModule(new FPGAZynqTop).module)
  dut.reset := driver.io.sys_reset
  driver.io.serial <> dut.serial
  driver.io.bdev <> dut.bdev
  driver.io.net <> dut.net
  io.success := driver.io.success

  dut.connectSimAXIMem()
}

class TestHarnessDriver(implicit p: Parameters) extends LazyModule {
  val xbar = LazyModule(new TLXbar)
  val config = p(ExtIn)
  val base = p(ZynqAdapterBase)

  val zynq = LazyModule(new ZynqAdapterCore(base, config.beatBytes))
  val converter = LazyModule(new TLToAXI4(config.beatBytes))

  val serDriver = LazyModule(new SerialDriver)
  val resetDriver = LazyModule(new ResetDriver)
  val blkdevDriver = LazyModule(new BlockDeviceDriver)
  val netDriver = LazyModule(new NetworkDriver)

  xbar.node := serDriver.node
  xbar.node := resetDriver.node
  xbar.node := blkdevDriver.node
  xbar.node := netDriver.node
  converter.node := xbar.node
  zynq.node := converter.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serial = Flipped(new SerialIO(SERIAL_IF_WIDTH))
      val bdev = Flipped(new BlockDeviceIO)
      val net = Flipped(new StreamIO(NET_IF_WIDTH))
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
    netDriver.module.reset := zynq.module.io.sys_reset

    zynq.module.io.serial <> io.serial
    simSerial.io.serial <> serDriver.module.io.serial
    zynq.module.io.bdev <> io.bdev
    zynq.module.io.net <> io.net
    simBlockDev.io.bdev <> blkdevDriver.module.io.bdev
    // Loopback for network driver
    netDriver.module.io.net.in <> Queue(netDriver.module.io.net.out, 64)

    io.sys_reset := zynq.module.io.sys_reset
    io.success := simSerial.io.exit
  }
}
