import chisel3._

import mesh_network._

object Main extends App {
  emitVerilog(new NetworkExample, Array("--target-dir", "generated"))
}