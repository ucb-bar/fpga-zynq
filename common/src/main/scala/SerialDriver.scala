package zynq

import chisel3._
import chisel3.util._
import junctions._
import junctions.NastiConstants._
import cde.{Parameters, Field}
import _root_.util._
import testchipip._
import rocket.XLen

case object BuildSerialDriver extends Field[Parameters => SerialDriver]

class IntegrationTestDriver(implicit p: Parameters) extends NastiModule()(p) {
  val io = IO(new Bundle {
    val nasti = new NastiIO
    val exit = Output(Bool())
  })

  require(p(XLen) == 64)
  require(p(SerialInterfaceWidth) == 32)
  require(nastiXDataBits == 32)

  val startAddr = 0x80000000L
  val testLen = 0x40
  val readAddr = Reg(UInt(4.W))

  val (cmd_read :: cmd_write :: Nil) = Enum(Bits(), 2)

  val (s_idle :: s_write_addr :: s_write_data :: s_write_resp ::
       s_read_addr :: s_read_data :: s_done :: Nil) = Enum(Bits(), 7)
  val state = Reg(init = s_idle)

  val testData = Vec(Seq.tabulate(testLen)(i => UInt(i * 3)))
  val idx = Reg(UInt(32.W))

  val writeData = MuxCase(UInt(0), Seq(
    (idx === UInt(0)) -> cmd_write,
    (idx === UInt(1)) -> UInt(startAddr),
    (idx === UInt(3)) -> UInt(testLen - 1),
    (idx >= UInt(5) && idx < UInt(5 + testLen)) -> testData(idx - UInt(5)),
    (idx === UInt(5 + testLen)) -> cmd_read,
    (idx === UInt(6 + testLen)) -> UInt(startAddr),
    (idx === UInt(8 + testLen)) -> UInt(testLen - 1)))

  val lastWriteIdx = 9 + testLen

  when (state === s_idle) {
    idx := UInt(0)
    readAddr := UInt(0x0)
    state := s_write_addr
  }

  when (io.nasti.aw.fire()) {
    state := s_write_data
  }

  when (io.nasti.w.fire()) {
    state := s_write_resp
  }

  when (io.nasti.b.fire()) {
    when (idx === UInt(lastWriteIdx)) {
      idx := UInt(0)
      state := s_read_addr
    } .otherwise {
      idx := idx + UInt(1)
      state := s_write_addr
    }
  }

  when (io.nasti.ar.fire()) {
    state := s_read_data
  }

  when (io.nasti.r.fire()) {
    switch (readAddr) {
      is (UInt(0x0)) {
        when (idx === UInt(testLen - 1)) {
          state := s_read_addr
          readAddr := UInt(0x4)
        } .otherwise {
          idx := idx + UInt(1)
          state := s_read_addr
        }
      }
      is (UInt(0x4)) { readAddr := UInt(0xC); state := s_read_addr }
      is (UInt(0xC)) { state := s_done }
    }
  }

  io.exit := (state === s_done)

  io.nasti.aw.valid := (state === s_write_addr)
  io.nasti.aw.bits := NastiWriteAddressChannel(
    id = UInt(0),
    addr = UInt(0x43C00008L),
    size = UInt(2))

  io.nasti.w.valid := (state === s_write_data)
  io.nasti.w.bits := NastiWriteDataChannel(data = writeData)

  io.nasti.ar.valid := (state === s_read_addr)
  io.nasti.ar.bits := NastiReadAddressChannel(
    id = UInt(0),
    addr = UInt(0x43C00000L) | readAddr,
    size = UInt(2))

  io.nasti.b.ready := (state === s_write_resp)
  io.nasti.r.ready := (state === s_read_data)

  val expectedData = MuxLookup(readAddr, UInt(0), Seq(
    UInt(0xC) -> UInt(p(SerialFIFODepth)),
    UInt(0x4) -> UInt(0),
    UInt(0x0) -> testData(idx)))

  assert(!io.nasti.b.valid || io.nasti.b.bits.resp === RESP_OKAY,
         "Integration test write error")
  assert(!io.nasti.r.valid || io.nasti.r.bits.data === expectedData,
         "Integration test data mismatch")
}

class IntegrationTestReset(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val nasti = new NastiIO
  })

  val (s_idle :: s_write_addr :: s_write_data :: s_done :: Nil) = Enum(Bits(), 4)
  val state = Reg(init = s_idle)

  when (state === s_idle) { state := s_write_addr }
  when (io.nasti.aw.fire()) { state := s_write_data }
  when (io.nasti.w.fire()) { state := s_done }

  io.nasti.aw.valid := state === s_write_addr
  io.nasti.aw.bits := NastiWriteAddressChannel(
    id = UInt(0),
    addr = UInt(0x43C00010L),
    size = UInt(2))

  io.nasti.w.valid := state === s_write_data
  io.nasti.w.bits := NastiWriteDataChannel(data = UInt(0))

  io.nasti.b.ready := (state === s_done)
  io.nasti.ar.valid := Bool(false)
  io.nasti.r.ready := Bool(false)
}

class IntegrationTestSerial(implicit p: Parameters) extends SerialDriver(p(SerialInterfaceWidth)) {
  val testParams = AdapterParams(p)
  val slave = Module(new ZynqAXISlave(2)(testParams))
  val driver = Module(new IntegrationTestDriver()(testParams))
  val resetter = Module(new IntegrationTestReset()(testParams))

  io.exit := driver.io.exit
  slave.io.nasti(0) <> driver.io.nasti
  slave.io.nasti(1) <> resetter.io.nasti
  slave.io.serial <> io.serial
  driver.reset := slave.io.sys_reset
}
