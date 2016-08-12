
package zynq

import Chisel._
import junctions._
import cde.{Parameters, Config, CDEMatchError}
import rocketchip._
import rocketchip.GeneratorUtils._
import uncore.devices.{DebugBusIO}

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
  rocket.io.debug <> adapter.io.debug
  io.mem_axi <> rocket.io.mem_axi
  rocket.io.interrupts map(_ := Bool(false))
}

/* Would like to disable interrupts in zynq configurations
class ZynqConfig extends Config(
  (pname, site, here) => pname match {
    case NExtInterrupts => 0
    case _ => throw new CDEMatchError
  })
*/

// Do this to avoid looking up the config in a second (in this case, RC) project
class DefaultFPGAConfig extends Config(new rocketchip.DefaultFPGAConfig)
class DefaultFPGASmallConfig extends Config(new rocketchip.DefaultFPGASmallConfig)

object Generator extends App {
  val projectName = args(0)
  val topModuleName = args(1)
  val configClassName = args(2)

  val config = getConfig(projectName, configClassName)
  val world = config.toInstance
  val paramsFromConfig = Parameters.root(world)

  elaborate(s"$projectName.$topModuleName", args.drop(3), paramsFromConfig)
}
