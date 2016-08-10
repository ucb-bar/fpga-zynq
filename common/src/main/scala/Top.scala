
package zynq

import Chisel._
import junctions._
import cde.{Parameters, Config, CDEMatchError}
import rocketchip._
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

// This copied directly from rocketchip 2a5aeea
// Could invoke RC main directly, but don't want all the test generation
object Generator extends App {
  val projectName = args(0)
  val topModuleName = args(1)
  val configClassName = args(2)

  val aggregateConfigs = configClassName.split('_')

  val finalConfig = aggregateConfigs.foldRight(new Config()) { case (currentConfigName, finalConfig) =>
    val currentConfig = try {
      Class.forName(s"$projectName.$currentConfigName").newInstance.asInstanceOf[Config]
    } catch {
      case e: java.lang.ClassNotFoundException =>
        throwException("Unable to find part \"" + currentConfigName +
          "\" of configClassName \"" + configClassName +
          "\", did you misspell it?", e)
    }
    currentConfig ++ finalConfig
  }
  val world = finalConfig.toInstance

  val paramsFromConfig: Parameters = Parameters.root(world)

  val gen = () =>
    Class.forName(s"$projectName.$topModuleName")
      .getConstructor(classOf[cde.Parameters])
      .newInstance(paramsFromConfig)
      .asInstanceOf[Module]

  chiselMain.run(args.drop(3), gen)
}
