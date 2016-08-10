
package zynq

import scala.math.min
import Chisel._
import uncore.tilelink._
import junctions._
import cde.Parameters
import uncore.devices.{DebugBusIO, DebugBusReq, DebugBusResp, DMKey}
import uncore.devices.DbBusConsts._

/* TODO: This module should be instantiated by a top-level project that also
 * instantiates rocket-chip. 
 *  val adapterParams = p.alterPartial({
 *    case NastiKey => NastiParameters(
 *      dataBits = 32,
 *      addrBits = 32,
 *      idBits = 12)
 *  })
 *
 */

class ZynqAdapter(implicit val p: Parameters)
    extends Module with HasNastiParameters {
  val io = new Bundle {
    val nasti = (new NastiIO).flip
    val reset = Bool(OUTPUT)
    val debug = new DebugBusIO
  }
  require(nastiXDataBits == 32)

  val aw = io.nasti.aw
  val ar = io.nasti.ar
  val w = io.nasti.w
  val r = io.nasti.r
  val b = io.nasti.b

  // Writing to 0x0, simply updates the contents of the register without
  // validating the request
  // Writing to 0x8, sets the valid register without changing the payload
  // Write-Only
  val REQ_PAYLOAD_ADDR = 0x0
  val REQ_VALID_ADDR = 0x8
  // Read-Only
  val RESP_ADDR = 0x10
  val RESET_ADDR = 0x20

  val debugAddrSize = p(DMKey).nDebugBusAddrSize
  val reqOffset = dbDataSize
  val opOffset = dbDataSize
  val addrOffset = dbDataSize + dbOpSize


  val reqReg = RegInit({
    val init = Wire(Valid(new DebugBusReq(debugAddrSize)))
    init.valid := Bool(false)
    init
  })

  val respReg = RegInit({
    val init = Wire(Valid(new DebugBusResp))
    init.valid := Bool(false)
    init
  })

  val awReady = RegEnable(Bool(false), Bool(true), aw.fire())
  val wReady = RegEnable(Bool(false), Bool(true), w.fire() && w.bits.last)
  val arReady = RegInit(Bool(false))
  val rValid = RegInit(Bool(false))
  val bValid = RegInit(Bool(false))
  val bId = RegEnable(aw.bits.id, aw.fire())
  val rId = RegEnable(ar.bits.id, ar.fire())
  val wData = RegEnable(w.bits.data, w.fire() && w.bits.last)
  val wAddr = RegEnable(aw.bits.addr(5,0), aw.fire())
  val rAddr = RegEnable(ar.bits.addr(5,0), ar.fire())
  val resetReg = RegInit(Bool(false))

  val rData = Mux(rAddr(2), respReg.bits.toBits()(respReg.bits.getWidth-1,32),
                  respReg.bits.toBits()(31,0))

  val reqL = Mux(~wAddr(2),
                 wData,
                 Cat(reqReg.bits.op, reqReg.bits.addr, reqReg.bits.data)(31,0))
  val reqH = Mux(wAddr(2),
                 wData,
                 Cat(reqReg.bits.op,
                     reqReg.bits.addr,
                     reqReg.bits.data)(reqReg.bits.getWidth-1,32))

  io.reset := Bool(false)

  when ((aw.fire() || ~aw.ready) && ((w.fire() && w.bits.last) || ~w.ready)){
    bValid := Bool(true)
  }

  when(b.fire()){
    when((wAddr) === UInt(REQ_VALID_ADDR)){
      reqReg.valid := Bool(true)
      respReg.valid := Bool(false)
    }.elsewhen((wAddr) === UInt(RESET_ADDR)){
      resetReg := Bool(true)
    }.otherwise{
      reqReg.bits := reqReg.bits.fromBits(Cat(reqH, reqL))
    }
    awReady := Bool(true)
    wReady := Bool(true)
    bValid := Bool(false)
  }

  when(ar.fire()){
    rValid := Bool(true)
  }

  when(r.fire() && r.bits.last){
    rValid := Bool(false)
  }

  when(io.debug.req.fire()){
    reqReg.valid := Bool(false)
  }

  when(io.debug.resp.fire()){
    respReg.valid := Bool(true)
    respReg.bits := io.debug.resp.bits
  }

  when(resetReg) {
    resetReg := Bool(false)
  }

  io.reset := resetReg

  ar.ready := respReg.valid
  aw.ready := awReady
  w.ready := wReady
  r.valid := rValid
  r.bits := NastiReadDataChannel(rId, rData)
  b.valid := bValid
  b.bits := NastiWriteResponseChannel(bId)

  io.debug.resp.ready := ~respReg.valid
  io.debug.req.valid := reqReg.valid
  io.debug.req.bits := reqReg.bits

  assert(!w.valid || w.bits.strb.andR,
    "Nasti to DebugBusIO converter cannot take partial writes")
  assert(!ar.valid ||
    ar.bits.len === UInt(0) ||
    ar.bits.burst === NastiConstants.BURST_FIXED,
    "Nasti to DebugBusIO converter can only take fixed bursts")
  assert(!aw.valid ||
    aw.bits.len === UInt(0) ||
    aw.bits.burst === NastiConstants.BURST_FIXED,
    "Nasti to DebugBusIO converter can only take fixed bursts")
}

