
package zynq

import Chisel._
import junctions._
import cde.{Parameters, Config, CDEMatchError}
import rocketchip._
import uncore.devices.{DebugBusIO}
import uncore.tilelink2.{LazyModule, LazyModuleImp}

import java.io.File

class Top(implicit val p: Parameters) extends Module {
  val io = new Bundle {
    val ps_axi_slave = new NastiIO()(AdapterParams(p)).flip
    val mem_axi = new NastiIO
  }

  val realtop = LazyModule(new FPGAZynqTop(p)).module
  realtop.io.ps_axi_slave <> io.ps_axi_slave
  io.mem_axi <> realtop.io.mem_axi.head

  require(realtop.io.mem_axi.size == 1)
  require(realtop.io.mem_ahb.isEmpty)
  require(realtop.io.mem_tl.isEmpty)
  require(realtop.io.mem_clk.isEmpty)
  require(realtop.io.mem_rst.isEmpty)
}

class FPGAZynqTop(q: Parameters) extends BaseTop(q)
    with PeripheryBootROM with PeripheryCoreplexLocalInterrupter
    with PeripheryZynq with PeripheryMasterMem {
  override lazy val module = Module(
    new FPGAZynqTopModule(p, this, new FPGAZynqTopBundle(p)))
}

class FPGAZynqTopBundle(p: Parameters) extends BaseTopBundle(p)
  with PeripheryBootROMBundle with PeripheryCoreplexLocalInterrupterBundle
  with PeripheryMasterMemBundle with PeripheryZynqBundle

class FPGAZynqTopModule(p: Parameters, l: FPGAZynqTop, b: => FPGAZynqTopBundle)
  extends BaseTopModule(p, l, b)
  with PeripheryBootROMModule with PeripheryCoreplexLocalInterrupterModule
  with PeripheryMasterMemModule with PeripheryZynqModule
  with HardwiredResetVector

object FPGAZynqGenerator extends Generator {
  val longName = names.topModuleClass + "." + names.configs
  generateFirrtl
}
