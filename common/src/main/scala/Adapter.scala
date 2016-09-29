package zynq

import scala.math.min
import Chisel._
import uncore.tilelink._
import uncore.tilelink2.LazyModule
import uncore.util._
import coreplex.BaseCoreplexBundle
import junctions._
import uncore.devices.{DebugBusIO, DebugBusReq, DebugBusResp, DMKey}
import uncore.devices.DbBusConsts._
import rocketchip._
import cde.{Parameters, Field}

case object SerialInterfaceWidth extends Field[Int]
case object SerialFIFODepth extends Field[Int]

object AdapterParams {
  def apply(p: Parameters) = p.alterPartial({
    case NastiKey => NastiParameters(
      dataBits = 32,
      addrBits = 32,
      idBits = 12)
    case TLId => "L1toL2"
  })
}

class ZynqAdapter(implicit p: Parameters) extends TLModule()(p) {
  val w = p(SerialInterfaceWidth)
  val io = new Bundle {
    val serial = new SerialIO(w)
    val master = new ClientUncachedTileLinkIO
    val slave = (new ClientUncachedTileLinkIO).flip
    val sys_reset = Bool(OUTPUT)
    val interrupt = Bool(OUTPUT)
  }

  val nChunksPerBeat = tlDataBits / w
  val pAddrBits = p(PAddrBits)
  val nChunksPerWord = pAddrBits / w

  val resetCycles = 10

  require(nChunksPerBeat > 0, s"Serial interface width must be <= TileLink width $tlDataBits")
  require(nChunksPerWord > 0, s"Serial interface width must be <= PAddrBits $pAddrBits")

  val cmd = Reg(UInt(width = w))
  val addr = Reg(UInt(width = pAddrBits))
  val len = Reg(UInt(width = pAddrBits))
  val body = Reg(Vec(nChunksPerBeat, UInt(width = w)))
  val bodyValid = Reg(UInt(width = nChunksPerBeat))
  val idx = Reg(UInt(width = log2Up(nChunksPerBeat)))
  val interrupt = Reg(init = Bool(false))

  val (cmd_reset :: cmd_interrupt :: cmd_write :: cmd_read :: Nil) = Enum(Bits(), 4)
  val (s_cmd :: s_addr :: s_len :: s_reset ::
       s_write_body :: s_write_data :: s_write_ack ::
       s_read_req  :: s_read_data :: s_read_body :: Nil) = Enum(Bits(), 10)
  val state = Reg(init = s_cmd)

  io.serial.in.ready := state.isOneOf(s_cmd, s_addr, s_len, s_write_body)
  io.serial.out.valid := state === s_read_body
  io.serial.out.bits := body(idx)

  val blockOffset = tlBeatAddrBits + tlByteAddrBits
  val blockAddr = addr(pAddrBits - 1, blockOffset)
  val beatAddr = addr(blockOffset - 1, tlByteAddrBits)
  val wmask = FillInterleaved(w/8, bodyValid)

  val put_acquire = Put(
    client_xact_id = UInt(0),
    addr_block = blockAddr,
    addr_beat = beatAddr,
    data = body.asUInt,
    wmask = Some(wmask))

  val get_acquire = Get(
    client_xact_id = UInt(0),
    addr_block = blockAddr,
    addr_beat = beatAddr)

  io.master.acquire.valid := state.isOneOf(s_write_data, s_read_req)
  io.master.acquire.bits := Mux(state === s_write_data, put_acquire, get_acquire)
  io.master.grant.ready := state.isOneOf(s_write_ack, s_read_data)

  io.interrupt := interrupt
  io.sys_reset := (state === s_reset)

  val slave_req = Queue(io.slave.acquire, 1)
  io.slave.grant.valid := slave_req.valid
  io.slave.grant.bits := Grant(
    is_builtin_type = Bool(true),
    g_type = slave_req.bits.getBuiltInGrantType(),
    client_xact_id = slave_req.bits.client_xact_id,
    manager_xact_id = UInt(0),
    addr_beat = slave_req.bits.client_xact_id,
    data = UInt(0))
  slave_req.ready := io.slave.grant.ready

  def shiftBits(bits: UInt, idx: UInt): UInt =
    bits << Cat(idx, UInt(0, log2Up(w)))

  def addrToIdx(addr: UInt): UInt =
    addr(tlByteAddrBits - 1, log2Up(w/8))

  val nextAddr = Cat(Cat(blockAddr, beatAddr) + UInt(1), UInt(0, tlByteAddrBits))

