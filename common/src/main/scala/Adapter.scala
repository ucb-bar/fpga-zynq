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
import rocket.XLen
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

class SerialAdapter(implicit p: Parameters) extends TLModule()(p) {
  val w = p(SerialInterfaceWidth)
  val io = new Bundle {
    val serial = new SerialIO(w)
    val mem = new ClientUncachedTileLinkIO
  }

  val nChunksPerBeat = tlDataBits / w
  val pAddrBits = p(PAddrBits)
  val xLen = p(XLen)
  val nChunksPerWord = xLen / w

  require(nChunksPerBeat > 0, s"Serial interface width must be <= TileLink width $tlDataBits")
  require(nChunksPerWord > 0, s"Serial interface width must be <= PAddrBits $pAddrBits")

  val cmd = Reg(UInt(width = w))
  val addr = Reg(UInt(width = xLen))
  val len = Reg(UInt(width = xLen))
  val body = Reg(Vec(nChunksPerBeat, UInt(width = w)))
  val bodyValid = Reg(UInt(width = nChunksPerBeat))
  val idx = Reg(UInt(width = log2Up(nChunksPerBeat)))

  val (cmd_read :: cmd_write :: Nil) = Enum(Bits(), 2)
  val (s_cmd :: s_addr :: s_len ::
       s_read_req  :: s_read_data :: s_read_body :: 
       s_write_body :: s_write_data :: s_write_ack :: Nil) = Enum(Bits(), 9)
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

  io.mem.acquire.valid := state.isOneOf(s_write_data, s_read_req)
  io.mem.acquire.bits := Mux(state === s_write_data, put_acquire, get_acquire)
  io.mem.grant.ready := state.isOneOf(s_write_ack, s_read_data)

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
    state := s_addr
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

  when (state === s_read_req && io.mem.acquire.ready) {
    state := s_read_data
  }

  when (state === s_read_data && io.mem.grant.valid) {
    body := body.fromBits(io.mem.grant.bits.data)
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

  when (state === s_write_data && io.mem.acquire.ready) {
    state := s_write_ack
  }

  when (state === s_write_ack && io.mem.grant.valid) {
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
}

trait PeripherySerial extends LazyModule {
  implicit val p: Parameters
  val pInterrupts: RangeManager
  val pBusMasters: RangeManager
  val pDevices: ResourceManager[AddrMapEntry]

  pBusMasters.add("serial", 1)
}

trait PeripherySerialBundle {
  implicit val p: Parameters

  val serial = new SerialIO(p(SerialInterfaceWidth))
}

trait PeripherySerialModule {
  implicit val p: Parameters
  val outer: PeripherySerial
  val io: PeripherySerialBundle
  val pBus: TileLinkRecursiveInterconnect
  val coreplexIO: BaseCoreplexBundle
  val coreplex: Module

  val (master_idx, _) = outer.pBusMasters.range("serial")

  val adapter = Module(new SerialAdapter()(AdapterParams(p)))
  coreplexIO.slave(master_idx) <> adapter.io.mem
  io.serial.out <> Queue(adapter.io.serial.out)
  adapter.io.serial.in <> Queue(io.serial.in)
}
