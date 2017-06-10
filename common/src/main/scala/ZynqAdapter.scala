package zynq

import chisel3._
import chisel3.util._
import junctions._
import junctions.NastiConstants._
import config.{Parameters, Field}
import testchipip._
import _root_.util.UIntIsOneOf

case object SerialFIFODepth extends Field[Int]
case object BlockDeviceFIFODepth extends Field[Int]
case object ResetCycles extends Field[Int]

class ZynqAdapter(nPorts: Int)(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val axi = Flipped(Vec(nPorts, new NastiIO()))
    val sys_reset = Output(Bool())
    val serial = Flipped(new SerialIO(p(SerialInterfaceWidth)))
    val bdev = Flipped(new BlockDeviceIO)
  })

  def routeSel(addr: UInt): UInt = {
    // 0x00 - 0x0F go to FIFO
    // 0x10 - 0x1F go to Reset generator
    // 0x20 - 0x3F go to BlockDevice
    Cat(addr(5), addr(5, 4) === 1.U, addr(5, 4) === 0.U)
  }

  val xbar = Module(new NastiCrossbar(nPorts, 3, routeSel _))
  val serial = Module(new SerialFIFO)
  val resetter = Module(new ResetController)
  val blkdev = Module(new BlockDeviceFIFO)

  xbar.io.masters <> io.axi
  serial.io.axi <> xbar.io.slaves(0)
  serial.io.serial <> io.serial
  resetter.io.axi <> xbar.io.slaves(1)
  io.sys_reset := resetter.io.sys_reset
  blkdev.io.bdev <> io.bdev
  blkdev.io.axi <> xbar.io.slaves(2)
}

class ResetController(implicit p: Parameters) extends NastiModule()(p) {
  val io = IO(new Bundle {
    val axi = Flipped(new NastiIO())
    val sys_reset = Output(Bool())
  })

  val reg_reset = RegInit(true.B)

  val readId = Reg(UInt(nastiXIdBits.W))

  val r_addr :: r_data :: Nil = Enum(2)
  val r_state = RegInit(r_addr)

  io.axi.ar.ready := r_state === r_addr
  io.axi.r.valid := r_state === r_data
  io.axi.r.bits := NastiReadDataChannel(
    id = readId,
    data = reg_reset)

  when (io.axi.ar.fire()) {
    readId := io.axi.ar.bits.id
    r_state := r_data
  }

  when (io.axi.r.fire()) {
    r_state := r_addr
  }

  val writeId = Reg(UInt(nastiXIdBits.W))

  val w_addr :: w_data :: w_resp :: Nil = Enum(3)
  val w_state = RegInit(w_addr)
  val timer = RegInit((p(ResetCycles) - 1).U)

  // Make sure reset period lasts for a certain number of cycles
  when (timer =/= 0.U) { timer := timer - 1.U }

  when (io.axi.aw.fire()) {
    writeId := io.axi.aw.bits.id
    w_state := w_data
  }

  when (io.axi.w.fire()) {
    timer := (p(ResetCycles) - 1).U
    reg_reset := io.axi.w.bits.data(0)
    w_state := w_resp
  }

  when (io.axi.b.fire()) {
    w_state := w_addr
  }

  io.axi.aw.ready := w_state === w_addr
  io.axi.w.ready := w_state === w_data
  io.axi.b.valid := w_state === w_resp && timer === 0.U
  io.axi.b.bits := NastiWriteResponseChannel(id = writeId)

  io.sys_reset := reg_reset
}

class SerialFIFO(implicit p: Parameters) extends NastiModule()(p) {
  val w = p(SerialInterfaceWidth)
  val depth = p(SerialFIFODepth)

  val io = IO(new Bundle {
    val axi = Flipped(new NastiIO())
    val serial = Flipped(new SerialIO(w))
  })

  require(nastiXDataBits == 32)
  require(nastiXDataBits == w)

