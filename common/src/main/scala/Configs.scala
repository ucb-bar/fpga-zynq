package zynq

import config.{Parameters, Config, CDEMatchError}
import coreplex.{DefaultCoreplex, WithSmallCores}
import diplomacy.LazyModule
import junctions.{NastiKey, NastiParameters}
import uncore.tilelink.TLId
import rocketchip._
import rocket._
import testchipip._
import Chisel._

class WithZynqAdapter extends Config(
  (pname, site, here) => pname match {
    case SerialInterfaceWidth => 32
    case SerialFIFODepth => 16
    case ResetCycles => 10
    case NExtTopInterrupts => 0
    // This should be removed once adapter is TL2
    case TLId => "L1toL2"
    // PS slave interface
    case NastiKey => NastiParameters(32, 32, 12)
    // To PS Memory System / MIG
    case ExtMem => MasterConfig(base=0x80000000L, size=0x10000000L, beatBytes=8, idBits=6)
    case _ => throw new CDEMatchError
  })

class ZynqConfig extends Config(new WithZynqAdapter ++ new DefaultFPGAConfig)
class ZynqSmallConfig extends Config(new WithSmallCores ++ new ZynqConfig)
class ZC706MIGConfig extends Config(new WithExtMemSize(0x40000000L) ++ new ZynqConfig)
class ZC706MIG2GConfig extends Config(new WithExtMemSize(0x80000000L) ++ new ZynqConfig)
class With8GExtMem extends Config(
  (pname, site, here) => pname match {
    case ExtMem => MasterConfig(base=0x200000000L, size=0x200000000L, beatBytes=8, idBits=6)
    case _ => throw new CDEMatchError
  })
// TODO: This is broken.
class ZC706MIG8GConfig extends Config(new With8GExtMem ++ new ZynqConfig)
