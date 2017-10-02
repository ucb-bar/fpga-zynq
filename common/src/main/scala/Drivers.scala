package zynq

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import testchipip._
import testchipip.SerialAdapter._
import icenet._
import icenet.IceNetConsts._

class InFIFODriver(name: String, addr: BigInt, maxSpace: Int)
    (implicit p: Parameters) extends LazyModule {

  val node = TLHelper.makeClientNode(
    name = name, sourceId = IdRange(0, 1))

  lazy val module = new LazyModuleImp(this) {
    val (tl, edge) = node.out(0)
    val dataBits = edge.bundle.dataBits
    val beatBytes = dataBits / 8

    val io = IO(new Bundle {
      val in = Flipped(Decoupled(UInt(dataBits.W)))
    })

    val timeout = 64
    val timer = RegInit(0.U(log2Ceil(timeout).W))
    val space = RegInit(0.U(log2Ceil(maxSpace + 1).W))

    val (s_start :: s_read_acq :: s_read_gnt ::
         s_req :: s_write_acq :: s_write_gnt :: Nil) = Enum(6)
    val state = RegInit(s_start)
    val data = Reg(UInt(dataBits.W))

    val put_acq = edge.Put(
      fromSource = 0.U,
      toAddress = addr.U,
      lgSize = log2Ceil(beatBytes).U,
      data = data)._2

    val get_acq = edge.Get(
      fromSource = 0.U,
      toAddress = (addr + beatBytes).U,
      lgSize = log2Ceil(beatBytes).U)._2

    tl.a.valid := state.isOneOf(s_read_acq, s_write_acq)
    tl.a.bits := Mux(state === s_read_acq, get_acq, put_acq)
    tl.d.ready := state.isOneOf(s_read_gnt, s_write_gnt)
    io.in.ready := state === s_req

    when (state === s_start) {
      when (space =/= 0.U) {
        state := s_req
      } .elsewhen (timer === 0.U) {
        timer := (timeout - 1).U
        state := s_read_acq
      } .otherwise {
        timer := timer - 1.U
      }
    }

    when (state === s_read_acq && tl.a.ready) { state := s_read_gnt }
    when (state === s_read_gnt && tl.d.valid) {
      space := tl.d.bits.data
      state := s_start
    }

    when (io.in.fire()) {
      data := io.in.bits
      space := space - 1.U
      state := s_write_acq
    }

    when (state === s_write_acq && tl.a.ready) { state := s_write_gnt }
    when (state === s_write_gnt && tl.d.valid) { state := s_start }
  }
}

class OutFIFODriver(name: String, addr: BigInt, maxCount: Int)
    (implicit p: Parameters) extends LazyModule {

  val node = TLHelper.makeClientNode(
    name = name, sourceId = IdRange(0, 1))

  lazy val module = new LazyModuleImp(this) {
    val (tl, edge) = node.out(0)
    val dataBits = edge.bundle.dataBits
    val beatBytes = dataBits / 8
    val lgSize = log2Ceil(beatBytes)

    val io = IO(new Bundle {
      val out = Decoupled(UInt(dataBits.W))
    })

    val timeout = 64
    val timer = RegInit(0.U(log2Ceil(timeout).W))
    val count = RegInit(0.U(log2Ceil(maxCount + 1).W))
    val (s_start :: s_count_acq :: s_count_gnt ::
         s_fifo_acq :: s_fifo_gnt :: Nil) = Enum(5)
    val state = RegInit(s_start)

    tl.a.valid := state.isOneOf(s_count_acq, s_fifo_acq)
    tl.a.bits := edge.Get(
      fromSource = 0.U,
      toAddress = Mux(state === s_count_acq, (addr + beatBytes).U, addr.U),
      lgSize = lgSize.U)._2

    tl.d.ready :=
      (state === s_count_gnt) ||
      (state === s_fifo_gnt && io.out.ready)

    io.out.valid := state === s_fifo_gnt && tl.d.valid
    io.out.bits := tl.d.bits.data

    when (state === s_start) {
      when (count =/= 0.U) {
        state := s_fifo_acq
      } .elsewhen (timer === 0.U) {
        timer := (timeout - 1).U
        state := s_count_acq
      } .otherwise {
        timer := timer - 1.U
      }
    }

    when (tl.a.fire()) {
      state := Mux(state === s_count_acq, s_count_gnt, s_fifo_gnt)
    }

    when (tl.d.fire()) {
      count := Mux(state === s_count_gnt, tl.d.bits.data, count - 1.U)
      state := s_start
    }
  }
}

