package zynq

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.{Parameters, Field}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, IdRange}
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.regmapper.{RegField, HasRegMap}
import freechips.rocketchip.chip.SlaveConfig
import testchipip._
import testchipip.SerialAdapter._
import icenet._
import icenet.IceNIC._

case object SerialFIFODepth extends Field[Int]
case object BlockDeviceFIFODepth extends Field[Int]
case object NetworkFIFODepth extends Field[Int]
case object ResetCycles extends Field[Int]

trait ZynqAdapterCoreBundle extends Bundle {
  implicit val p: Parameters

  val sys_reset = Output(Bool())
  val serial = Flipped(new SerialIO(SERIAL_IF_WIDTH))
  val bdev = Flipped(new BlockDeviceIO)
  val net = Flipped(new NICIO)
}

trait ZynqAdapterCoreModule extends Module with HasRegMap
    with HasBlockDeviceParameters {
  implicit val p: Parameters
  val io: ZynqAdapterCoreBundle
  val w = SERIAL_IF_WIDTH

  val serDepth = p(SerialFIFODepth)
  val bdevDepth = p(BlockDeviceFIFODepth)
  val netDepth = p(NetworkFIFODepth)
  val serCountBits = log2Ceil(serDepth + 1)
  val bdevCountBits = log2Ceil(bdevDepth + 1)
  val netCountBits = log2Ceil(netDepth + 1)

  val ser_out_fifo = Module(new Queue(UInt(w.W), serDepth))
  val ser_in_fifo  = Module(new Queue(UInt(w.W), serDepth))

  ser_out_fifo.io.enq <> io.serial.out
  io.serial.in <> ser_in_fifo.io.deq

  val sys_reset = RegInit(true.B)
  io.sys_reset := sys_reset

  val bdev_req_fifo  = Module(new Queue(UInt(w.W), bdevDepth))
  val bdev_data_fifo = Module(new Queue(UInt(w.W), bdevDepth))
  val bdev_resp_fifo = Module(new Queue(UInt(w.W), bdevDepth))
  val bdev_info = Reg(new BlockDeviceInfo)
  val bdev_serdes = Module(new BlockDeviceSerdes(w))

  bdev_serdes.io.bdev <> io.bdev
  bdev_req_fifo.io.enq <> bdev_serdes.io.ser.req
  bdev_data_fifo.io.enq <> bdev_serdes.io.ser.data
  bdev_serdes.io.ser.resp <> bdev_resp_fifo.io.deq
  io.bdev.info := bdev_info

  val net_out_fifo = Module(new Queue(UInt(w.W), netDepth))
  val net_in_fifo = Module(new Queue(UInt(w.W), netDepth))
  val net_serdes = Module(new NetworkSerdes(w))

  net_serdes.io.net <> io.net
  net_out_fifo.io.enq <> net_serdes.io.ser.out
  net_serdes.io.ser.in <> net_in_fifo.io.deq

  val ser_in_space = (serDepth.U - ser_in_fifo.io.count)
  val bdev_resp_space = (bdevDepth.U - bdev_resp_fifo.io.count)
  val net_in_space = (netDepth.U - net_in_fifo.io.count)

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
   * 0x40 - network out FIFO data
   * 0x44 - network out FIFO data available (words)
   * 0x48 - network in FIFO data
   * 0x4C - network in FIFO space available (words)
   * 0x50 - MAC 0-3
   * 0x54 - MAC 4-5
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
    0x3C -> Seq(RegField(sectorBits, bdev_info.max_req_len)),
    0x40 -> Seq(RegField.r(w, net_out_fifo.io.deq)),
    0x44 -> Seq(RegField.r(netCountBits, net_out_fifo.io.count)),
    0x48 -> Seq(RegField.w(w, net_in_fifo.io.enq)),
    0x4C -> Seq(RegField.r(netCountBits, net_in_space)),
    0x50 -> Seq(RegField.r(32, io.net.macAddr.bits(31, 0))),
    0x54 -> Seq(RegField.r(16, io.net.macAddr.bits(47, 32))))
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
      val serial = Flipped(new SerialIO(SERIAL_IF_WIDTH))
      val bdev = Flipped(new BlockDeviceIO)
      val net = Flipped(new NICIO)
    }

    val coreIO = core.module.io
    io.sys_reset := coreIO.sys_reset
    coreIO.serial <> io.serial
    coreIO.bdev <> io.bdev
    coreIO.net <> io.net
  }
}
