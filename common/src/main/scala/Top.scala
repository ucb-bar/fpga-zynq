
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

object Generator extends App {
  // Check the current project, before looking up the configuration in RC
  def getConfig(projectName: String, configClassName: String): Config = {
    val aggregateConfigs = configClassName.split('_')

    aggregateConfigs.foldRight(new Config()) { case (currentConfigName, finalConfig) =>
      val currentConfig = try {
        try {
          // Look locally first, before looking in rocketchip
          Class.forName(s"$projectName.$currentConfigName").newInstance.asInstanceOf[Config]
        } catch {
          case e: java.lang.ClassNotFoundException =>
            Class.forName(s"rocketchip.$currentConfigName").newInstance.asInstanceOf[Config]
        }
      } catch {
        case e: java.lang.ClassNotFoundException =>
          throwException("Unable to find part \"" + currentConfigName +
            "\" of configClassName \"" + configClassName +
            "\", did you misspell it?", e)
      }
      currentConfig ++ finalConfig
    }
  }
  val projectName = args(0)
  val topModuleName = args(1)
  val configClassName = args(2)
  val config = getConfig(projectName, configClassName)
  val paramsFromConfig = getParameters(config)

  elaborate(s"$projectName.$topModuleName", args.drop(3), paramsFromConfig)
}