  val outq = Module(new Queue(UInt(w.W), depth))
  val inq  = Module(new Queue(UInt(w.W), depth))
  val writing = RegInit(false.B)
  val reading = RegInit(false.B)
  val responding = RegInit(false.B)
  val len = Reg(UInt(nastiXLenBits.W))
  val bid = Reg(UInt(nastiXIdBits.W))
  val rid = Reg(UInt(nastiXIdBits.W))

  io.serial.in <> inq.io.deq
  outq.io.enq <> io.serial.out

  val nRegisters = 3
  val addrLSB = log2Ceil(w / 8)
  val addrMSB = addrLSB + log2Ceil(nRegisters) - 1
  val araddr = io.axi.ar.bits.addr(addrMSB, addrLSB)
  val awaddr = io.axi.aw.bits.addr(addrMSB, addrLSB)
  val raddr = Reg(araddr.cloneType)
  val waddr = Reg(awaddr.cloneType)

  inq.io.enq.valid := io.axi.w.valid && writing && (waddr === 2.U)
  io.axi.w.ready := (inq.io.enq.ready || waddr =/= 2.U) && writing
  inq.io.enq.bits  := io.axi.w.bits.data

  /**
   * Address Map
   * 0x00 - out FIFO data
   * 0x04 - out FIFO data available (words)
   * 0x08 - in  FIFO data
   * 0x0C - in  FIFO space available (words)
   */
  io.axi.r.valid := reading && (raddr =/= 0.U || outq.io.deq.valid)
  outq.io.deq.ready := reading && raddr === 0.U && io.axi.r.ready
  io.axi.r.bits := NastiReadDataChannel(
    id = rid,
    data = MuxLookup(raddr, 0.U, Seq(
      0.U -> outq.io.deq.bits,
      1.U -> outq.io.count,
      3.U -> (depth.U - inq.io.count))),
    last = len === 0.U)

  io.axi.aw.ready := !writing && !responding
  io.axi.ar.ready := !reading
  io.axi.b.valid := responding
  io.axi.b.bits := NastiWriteResponseChannel(
    id = bid,
    // writing to anything other that the in FIFO is an error
    resp = Mux(waddr === 2.U, RESP_OKAY, RESP_SLVERR))

  when (io.axi.aw.fire()) {
    writing := true.B
    waddr := awaddr
    bid := io.axi.aw.bits.id
  }
  when (io.axi.w.fire() && io.axi.w.bits.last) {
    writing := false.B
    responding := true.B
  }
  when (io.axi.b.fire()) { responding := false.B }
  when (io.axi.ar.fire()) {
    len := io.axi.ar.bits.len
    rid := io.axi.ar.bits.id
    raddr := araddr
    reading := true.B
  }
  when (io.axi.r.fire()) {
    len := len - 1.U
    when (len === 0.U) { reading := false.B }
  }

  def addressOK(chan: NastiAddressChannel): Bool =
    (chan.len === 0.U || chan.burst === BURST_FIXED) &&
    chan.size === log2Ceil(w/8).U &&
    chan.addr(log2Ceil(nastiWStrobeBits)-1, 0) === 0.U

  def dataOK(chan: NastiWriteDataChannel): Bool =
    chan.strb(w/8-1, 0).andR

  assert(!io.axi.aw.valid || addressOK(io.axi.aw.bits),
    s"SerialFIFO aw can only accept aligned fixed bursts of size $w")

  assert(!io.axi.ar.valid || addressOK(io.axi.ar.bits),
    s"SerialFIFO ar can only accept aligned fixed bursts of size $w")

  assert(!io.axi.w.valid || dataOK(io.axi.w.bits),
    s"SerialFIFO w cannot accept partial writes")
}

class BlockDeviceFIFO(implicit p: Parameters) extends NastiModule {
  val io = IO(new Bundle {
    val bdev = Flipped(new BlockDeviceIO)
    val axi = Flipped(new NastiIO)
  })

