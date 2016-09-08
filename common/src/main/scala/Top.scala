
package zynq

import Chisel._
import junctions._
import cde.{Parameters, Config, CDEMatchError}
import rocketchip._
import uncore.devices.{DebugBusIO}

import java.io.File

class Top(implicit val p: Parameters) extends Module
  with HasTopLevelParameters {
  val adapterParams = p.alterPartial({
    case NastiKey => NastiParameters(
      dataBits = 32,
      addrBits = 32,
      idBits = 12)
  })
  val io = new Bundle {
    val ps_axi_slave = new NastiIO()(adapterParams).flip
    val mem_axi = Vec(nMemAXIChannels, new NastiIO)
  }

  val adapter = Module(new ZynqAdapter()(adapterParams))
  val rocket = Module(new rocketchip.Top(p))

  adapter.io.nasti <> io.ps_axi_slave
  rocket.reset := adapter.io.reset
  val debug = rocket.io.debug.getOrElse(
    throw new RuntimeException("Zynq top requires bare DTM interface"))
  debug <> adapter.io.debug
  io.mem_axi <> rocket.io.mem_axi
  rocket.io.interrupts map(_ := Bool(false))
}

object FPGAZynqGenerator extends Generator {
  val longName = names.topModuleClass + "." + names.configs
  val td = names.targetDir
  Driver.dumpFirrtl(circuit, Some(new File(td, s"$longName.fir"))) // FIRRTL
}