  when (state === s_cmd && io.serial.in.valid) {
    cmd := io.serial.in.bits
    idx := UInt(0)
    addr := UInt(0)
    len := UInt(0)
    switch (io.serial.in.bits) {
      is (cmd_reset)     { state := s_reset; len := UInt(resetCycles - 1) }
      is (cmd_interrupt) { interrupt := Bool(true) }
      is (cmd_write)     { state := s_addr }
      is (cmd_read)      { state := s_addr }
    }
  }

  when (state === s_addr && io.serial.in.valid) {
    val addrIdx = idx(log2Up(nChunksPerWord) - 1, 0)
    addr := addr | shiftBits(io.serial.in.bits, addrIdx)
    idx := idx + UInt(1)
    when (idx === UInt(nChunksPerWord - 1)) {
      idx := UInt(0)
      state := s_len
    }
  }

  when (state === s_len && io.serial.in.valid) {
    val lenIdx = idx(log2Up(nChunksPerWord) - 1, 0)
    len := len | shiftBits(io.serial.in.bits, lenIdx)
    idx := idx + UInt(1)
    when (idx === UInt(nChunksPerWord - 1)) {
      idx := addrToIdx(addr)
      when (cmd === cmd_write) {
        bodyValid := UInt(0)
        state := s_write_body
      } .otherwise {
        state := s_read_req
      }
    }
  }

  when (state === s_write_body && io.serial.in.valid) {
    body(idx) := io.serial.in.bits
    bodyValid := bodyValid | UIntToOH(idx)
    when (idx === UInt(nChunksPerBeat - 1) || len === UInt(0)) {
      state := s_write_data
    } .otherwise {
      idx := idx + UInt(1)
      len := len - UInt(1)
    }
  }

  when (state === s_write_data && io.master.acquire.ready) {
    state := s_write_ack
  }

  when (state === s_write_ack && io.master.grant.valid) {
    when (len === UInt(0)) {
      state := s_cmd
    } .otherwise {
      addr := nextAddr
      len := len - UInt(1)
      idx := UInt(0)
      bodyValid := UInt(0)
      state := s_write_body
    }
  }

  when (state === s_read_req && io.master.acquire.ready) {
    state := s_read_data
  }

  when (state === s_read_data && io.master.grant.valid) {
    body := body.fromBits(io.master.grant.bits.data)
    idx := addrToIdx(addr)
    addr := nextAddr
    state := s_read_body
  }

  when (state === s_read_body && io.serial.out.ready) {
    idx := idx + UInt(1)
    len := len - UInt(1)
    when (len === UInt(0)) { state := s_cmd }
    .elsewhen (idx === UInt(nChunksPerBeat - 1)) { state := s_read_req }
  }

  when (state === s_reset) {
    len := len - UInt(1)
    when (len === UInt(0)) { state := s_cmd }
  }

  when (slave_req.fire()) { interrupt := Bool(false) }
}

trait PeripheryZynq extends LazyModule {
  implicit val p: Parameters
  val pInterrupts: RangeManager
  val pBusMasters: RangeManager
  val pDevices: ResourceManager[AddrMapEntry]

  pInterrupts.add("zynq", 1)
  pBusMasters.add("zynq", 1)
  pDevices.add(AddrMapEntry("zynq", MemSize(4096, MemAttr(AddrMapProt.RW))))
}

trait PeripheryZynqBundle {
  implicit val p: Parameters

  val serial = new SerialIO(p(SerialInterfaceWidth))
}

trait PeripheryZynqModule {
  implicit val p: Parameters
  val outer: PeripheryZynq
  val io: PeripheryZynqBundle
  val pBus: TileLinkRecursiveInterconnect
  val coreplexIO: BaseCoreplexBundle
  val coreplex: Module
  val reset: Bool

  val (int_idx, _) = outer.pInterrupts.range("zynq")
  val (master_idx, _) = outer.pBusMasters.range("zynq")

  val adapter = Module(new ZynqAdapter()(AdapterParams(p)))
  adapter.io.slave <> pBus.port("zynq")
  coreplexIO.slave(master_idx) <> adapter.io.master
  io.serial <> adapter.io.serial
  coreplexIO.interrupts(int_idx) := adapter.io.interrupt
  coreplex.reset := adapter.io.sys_reset || reset
}

trait NoDebug {
  val coreplexIO: BaseCoreplexBundle
  coreplexIO.debug.req.valid := Bool(false)
  coreplexIO.debug.resp.ready := Bool(false)
}
