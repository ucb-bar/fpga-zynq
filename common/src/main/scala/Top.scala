
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
  val projectName = args(0)
  val topModuleName = args(1)
  // arg(2) = rocketchip -> reuse existing rocketchip configurations
  // arg(2) = zynq -> use new configurations defined here
  val configProjectName = args(2)
  val configClassName = args(3)
  val paramsFromConfig = getParameters(configProjectName, configClassName)

  elaborate(s"$projectName.$topModuleName", args.drop(4), paramsFromConfig)
}
