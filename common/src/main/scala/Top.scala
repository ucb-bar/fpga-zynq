package zynq

import chisel3._
import diplomacy.{LazyModule, LazyModuleImp}
import config.{Parameters, Field}
import rocketchip._
import testchipip._

case object ZynqAdapterBase extends Field[BigInt]

class Top(implicit val p: Parameters) extends Module {
  val address = p(ZynqAdapterBase)
  val config = p(ExtIn)
  val target = Module(LazyModule(new FPGAZynqTop).module)
  val adapter = Module(LazyModule(new ZynqAdapter(address, config)).module)

  require(target.io.mem_axi4.size == 1)

  val io = IO(new Bundle {
    val ps_axi_slave = Flipped(adapter.io.axi.head.cloneType)
    val mem_axi = target.io.mem_axi4.head.cloneType
  })

  io.mem_axi <> target.io.mem_axi4.head
  adapter.io.axi.head <> io.ps_axi_slave
  adapter.io.serial <> target.io.serial
  adapter.io.bdev <> target.io.bdev
  target.reset := adapter.io.sys_reset
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
  override lazy val module = new FPGAZynqTopModule(this, () => new FPGAZynqTopBundle(this))
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
