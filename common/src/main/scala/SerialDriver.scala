package zynq

import Chisel._
import junctions.{SerialIO, PAddrBits}
import cde.{Parameters, Field}
import util._
import testchipip._

case object BuildSerialDriver extends Field[Parameters => SerialDriver]

abstract class SerialDriver(w: Int) extends Module {
  val io = new Bundle {
    val serial = new SerialIO(w).flip
    val exit = Bool(OUTPUT)
  }
}

class SimSerial(w: Int) extends BlackBox {
  val io = new Bundle {
    val clock = Clock(INPUT)
    val reset = Bool(INPUT)
    val serial = new SerialIO(w).flip
    val exit = Bool(OUTPUT)
  }
}

class SimSerialWrapper(w: Int) extends SerialDriver(w) {
  val bbox = Module(new SimSerial(w))
  bbox.io.clock := clock
  bbox.io.reset := reset
  bbox.io.serial <> io.serial
  io.exit := bbox.io.exit
}

class IntegrationTestSerial(implicit p: Parameters) extends SerialDriver(p(SerialInterfaceWidth)) {
  val (cmd_reset :: cmd_interrupt :: cmd_write :: cmd_read :: Nil) = Enum(Bits(), 4)

  require(p(PAddrBits) == 32)
  require(p(SerialInterfaceWidth) == 32)

  val (s_init :: s_reset_cmd ::
       s_write_cmd :: s_write_addr :: s_write_len :: s_write_data ::
       s_read_cmd :: s_read_addr :: s_read_len :: s_read_data ::
       s_finished :: Nil) = Enum(Bits(), 11)

  val state = Reg(init = s_init)
  val startAddr = 0x80000000L
  val testLen = 0x100
  val testData = Vec(Seq.tabulate(testLen)(i => UInt(i * 3)))

  val idx = Reg(UInt(width = log2Up(testLen)))

  when (state === s_init) { state := s_reset_cmd }
  when (io.serial.in.fire()) {
    switch (state) {
      is (s_reset_cmd)  { state := s_write_cmd }
      is (s_write_cmd)  { state := s_write_addr }
      is (s_write_addr) { state := s_write_len }
      is (s_write_len)  { state := s_write_data; idx := UInt(0) }
      is (s_write_data) {
        idx := idx + UInt(1)
        when (idx === UInt(testLen - 1)) { state := s_read_cmd }
      }
      is (s_read_cmd)   { state := s_read_addr }
      is (s_read_addr)  { state := s_read_len }
      is (s_read_len)   { state := s_read_data; idx := UInt(0) }
    }
  }
  when (io.serial.out.fire()) {
    idx := idx + UInt(1)
    when (idx === UInt(testLen - 1)) { state := s_finished }
  }

  io.exit := (state === s_finished)
  io.serial.in.valid := state.isOneOf(
    s_reset_cmd,
    s_write_cmd, s_write_addr, s_write_len, s_write_data,
    s_read_cmd, s_read_addr, s_read_len)
  io.serial.in.bits := MuxLookup(state, UInt(0), Seq(
    s_reset_cmd  -> cmd_reset,
    s_write_cmd  -> cmd_write,
    s_write_addr -> UInt(startAddr),
    s_write_len  -> UInt(testLen - 1),
    s_write_data -> testData(idx),
    s_read_cmd   -> cmd_read,
    s_read_addr  -> UInt(startAddr),
    s_read_len   -> UInt(testLen - 1)))
  io.serial.out.ready := state === s_read_data

  assert(state =/= s_read_data || !io.serial.out.valid ||
         io.serial.out.bits === testData(idx),
         "Read Data does not match")
}
