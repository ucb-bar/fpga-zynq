package zynq

import boom.system.{BoomCoreplex, BoomCoreplexModule}
import chisel3._
import freechips.rocketchip.config.{Parameters, Field}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp}
import freechips.rocketchip.util.DontTouch
import testchipip._
import icenet._

case object ZynqAdapterBase extends Field[BigInt]
case object UseBoom extends Field[Boolean]

class Top(implicit val p: Parameters) extends Module {
  val address = p(ZynqAdapterBase)
  val config = p(ExtIn)
  val adapter = Module(LazyModule(new ZynqAdapter(address, config)).module)
  val target = if (p(UseBoom)) {
    Module(LazyModule(new FPGAZynqBoomTop).module)
  } else {
    Module(LazyModule(new FPGAZynqTop).module)
  }

  require(target.mem_axi4.size == 1)

  val io = IO(new Bundle {
    val ps_axi_slave = Flipped(adapter.axi.cloneType)
    val mem_axi = target.mem_axi4.head.cloneType
  })

  io.mem_axi <> target.mem_axi4.head
  adapter.axi <> io.ps_axi_slave

  adapter.io.serial <> target.serial
  adapter.io.bdev <> target.bdev
  adapter.io.net <> target.net

  target.debug := DontCare
  target.tieOffInterrupts()
  target.dontTouchPorts()
  target.reset := adapter.io.sys_reset
}

class FPGAZynqTop(implicit p: Parameters) extends RocketSubsystem
    with CanHaveMasterAXI4MemPort
    with HasSystemErrorSlave
    with HasPeripheryBootROM
    with HasSyncExtInterrupts
    with HasNoDebug
    with HasPeripherySerial
    with HasPeripheryBlockDevice
    with HasPeripheryIceNIC {
  override lazy val module = new FPGAZynqTopModule(this)
}

class FPGAZynqTopModule(outer: FPGAZynqTop) extends RocketCoreplexModule(outer)
    with HasRTCModuleImp
    with HasMasterAXI4MemPortModuleImp
    with HasPeripheryBootROMModuleImp
    with HasExtInterruptsModuleImp
    with HasNoDebugModuleImp
    with HasPeripherySerialModuleImp
    with HasPeripheryBlockDeviceModuleImp
    with HasPeripheryIceNICModuleImp
    with DontTouch

class FPGAZynqBoomTop(implicit p: Parameters) extends BoomCoreplex
    with HasMasterAXI4MemPort
    with HasSystemErrorSlave
    with HasPeripheryBootROM
    with HasSyncExtInterrupts
    with HasNoDebug
    with HasPeripherySerial
    with HasPeripheryBlockDevice
    with HasPeripheryIceNIC {
  override lazy val module = new FPGAZynqBoomTopModule(this)
}

class FPGAZynqBoomTopModule(outer: FPGAZynqBoomTop) extends BoomCoreplexModule(outer)
    with HasRTCModuleImp
    with HasMasterAXI4MemPortModuleImp
    with HasPeripheryBootROMModuleImp
    with HasExtInterruptsModuleImp
    with HasNoDebugModuleImp
    with HasPeripherySerialModuleImp
    with HasPeripheryBlockDeviceModuleImp
    with HasPeripheryIceNICModuleImp
    with DontTouch
