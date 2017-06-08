package zynq

import chisel3._
import config.Parameters
import tile.{HasCoreIO, CoreModule}

class DummyCore(implicit p: Parameters) extends CoreModule with HasCoreIO {
  io.imem.req.valid := false.B
  io.imem.sfence.valid := false.B
  io.imem.btb_update.valid := false.B
  io.imem.bht_update.valid := false.B

  io.fpu.valid := false.B
  io.fpu.dmem_resp_val := false.B

  io.dmem.req.valid := false.B
  io.dmem.invalidate_lr := false.B
  io.dmem.s1_kill := false.B

  io.rocc.cmd.valid := false.B
  io.ptw.invalidate := false.B
}
