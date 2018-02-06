package zynq

import chisel3._
import freechips.rocketchip.config.{Parameters, Config}
import freechips.rocketchip.coreplex._
import freechips.rocketchip.devices.tilelink.BootROMParams
import freechips.rocketchip.rocket.{RocketCoreParams, MulDivParams, DCacheParams, ICacheParams}
import freechips.rocketchip.tile.{RocketTileParams, BuildCore, XLen}
import icenet.{NICKey, NICConfig}
import testchipip._

class WithBootROM extends Config((site, here, up) => {
  case BootROMParams => BootROMParams(
    contentFileName = s"./bootrom/bootrom.rv${site(XLen)}.img")
})

class WithRocket extends Config((site, here, up) => {
  case UseBoom => false
})

class WithBoom extends Config((site, here, up) => {
  case UseBoom => true
})

class WithZynqAdapter extends Config((site, here, up) => {
  case SerialFIFODepth => 16
  case ResetCycles => 10
  case ZynqAdapterBase => BigInt(0x43C00000L)
  case ExtMem => up(ExtMem, site).copy(idBits = 6)
  case ExtIn => up(ExtIn, site).copy(beatBytes = 4, idBits = 12)
  case BlockDeviceKey => BlockDeviceConfig(nTrackers = 2)
  case BlockDeviceFIFODepth => 16
  case NetworkFIFODepth => 16
  case NICKey => NICConfig(ctrlQueueDepth = 64)
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
        rowBits = site(SystemBusKey).beatBytes*8,
        nSets = 64,
        nWays = 1,
        nTLBEntries = 4,
        nMSHRs = 0,
        blockBytes = site(CacheBlockBytes))),
      icache = Some(ICacheParams(
        rowBits = site(SystemBusKey).beatBytes*8,
        nSets = 64,
        nWays = 1,
        nTLBEntries = 4,
        blockBytes = site(CacheBlockBytes))))
    List.fill(n)(medium) ++ up(RocketTilesKey, site)
  }
})

class DefaultConfig extends Config(
  new WithBootROM ++ new WithRocket ++
  new freechips.rocketchip.system.DefaultConfig)
class DefaultMediumConfig extends Config(
  new WithBootROM ++ new WithNMediumCores(1) ++ new WithRocket ++
  new freechips.rocketchip.system.BaseConfig)
class DefaultSmallConfig extends Config(
  new WithBootROM ++ new WithRocket ++
  new freechips.rocketchip.system.DefaultSmallConfig)

class BoomConfig extends Config(
  new WithBootROM ++ new WithBoom ++
  new boom.system.BoomConfig)
class SmallBoomConfig extends Config(
  new WithBootROM ++ new WithBoom ++
  new boom.system.SmallBoomConfig)

class ZynqConfig extends Config(new WithZynqAdapter ++ new DefaultConfig)
class ZynqMediumConfig extends Config(new WithZynqAdapter ++ new DefaultMediumConfig)
class ZynqSmallConfig extends Config(new WithZynqAdapter ++ new DefaultSmallConfig)

class ZynqBoomConfig extends Config(new WithZynqAdapter ++ new BoomConfig)
class ZynqSmallBoomConfig extends Config(new WithZynqAdapter ++ new SmallBoomConfig)

class ZynqFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqConfig)
class ZynqMediumFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqMediumConfig)
class ZynqSmallFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqSmallConfig)

class ZynqBoomFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqBoomConfig)
class ZynqSmallBoomFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqSmallBoomConfig)
