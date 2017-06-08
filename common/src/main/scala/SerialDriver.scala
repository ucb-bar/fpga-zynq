package zynq

import chisel3._
import chisel3.util._
import junctions._
import junctions.NastiConstants._
import config.{Parameters, Field}
import _root_.util._
import testchipip._
import tile.XLen

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

  val (cmd_read :: cmd_write :: Nil) = Enum(2)

  val (s_idle :: s_write_addr :: s_write_data :: s_write_resp ::
       s_read_addr :: s_read_data :: s_done :: Nil) = Enum(7)
  val state = RegInit(s_idle)

  val testData = Vec(Seq.tabulate(testLen)(i => (i * 3).U))
  val idx = Reg(UInt(32.W))

  val writeData = MuxCase(0.U, Seq(
    (idx === 0.U) -> cmd_write,
    (idx === 1.U) -> startAddr.U,
    (idx === 3.U) -> (testLen - 1).U,
    (idx >= 5.U && idx < (5 + testLen).U) -> testData(idx - 5.U),
    (idx === (5 + testLen).U) -> cmd_read,
    (idx === (6 + testLen).U) -> startAddr.U,
    (idx === (8 + testLen).U) -> (testLen - 1).U))

  val lastWriteIdx = 9 + testLen

  when (state === s_idle) {
    idx := 0.U
    readAddr := 0x0.U
    state := s_write_addr
  }

  when (io.nasti.aw.fire()) {
    state := s_write_data
  }

  when (io.nasti.w.fire()) {
    state := s_write_resp
  }

  when (io.nasti.b.fire()) {
    when (idx === lastWriteIdx.U) {
      idx := 0.U
      state := s_read_addr
    } .otherwise {
      idx := idx + 1.U
      state := s_write_addr
    }
  }

  when (io.nasti.ar.fire()) {
    state := s_read_data
  }

  when (io.nasti.r.fire()) {
    switch (readAddr) {
      is (0x0.U) {
        when (idx === (testLen - 1).U) {
          state := s_read_addr
          readAddr := 0x4.U
        } .otherwise {
          idx := idx + 1.U
          state := s_read_addr
        }
      }
      is (0x4.U) { readAddr := 0xC.U; state := s_read_addr }
      is (0xC.U) { state := s_done }
    }
  }

  io.exit := (state === s_done)

  io.nasti.aw.valid := (state === s_write_addr)
  io.nasti.aw.bits := NastiWriteAddressChannel(
    id = 0.U,
    addr = 0x43C00008L.U,
    size = 2.U)

  io.nasti.w.valid := (state === s_write_data)
  io.nasti.w.bits := NastiWriteDataChannel(data = writeData)

  io.nasti.ar.valid := (state === s_read_addr)
  io.nasti.ar.bits := NastiReadAddressChannel(
    id = 0.U,
    addr = 0x43C00000L.U | readAddr,
    size = 2.U)

  io.nasti.b.ready := (state === s_write_resp)
  io.nasti.r.ready := (state === s_read_data)

  val expectedData = MuxLookup(readAddr, 0.U, Seq(
    0xC.U -> p(SerialFIFODepth).U,
    0x4.U -> 0.U,
    0x0.U -> testData(idx)))

  assert(!io.nasti.b.valid || io.nasti.b.bits.resp === RESP_OKAY,
         "Integration test write error")
  assert(!io.nasti.r.valid || io.nasti.r.bits.data === expectedData,
         "Integration test data mismatch")
}

class IntegrationTestReset(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val nasti = new NastiIO
  })

  val (s_idle :: s_write_addr :: s_write_data :: s_done :: Nil) = Enum(4)
  val state = RegInit(s_idle)

  when (state === s_idle) { state := s_write_addr }
  when (io.nasti.aw.fire()) { state := s_write_data }
  when (io.nasti.w.fire()) { state := s_done }

  io.nasti.aw.valid := state === s_write_addr
  io.nasti.aw.bits := NastiWriteAddressChannel(
    id = 0.U,
    addr = 0x43C00010L.U,
    size = 2.U)

  io.nasti.w.valid := state === s_write_data
  io.nasti.w.bits := NastiWriteDataChannel(data = 0.U)

  io.nasti.b.ready := (state === s_done)
  io.nasti.ar.valid := false.B
  io.nasti.r.ready := false.B
}

class IntegrationTestSerial(implicit p: Parameters) extends SerialDriver(p(SerialInterfaceWidth)) {
  val testParams = AdapterParams(p)
  val slave = Module(new ZynqAXISlave(2)(testParams))
  val driver = Module(new IntegrationTestDriver()(testParams))
  val resetter = Module(new IntegrationTestReset()(testParams))

  io.exit := driver.io.exit
  slave.io.axi(0) <> driver.io.nasti
  slave.io.axi(1) <> resetter.io.nasti
  slave.io.serial <> io.serial
  driver.reset := slave.io.sys_reset
}
