package mesh_network

import chisel3._
import chisel3.util._

object NetworkConfig {
  val rows = 2
  val columns = 2
  val nodes = rows * columns
  val virtual_channels = 2
  val flit_load_width = 128
  val buffer_depth = 8

  def idx2Coordinate(idx: Int): (Int, Int) = {
    val x = idx / rows
    val y = idx % rows
    (x, y)
  }
}

// the address of a router
class Coordinate extends Bundle {
  val x = UInt(log2Ceil(NetworkConfig.columns).W)
  val y = UInt(log2Ceil(NetworkConfig.rows).W)
}
