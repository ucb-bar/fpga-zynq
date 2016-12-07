package zynq

import Chisel._
import config.Parameters
import diplomacy.LazyModule
import rocketchip.{ExtMem, SimAXIMem}
import junctions.SerialIO
import testchipip.{SimSerialWrapper, SerialInterfaceWidth}

class TestHarness(implicit val p: Parameters) extends Module {
  val io = new Bundle {
    val success = Bool(OUTPUT)
  }

  val dut = Module(LazyModule(new FPGAZynqTop()(p)).module)
  val mem = Module(LazyModule(new SimAXIMem(BigInt(p(ExtMem).size))).module)
  val ser = Module(new SimSerialWrapper(p(SerialInterfaceWidth)))

  mem.io.axi4 <> dut.io.mem_axi4.head
  ser.io.serial <> dut.io.serial
  io.success := ser.io.exit
}

