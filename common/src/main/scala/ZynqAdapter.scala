package zynq

import chisel3._
import chisel3.util._
import config.{Parameters, Field}
import diplomacy.{LazyModule, LazyModuleImp, IdRange}
import junctions.{SerialIO, NastiIO}
import testchipip._
import uncore.axi4._
import regmapper.{RegField, HasRegMap}
import rocketchip.SlaveConfig

case object SerialFIFODepth extends Field[Int]
case object BlockDeviceFIFODepth extends Field[Int]
case object ResetCycles extends Field[Int]

trait ZynqAdapterCoreBundle extends Bundle {
  implicit val p: Parameters

  val sys_reset = Output(Bool())
  val serial = Flipped(new SerialIO(p(SerialInterfaceWidth)))
  val bdev = Flipped(new BlockDeviceIO)
}

trait ZynqAdapterCoreModule extends Module with HasRegMap
    with HasBlockDeviceParameters {
  implicit val p: Parameters
  val io: ZynqAdapterCoreBundle
  val w = p(SerialInterfaceWidth)

  val serDepth = p(SerialFIFODepth)
  val bdevDepth = p(BlockDeviceFIFODepth)
  val serCountBits = log2Ceil(serDepth + 1)
  val bdevCountBits = log2Ceil(bdevDepth + 1)

  val ser_out_fifo = Module(new Queue(UInt(w.W), serDepth))
  val ser_in_fifo  = Module(new Queue(UInt(w.W), serDepth))
  val sys_reset = RegInit(true.B)
  val bdev_req_fifo  = Module(new Queue(UInt(w.W), bdevDepth))
  val bdev_data_fifo = Module(new Queue(UInt(w.W), bdevDepth))
  val bdev_resp_fifo = Module(new Queue(UInt(w.W), bdevDepth))
  val bdev_info = Reg(new BlockDeviceInfo)
  val serdes = Module(new BlockDeviceSerdes(w))

  serdes.io.bdev <> io.bdev
  bdev_req_fifo.io.enq <> serdes.io.ser.req
  bdev_data_fifo.io.enq <> serdes.io.ser.data
  serdes.io.ser.resp <> bdev_resp_fifo.io.deq
  io.bdev.info := bdev_info
  ser_out_fifo.io.enq <> io.serial.out
  io.serial.in <> ser_in_fifo.io.deq
  io.sys_reset := sys_reset

  val ser_in_space = (serDepth.U - ser_in_fifo.io.count)
  val bdev_resp_space = (bdevDepth.U - bdev_resp_fifo.io.count)

  /**
   * Address Map
   * 0x00 - serial out FIFO data
   * 0x04 - serial out FIFO data available (words)
   * 0x08 - serial in  FIFO data
   * 0x0C - serial in  FIFO space available (words)
   * 0x10 - system reset
   * 0x20 - req FIFO data
   * 0x24 - req FIFO data available (words)
   * 0x28 - data FIFO data
   * 0x2C - data FIFO data available (words)
   * 0x30 - resp FIFO data
   * 0x34 - resp FIFO space available (words)
   * 0x38 - nsectors
   * 0x3C - max request length
   * 0x40 - # of trackers
   */
  regmap(
    0x00 -> Seq(RegField.r(w, ser_out_fifo.io.deq)),
    0x04 -> Seq(RegField.r(serCountBits, ser_out_fifo.io.count)),
    0x08 -> Seq(RegField.w(w, ser_in_fifo.io.enq)),
    0x0C -> Seq(RegField.r(serCountBits, ser_in_space)),
    0x10 -> Seq(RegField(1, sys_reset)),
    0x20 -> Seq(RegField.r(w, bdev_req_fifo.io.deq)),
    0x24 -> Seq(RegField.r(bdevCountBits, bdev_req_fifo.io.count)),
    0x28 -> Seq(RegField.r(w, bdev_data_fifo.io.deq)),
    0x2C -> Seq(RegField.r(bdevCountBits, bdev_data_fifo.io.count)),
    0x30 -> Seq(RegField.w(w, bdev_resp_fifo.io.enq)),
    0x34 -> Seq(RegField.r(bdevCountBits, bdev_resp_space)),
    0x38 -> Seq(RegField(sectorBits, bdev_info.nsectors)),
    0x3C -> Seq(RegField(sectorBits, bdev_info.max_req_len)))
}