class SetRegisterDriver(name: String, addr: BigInt, n: Int)
    (implicit p: Parameters) extends LazyModule {

  val node = TLHelper.makeClientNode(
    name = name, sourceId = IdRange(0, 1))

  lazy val module = new LazyModuleImp(this) {
    val (tl, edge) = node.out(0)
    val dataBits = edge.bundle.dataBits
    val beatBytes = dataBits / 8
    val lgSize = log2Ceil(beatBytes)

    val io = IO(new Bundle {
      val values = Input(Vec(n, UInt(dataBits.W)))
    })

    val (s_start :: s_write_acq :: s_write_gnt :: s_wait :: Nil) = Enum(4)
    val state = RegInit(s_start)
    val values = Reg(Vec(n, UInt(dataBits.W)))

    val value_diff = Cat(io.values.zip(values).map {
      case (iovalue, value) => iovalue != value
    }.reverse)
    val value_set = RegInit(UInt(n.W), ~0.U(n.W))
    val value_set_oh = PriorityEncoderOH(value_set)
    val value_idx = OHToUInt(value_set_oh)

    tl.a.valid := state === s_write_acq
    tl.a.bits := edge.Put(
      fromSource = 0.U,
      toAddress = addr.U + (value_idx << lgSize.U),
      lgSize = lgSize.U,
      data = values(value_idx))._2
    tl.d.ready := state === s_write_gnt

    when (state === s_start) {
      state := s_write_acq
      for (i <- 0 until n) {
        when (value_set(i)) {
          values(i) := io.values(i)
        }
      }
    }
    when (tl.a.fire()) { state := s_write_gnt }
    when (tl.d.fire()) {
      value_set := value_set & ~value_set_oh
      state := s_wait
    }
    when (state === s_wait) {
      when (value_set.orR) {
        state := s_write_acq
      } .elsewhen (value_diff.orR) {
        value_set := value_diff
        state := s_start
      }
    }
  }
}

class SerialDriver(implicit p: Parameters) extends LazyModule {
  val base = p(ZynqAdapterBase)
  val depth = p(SerialFIFODepth)

  val node = TLIdentityNode()
  val xbar = LazyModule(new TLXbar)
  val outdrv = LazyModule(new OutFIFODriver("serial-out", base, depth))
  val indrv = LazyModule(new InFIFODriver("serial-in", base + BigInt(8), depth))

  xbar.node := outdrv.node
  xbar.node := indrv.node
  node := xbar.node

  lazy val module = new LazyModuleImp(this) {
    val (tl, edge) = node.out(0)
    require(edge.bundle.dataBits == SERIAL_IF_WIDTH)

    val io = IO(new Bundle {
      val serial = new SerialIO(SERIAL_IF_WIDTH)
    })

    indrv.module.io.in <> io.serial.in
    io.serial.out <> outdrv.module.io.out
  }
}

class ResetDriver(implicit p: Parameters) extends LazyModule {
  val base = p(ZynqAdapterBase)

  val node = TLIdentityNode()
  val driver = LazyModule(new SetRegisterDriver("reset", base + BigInt(0x10), 1))

  node := driver.node

  lazy val module = new LazyModuleImp(this) {
    driver.module.io.values(0) := 0.U
  }
}

class BlockDeviceDriver(implicit p: Parameters) extends LazyModule {
  val base = p(ZynqAdapterBase)
  val depth = p(BlockDeviceFIFODepth)

  val node = TLIdentityNode()
  val xbar = LazyModule(new TLXbar)
  val reqdrv  = LazyModule(new OutFIFODriver(
    "bdev-req", base + BigInt(0x20), depth))
  val datadrv = LazyModule(new OutFIFODriver(
    "bdev-data", base + BigInt(0x28), depth))
  val respdrv = LazyModule(new InFIFODriver(
    "bdev-resp", base + BigInt(0x30), depth))
  val infodrv = LazyModule(new SetRegisterDriver(
    "bdev-info", base + BigInt(0x38), 2))

  xbar.node := reqdrv.node
  xbar.node := datadrv.node
  xbar.node := respdrv.node
  xbar.node := infodrv.node
  node := xbar.node

  lazy val module = new LazyModuleImp(this) {
    val (tl, edge) = node.out(0)
    val dataBits = edge.bundle.dataBits

    val io = IO(new Bundle {
      val bdev = new BlockDeviceIO
    })

    val desser = Module(new BlockDeviceDesser(dataBits))
    io.bdev <> desser.io.bdev
    desser.io.ser.req <> reqdrv.module.io.out
    desser.io.ser.data <> datadrv.module.io.out
    respdrv.module.io.in <> desser.io.ser.resp
    infodrv.module.io.values := Seq(
      io.bdev.info.nsectors, io.bdev.info.max_req_len)
  }
}

class NetworkDriver(implicit p: Parameters) extends LazyModule {
  val base = p(ZynqAdapterBase)
  val depth = p(NetworkFIFODepth)

  val node = TLIdentityNode()
  val xbar = LazyModule(new TLXbar)
  val outdrv = LazyModule(new OutFIFODriver("net-out", base + BigInt(0x40), depth))
  val indrv  = LazyModule(new InFIFODriver("net-in", base + BigInt(0x48), depth))

  xbar.node := outdrv.node
  xbar.node := indrv.node
  node := xbar.node

  lazy val module = new LazyModuleImp(this) {
    val (tl, edge) = node.out(0)
    val dataBits = edge.bundle.dataBits

    val io = IO(new Bundle {
      val net = new NICIO
    })

    val desser = Module(new NetworkDesser(dataBits))
    desser.io.ser.out <> outdrv.module.io.out
    indrv.module.io.in <> desser.io.ser.in
    io.net <> desser.io.net
  }
}
