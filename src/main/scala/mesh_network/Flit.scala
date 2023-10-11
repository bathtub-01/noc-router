package mesh_network

import chisel3._
import chisel3.util._

object FlitTypes extends ChiselEnum {
  val head = Value
  val body = Value
  val tail = Value
  val single = Value
}

class FlitHeader extends Bundle {
  val flit_type = FlitTypes()
  val vc_id = UInt(log2Ceil(NetworkConfig.virtual_channels).W)
}

abstract class FlitLoad extends Bundle

class HeadFlitLoad extends FlitLoad {
  val source = new Coordinate
  val dest = new Coordinate
  val data = Bits((NetworkConfig.flit_load_width - source.getWidth - dest.getWidth).W)
}

class DataFlitLoad extends FlitLoad {
  val data = Bits(NetworkConfig.flit_load_width.W)
}

class Flit extends Bundle {
  val header = new FlitHeader
  val load = Bits(NetworkConfig.flit_load_width.W)
}
