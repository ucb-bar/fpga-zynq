package zynq

import chisel3._
import config.{Parameters, Config}
import coreplex.{RocketTilesKey, L1toL2Config, CacheBlockBytes}
import rocket.{RocketTileParams, RocketCoreParams, MulDivParams, DCacheParams, ICacheParams}
import rocketchip.{ExtMem, DefaultConfig, DefaultSmallConfig, BaseConfig, WithoutTLMonitors}
import testchipip._
import tile.BuildCore

class WithZynqAdapter extends Config((site, here, up) => {
  case SerialInterfaceWidth => 32
  case SerialFIFODepth => 16
  case ResetCycles => 10
  case BuildSerialDriver =>
    (p: Parameters) => Module(new SimSerialWrapper(p(SerialInterfaceWidth)))
  case ExtMem => up(ExtMem, site).copy(idBits = 6)
})

class WithNMediumCores(n: Int) extends Config((site, here, up) => {
  case RocketTilesKey => {
    val medium = RocketTileParams(
      core = RocketCoreParams(mulDiv = Some(MulDivParams(
        mulUnroll = 8,
        mulEarlyOut = true,
        divEarlyOut = true)),
        fpu = None),
      dcache = Some(DCacheParams(
        rowBits = site(L1toL2Config).beatBytes*8,
        nSets = 64,
        nWays = 1,
        nTLBEntries = 4,
        nMSHRs = 0,
        blockBytes = site(CacheBlockBytes))),
      icache = Some(ICacheParams(
        rowBits = site(L1toL2Config).beatBytes*8,
        nSets = 64,
        nWays = 1,
        nTLBEntries = 4,
        blockBytes = site(CacheBlockBytes))))
    List.fill(n)(medium) ++ up(RocketTilesKey, site)
  }
})

class DefaultMediumConfig extends Config(new WithNMediumCores(1) ++ new BaseConfig)

class ZynqConfig extends Config(new WithZynqAdapter ++ new DefaultConfig)
class ZynqMediumConfig extends Config(new WithZynqAdapter ++ new DefaultMediumConfig)
class ZynqSmallConfig extends Config(new WithZynqAdapter ++ new DefaultSmallConfig)

class ZynqFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqConfig)
class ZynqMediumFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqMediumConfig)
class ZynqSmallFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqSmallConfig)

class WithIntegrationTest extends Config((site, here, up) => {
  case BuildCore => (p: Parameters) => new DummyCore()(p)
  case BuildSerialDriver =>
    (p: Parameters) => Module(new IntegrationTestSerial()(p))
})

class IntegrationTestConfig extends Config(
  new WithIntegrationTest ++ new ZynqSmallConfig)
