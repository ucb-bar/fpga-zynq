package zynq

import Chisel._
import config.Parameters
import diplomacy.LazyModule
import rocketchip._
import rocket.Tile
import uncore.tilelink.{ClientTileLinkIO, ClientUncachedTileLinkIO}
import uncore.coherence.ClientMetadata
import junctions.SerialIO

class TestHarness(implicit val p: Parameters) extends Module {
  val io = new Bundle {
    val success = Bool(OUTPUT)
  }

  val dut = LazyModule(new FPGAZynqTop(p)).module
  val mem = Module(new SimAXIMem(BigInt(p(ExtMemSize))))
  val ser = p(BuildSerialDriver)(p)

  mem.io.axi <> dut.io.mem_axi.head
  ser.io.serial <> dut.io.serial
  io.success := ser.io.exit
}

class DummyTile(implicit p: Parameters) extends Tile()(p) {
  def tieOff(cached: ClientTileLinkIO) {
    cached.acquire.valid := Bool(false)
    cached.grant.ready := Bool(false)
    cached.finish.valid := Bool(false)

    val prb = Queue(cached.probe)
    cached.release.valid := prb.valid
    prb.ready := cached.release.ready
    cached.release.bits := ClientMetadata.onReset.makeRelease(prb.bits)
  }

  def tieOff(uncached: ClientUncachedTileLinkIO) {
    uncached.acquire.valid := Bool(false)
    uncached.grant.ready := Bool(false)
  }

  io.cached.foreach(tieOff(_))
  io.uncached.foreach(tieOff(_))

  require(io.slave.isEmpty)
}