  val w = nastiXDataBits
  val reqFIFO  = Module(new BlockDeviceRequestFIFO)
  val dataFIFO = Module(new BlockDeviceRequestFIFO)
  val respFIFO = Module(new BlockDeviceResponseFIFO)
  val infoReg = Module(new BlockDeviceInfoRegister)
  val serdes = Module(new BlockDeviceSerdes(w))
  serdes.io.bdev <> io.bdev
  reqFIFO.io.req <> serdes.io.ser.req
  dataFIFO.io.req <> serdes.io.ser.data
  serdes.io.ser.resp <> respFIFO.io.resp
  io.bdev.info := infoReg.io.info

  /**
   * Address Map
   * 0x00 - req FIFO data
   * 0x04 - req FIFO data available (words)
   * 0x08 - data FIFO data
   * 0x0C - data FIFO data available (words)
   * 0x10 - resp FIFO data
   * 0x14 - resp FIFO space available (words)
   * 0x18 - nsectors
   */
  def routeSel(addr: UInt): UInt = UIntToOH(addr(4, 3))

  val router = Module(new NastiRouter(4, routeSel _))
  router.io.master <> io.axi
  reqFIFO.io.axi  <> router.io.slave(0)
  dataFIFO.io.axi <> router.io.slave(1)
  respFIFO.io.axi <> router.io.slave(2)
  infoReg.io.axi  <> router.io.slave(3)

  def addressOK(chan: NastiAddressChannel): Bool =
    (chan.len === 0.U || chan.burst === BURST_FIXED) &&
    chan.size === log2Ceil(w/8).U &&
    chan.addr(log2Ceil(w/8)-1, 0) === 0.U

  assert(!io.axi.aw.valid || addressOK(io.axi.aw.bits),
    s"BlockDeviceFIFO aw can only accept aligned fixed bursts of size $w")

  assert(!io.axi.ar.valid || addressOK(io.axi.ar.bits),
    s"BlockDeviceFIFO ar can only accept aligned fixed bursts of size $w")

  assert(!io.axi.w.valid || io.axi.w.bits.strb.andR,
    s"BlockDeviceFIFO w cannot accept partial writes")
}

class BlockDeviceRequestFIFO(implicit p: Parameters) extends NastiModule {
  val w = nastiXDataBits
  val depth = p(BlockDeviceFIFODepth)

  val io = IO(new Bundle {
    val axi = Flipped(new NastiIO)
    val req = Flipped(Decoupled(UInt(w.W)))
  })

  val reqq = Module(new Queue(UInt(w.W), depth))
  val len = Reg(UInt(nastiXAddrBits.W))
  val reading = RegInit(false.B)
  val writing = RegInit(false.B)
  val responding = RegInit(false.B)

  val addrLSB = log2Ceil(w / 8)
  val araddr = io.axi.ar.bits.addr(addrLSB)
  val raddr = RegEnable(araddr, io.axi.ar.fire())
  val rid = RegEnable(io.axi.ar.bits.id, io.axi.ar.fire())
  val bid = RegEnable(io.axi.aw.bits.id, io.axi.aw.fire())

  when (io.axi.ar.fire()) {
    len := io.axi.ar.bits.len
    reading := true.B
  }
  when (io.axi.r.fire()) {
    len := len - 1.U
    when (len === 0.U) { reading := false.B }
  }
  when (io.axi.aw.fire()) {
    writing := true.B
  }
  when (io.axi.w.fire() && io.axi.w.bits.last) {
    writing := false.B
    responding := true.B
  }
  when (io.axi.b.fire()) {
    responding := false.B
  }

  io.axi.ar.ready := !reading
  io.axi.r.valid := reading && (raddr =/= 0.U || reqq.io.deq.valid)
  io.axi.r.bits := NastiReadDataChannel(
    id = rid,
    data = Mux(raddr === 0.U, reqq.io.deq.bits, reqq.io.count),
    last = len === 0.U)
  io.axi.aw.ready := !writing && !responding
  io.axi.w.ready := writing
  io.axi.b.valid := responding
  io.axi.b.bits := NastiWriteResponseChannel(id = bid, resp = RESP_SLVERR)
  reqq.io.deq.ready := reading && araddr === 0.U && io.axi.r.ready
  reqq.io.enq <> io.req
}

