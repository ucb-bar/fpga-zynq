package zynq

import cde.{Parameters, Config, CDEMatchError}
import rocketchip._
import rocket.{TileId, NUncachedTileLinkPorts}
import coreplex.BuildTiles
import uncore.devices.NTiles
import uncore.tilelink.TLId
import testchipip._
import Chisel._

class WithZynqAdapter extends Config(
  (pname, site, here) => pname match {
    case SerialInterfaceWidth => 32
    case SerialFIFODepth => 16
    case BuildSerialDriver =>
      (p: Parameters) => Module(new SimSerialWrapper(p(SerialInterfaceWidth)))
    case _ => throw new CDEMatchError
  })

class ZynqConfig extends Config(new WithZynqAdapter ++ new DefaultFPGAConfig)
class ZynqSmallConfig extends Config(new WithZynqAdapter ++ new DefaultFPGASmallConfig)

class WithIntegrationTest extends Config(
  (pname, site, here) => pname match {
    case BuildSerialDriver =>
      (p: Parameters) => Module(new IntegrationTestSerial()(p))
    case BuildTiles => Seq.fill(site(NTiles)) {
      (_reset: Bool, p: Parameters) => Module(new DummyTile()(p.alterPartial({
        case TileId => 0
        case TLId => "L1toL2"
        case NUncachedTileLinkPorts => 1
      })))
    }
    case _ => throw new CDEMatchError
  })

class IntegrationTestConfig extends Config(new WithIntegrationTest ++ new ZynqConfig)