class ZynqAdapterCore(address: BigInt, beatBytes: Int)(implicit p: Parameters)
  extends AXI4RegisterRouter(
    address, beatBytes = beatBytes, concurrency = 1)(
      new AXI4RegBundle((), _)    with ZynqAdapterCoreBundle)(
      new AXI4RegModule((), _, _) with ZynqAdapterCoreModule)

class ZynqAdapter(address: BigInt, config: SlaveConfig)(implicit p: Parameters)
    extends LazyModule {

  val node = AXI4BlindInputNode(Seq(AXI4MasterPortParameters(
    masters = Seq(AXI4MasterParameters(
      name = "Zynq Adapter",
      id = IdRange(0, 1 << config.idBits))))))

  val core = LazyModule(new ZynqAdapterCore(address, config.beatBytes))
  core.node := AXI4Fragmenter()(node)

  lazy val module = new LazyModuleImp(this) {
    val io = new Bundle {
      val axi = node.bundleIn
      val sys_reset = Output(Bool())
      val serial = Flipped(new SerialIO(p(SerialInterfaceWidth)))
      val bdev = Flipped(new BlockDeviceIO)
    }

    val coreIO = core.module.io
    io.sys_reset := coreIO.sys_reset
    coreIO.serial <> io.serial
    coreIO.bdev <> io.bdev
  }
}

object NastiAXIConnect {
  def apply(left: AXI4Bundle, right: NastiIO) {
    left.ar.valid      := right.ar.valid
    left.ar.bits.id    := right.ar.bits.id
    left.ar.bits.addr  := right.ar.bits.addr
    left.ar.bits.len   := right.ar.bits.len
    left.ar.bits.size  := right.ar.bits.size
    left.ar.bits.burst := right.ar.bits.burst
    left.ar.bits.lock  := right.ar.bits.lock
    left.ar.bits.cache := right.ar.bits.cache
    left.ar.bits.prot  := right.ar.bits.prot
    left.ar.bits.qos   := right.ar.bits.qos
    left.ar.bits.user.foreach(_ := right.ar.bits.user)
    right.ar.ready     := left.ar.ready

    left.aw.valid      := right.aw.valid
    left.aw.bits.id    := right.aw.bits.id
    left.aw.bits.addr  := right.aw.bits.addr
    left.aw.bits.len   := right.aw.bits.len
    left.aw.bits.size  := right.aw.bits.size
    left.aw.bits.burst := right.aw.bits.burst
    left.aw.bits.lock  := right.aw.bits.lock
    left.aw.bits.cache := right.aw.bits.cache
    left.aw.bits.prot  := right.aw.bits.prot
    left.aw.bits.qos   := right.aw.bits.qos
    left.aw.bits.user.foreach(_ := right.aw.bits.user)
    right.aw.ready     := left.aw.ready

    left.w.valid     := right.w.valid
    left.w.bits.data := right.w.bits.data
    left.w.bits.strb := right.w.bits.strb
    left.w.bits.last := right.w.bits.last
    right.w.ready    := left.w.ready

    right.r.valid     := left.r.valid
    right.r.bits.id   := left.r.bits.id
    right.r.bits.resp := left.r.bits.resp
    right.r.bits.data := left.r.bits.data
    right.r.bits.last := left.r.bits.last
    left.r.bits.user.foreach(right.r.bits.user := _)
    left.r.ready      := right.r.ready

    right.b.valid     := left.b.valid
    right.b.bits.id   := left.b.bits.id
    right.b.bits.resp := left.b.bits.resp
    left.b.bits.user.foreach(right.b.bits.user := _)
    left.b.ready      := right.b.ready
  }
}