class BlockDeviceResponseFIFO(implicit p: Parameters) extends NastiModule {
  val w = nastiXDataBits
  val depth = p(BlockDeviceFIFODepth)

  val io = IO(new Bundle {
    val axi = Flipped(new NastiIO)
    val resp = Decoupled(UInt(w.W))
  })

  val respq = Module(new Queue(UInt(w.W), depth))
  val reading = RegInit(false.B)
  val writing = RegInit(false.B)
  val responding = RegInit(false.B)

  val addrLSB = log2Ceil(w / 8)
  val araddr = io.axi.ar.bits.addr(addrLSB)
  val awaddr = io.axi.aw.bits.addr(addrLSB)
  val raddr = RegEnable(araddr, io.axi.ar.fire())
  val waddr = RegEnable(awaddr, io.axi.aw.fire())
  val rid = RegEnable(io.axi.ar.bits.id, io.axi.ar.fire())
  val bid = RegEnable(io.axi.aw.bits.id, io.axi.aw.fire())
  val len = Reg(UInt(nastiXLenBits.W))

  when (io.axi.ar.fire()) {
    len := io.axi.ar.bits.len
    reading := true.B
  }
  when (io.axi.r.fire()) {
    len := len - 1.U
    when (len === 0.U) { reading := false.B }
  }
  when (io.axi.aw.fire()) {
    writing := true.B
  }
  when (io.axi.w.fire() && io.axi.w.bits.last) {
    writing := false.B
    responding := true.B
  }
  when (io.axi.b.fire()) {
    responding := false.B
  }

  io.axi.ar.ready := !reading
  io.axi.r.valid := reading
  io.axi.r.bits := NastiReadDataChannel(
    id = rid,
    resp = Mux(raddr === 1.U, RESP_OKAY, RESP_SLVERR),
    data = (depth.U - respq.io.count),
    last = len === 0.U)
  io.axi.aw.ready := !writing && !responding
  io.axi.w.ready := writing && (waddr =/= 0.U || respq.io.enq.ready)
  io.axi.b.valid := responding
  io.axi.b.bits := NastiWriteResponseChannel(
    id = bid,
    resp = Mux(waddr === 0.U, RESP_OKAY, RESP_SLVERR))
  respq.io.enq.valid := writing && waddr === 0.U && io.axi.w.valid
  respq.io.enq.bits := io.axi.w.bits.data
  io.resp <> respq.io.deq
}

class BlockDeviceInfoRegister(implicit p: Parameters) extends NastiModule {
  val io = IO(new Bundle {
    val axi = Flipped(new NastiIO)
    val info = Output(new BlockDeviceInfo)
  })

  val info = Reg(new BlockDeviceInfo)
  val reading = RegInit(false.B)
  val writing = RegInit(false.B)
  val responding = RegInit(false.B)

  val rid = RegEnable(io.axi.ar.bits.id, io.axi.ar.fire())
  val bid = RegEnable(io.axi.aw.bits.id, io.axi.aw.fire())
  val len = Reg(UInt(nastiXLenBits.W))

  when (io.axi.ar.fire()) {
    len := io.axi.ar.bits.len
    reading := true.B
  }
  when (io.axi.r.fire()) {
    len := len - 1.U
    when (len === 0.U) { reading := false.B }
  }
  when (io.axi.aw.fire()) {
    writing := true.B
  }
  when (io.axi.w.fire()) {
    info.nsectors := io.axi.w.bits.data
    when (io.axi.w.bits.last) {
      writing := false.B
      responding := true.B
    }
  }
  when (io.axi.b.fire()) {
    responding := false.B
  }

  io.axi.ar.ready := !reading
  io.axi.r.valid := reading
  io.axi.r.bits := NastiReadDataChannel(
    id = rid,
    data = info.nsectors,
    last = len === 0.U)
  io.axi.aw.ready := !writing && !responding
  io.axi.w.ready := writing
  io.axi.b.valid := responding
  io.axi.b.bits := NastiWriteResponseChannel(id = bid)
  io.info := info
}
