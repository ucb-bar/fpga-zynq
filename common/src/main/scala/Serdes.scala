package zynq

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import testchipip._

class BlockDeviceSerialIO(w: Int) extends Bundle {
  val req = Decoupled(UInt(w.W))
  val data = Decoupled(UInt(w.W))
  val resp = Flipped(Decoupled(UInt(w.W)))

  override def cloneType = new BlockDeviceSerialIO(w).asInstanceOf[this.type]
}

class BlockDeviceSerdes(w: Int)(implicit p: Parameters)
    extends BlockDeviceModule {
  val io = IO(new Bundle {
    val bdev = Flipped(new BlockDeviceIO)
    val ser = new BlockDeviceSerialIO(w)
  })

  require(w >= sectorBits)
  require(w >= pAddrBits)
  require(w >= (tagBits + 1))
  require(dataBitsPerBeat % w == 0)

  val reqWords = 4
  val dataWords = 1 + dataBitsPerBeat / w

  val req = Reg(new BlockDeviceRequest)
  val data = Reg(new BlockDeviceData)
  val resp = Reg(new BlockDeviceData)

  val (req_idx, req_done) = Counter(io.ser.req.fire(), reqWords)
  val req_send = RegInit(false.B)

  val (data_idx, data_done) = Counter(io.ser.data.fire(), dataWords)
  val data_send = RegInit(false.B)

  val (resp_idx, resp_done) = Counter(io.ser.resp.fire(), dataWords)
  val resp_send = RegInit(false.B)

  when (io.bdev.req.fire()) {
    req := io.bdev.req.bits
    req_send := true.B
  }
  when (req_done) { req_send := false.B }

  when (io.bdev.data.fire()) {
    data := io.bdev.data.bits
    data_send := true.B
  }
  when (data_done) { data_send := false.B }

  when (io.ser.resp.fire()) {
    when (resp_idx === 0.U) {
      resp.tag := io.ser.resp.bits
      resp.data := 0.U
    } .otherwise {
      val shift_amt = (resp_idx - 1.U) << log2Ceil(w).U
      resp.data := resp.data | (io.ser.resp.bits << shift_amt)
    }
  }
  when (resp_done) { resp_send := true.B }
  when (io.bdev.resp.fire()) { resp_send := false.B }

  io.bdev.req.ready := !req_send
  io.bdev.data.ready := !data_send
  io.bdev.resp.valid := resp_send
  io.bdev.resp.bits := resp

  val req_vec = Vec(Cat(req.tag, req.write), req.addr, req.offset, req.len)
  val data_vec = Vec(data.tag +: Seq.tabulate(dataBitsPerBeat/w) {
    i => data.data((i + 1) * w - 1, i * w)
  })

  io.ser.req.valid := req_send
  io.ser.req.bits := req_vec(req_idx)
  io.ser.data.valid := data_send
  io.ser.data.bits := data_vec(data_idx)
  io.ser.resp.ready := !resp_send
}

class BlockDeviceDesser(w: Int)(implicit p: Parameters) extends BlockDeviceModule {
  val io = IO(new Bundle {
    val bdev = new BlockDeviceIO
    val ser = Flipped(new BlockDeviceSerialIO(w))
  })

  require(w >= sectorBits)
  require(w >= pAddrBits)
  require(w >= (tagBits + 1))
  require(dataBitsPerBeat % w == 0)

  val reqWords = 4
  val dataWords = 1 + dataBitsPerBeat / w

  val req = Reg(new BlockDeviceRequest)
  val data = Reg(new BlockDeviceData)
  val resp = Reg(new BlockDeviceData)

  val (req_idx, req_done) = Counter(io.ser.req.fire(), reqWords)
  val req_send = RegInit(false.B)

  val (data_idx, data_done) = Counter(io.ser.data.fire(), dataWords)
  val data_send = RegInit(false.B)

  val (resp_idx, resp_done) = Counter(io.ser.resp.fire(), dataWords)
  val resp_send = RegInit(false.B)

  when (io.ser.req.fire()) {
    switch (req_idx) {
      is (0.U) {
        req.write := io.ser.req.bits(0)
        req.tag := io.ser.req.bits(tagBits, 1)
      }
      is (1.U) {
        req.addr := io.ser.req.bits
      }
      is (2.U) {
        req.offset := io.ser.req.bits
      }
      is (3.U) {
        req.len := io.ser.req.bits
      }
    }
  }
  when (req_done) { req_send := true.B }
  when (io.bdev.req.fire()) {
    req_send := false.B
  }

  when (io.ser.data.fire()) {
    when (data_idx === 0.U) {
      data.tag := io.ser.data.bits
      data.data := 0.U
    } .otherwise {
      val shift_amt = (data_idx - 1.U) << log2Ceil(w).U
      data.data := data.data | (io.ser.data.bits << shift_amt)
    }
  }
  when (data_done) { data_send := true.B }
  when (io.bdev.data.fire()) { data_send := false.B }

  when (io.bdev.resp.fire()) {
    resp := io.bdev.resp.bits
    resp_send := true.B
  }
  when (resp_done) { resp_send := false.B }

  io.bdev.req.valid := req_send
  io.bdev.req.bits := req
  io.bdev.data.valid := data_send
  io.bdev.data.bits := data
  io.bdev.resp.ready := !resp_send

  val resp_vec = Vec(resp.tag +: Seq.tabulate(dataBitsPerBeat/w) {
    i => resp.data((i + 1) * w - 1, i * w)
  })

  io.ser.req.ready := !req_send
  io.ser.data.ready := !data_send
  io.ser.resp.valid := resp_send
  io.ser.resp.bits := resp_vec(resp_idx)
}

class NetworkSerdes(w: Int) extends Module {
  val io = IO(new Bundle {
    val net = Flipped(new StreamIO(64))
    val ser = new SerialIO(w)
  })

  require(64 % w == 0)

  val dataBeats = 64 / w
  val outnet = Reg(new StreamChannel(64))
  val outvec = Vec(Seq.tabulate(dataBeats) { i =>
    outnet.data(w * (i + 1) - 1, w * i)
  } :+ outnet.last)
  val invec = Reg(Vec(dataBeats + 1, UInt(w.W)))

  val out_valid = RegInit(false.B)
  val in_valid = RegInit(false.B)

  val (outcnt, outdone) = Counter(io.ser.out.fire(), dataBeats + 1)
  val (incnt, indone) = Counter(io.ser.in.fire(), dataBeats + 1)

  when (io.net.out.fire()) {
    out_valid := true.B
    outnet := io.net.out.bits
  }
  when (outdone) { out_valid := false.B }
  when (io.ser.in.fire()) { invec(incnt) := io.ser.in.bits }
  when (indone) { in_valid := true.B }
  when (io.net.in.fire()) { in_valid := false.B }

  io.net.out.ready := !out_valid
  io.ser.out.valid := out_valid
  io.ser.out.bits := outvec(outcnt)
  io.ser.in.ready := !in_valid
  io.net.in.valid := in_valid
  io.net.in.bits.data := Cat(invec.take(dataBeats).reverse)
  io.net.in.bits.last := invec(dataBeats)(0)
}

class NetworkDesser(w: Int) extends Module {
  val io = IO(new Bundle {
    val ser = Flipped(new SerialIO(w))
    val net = new StreamIO(64)
  })

  // Yes, the desser is just the reverse of the serdes
  val serdes = Module(new NetworkSerdes(w))
  serdes.io.ser.in <> io.ser.out
  io.ser.in <> serdes.io.ser.out
  io.net.out <> serdes.io.net.in
  serdes.io.net.out <> io.net.in
}
