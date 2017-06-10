package zynq

import chisel3._
import diplomacy.{LazyModule, LazyModuleImp}
import config.{Parameters, Field}
import rocketchip._
import testchipip._

class Top(implicit val p: Parameters) extends Module {
  val extMem = p(ExtMem)
  val inParams = AdapterParams(p)

  val target = LazyModule(new FPGAZynqTop).module
  val slave = Module(new ZynqAdapter(1)(inParams))

  require(target.io.mem_axi4.size == 1)

  val io = IO(new Bundle {
    val ps_axi_slave = Flipped(slave.io.axi.head.cloneType)
    val mem_axi = target.io.mem_axi4.head.cloneType
  })

  io.mem_axi <> target.io.mem_axi4.head
  slave.io.axi.head <> io.ps_axi_slave
  slave.io.serial <> target.io.serial
  slave.io.bdev <> target.io.bdev
  target.reset := slave.io.sys_reset
}

class FPGAZynqTop(implicit p: Parameters) extends BaseTop
    with PeripheryMasterAXI4Mem
    with PeripheryBootROM
    with PeripheryZero
    with PeripheryCounter
    with HardwiredResetVector
    with RocketPlexMaster
    with NoDebug
    with PeripherySerial
    with PeripheryBlockDevice {
  override lazy val module = Module(
    new FPGAZynqTopModule(this, () => new FPGAZynqTopBundle(this)))
}

class FPGAZynqTopBundle(outer: FPGAZynqTop) extends BaseTopBundle(outer)
    with PeripheryMasterAXI4MemBundle
    with PeripheryBootROMBundle
    with PeripheryZeroBundle
    with PeripheryCounterBundle
    with HardwiredResetVectorBundle
    with RocketPlexMasterBundle
    with PeripherySerialBundle
    with PeripheryBlockDeviceBundle

class FPGAZynqTopModule(outer: FPGAZynqTop, bundle: () => FPGAZynqTopBundle)
  extends BaseTopModule(outer, bundle)
    with PeripheryMasterAXI4MemModule
    with PeripheryBootROMModule
    with PeripheryZeroModule
    with PeripheryCounterModule
    with HardwiredResetVectorModule
    with RocketPlexMasterModule
    with NoDebugModule
    with PeripherySerialModule
    with PeripheryBlockDeviceModule
