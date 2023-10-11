package mesh_network

import chisel3._
import chisel3.util._

// an arbiter that only shows granted results
class Granter(n: Int) extends Module {
  val io = IO(new Bundle {
    val request = Input(Vec(n, Bool()))
    val granted = Output(UInt(log2Ceil(n).W))
  })
  val arbiter = Module(new RRArbiter(UInt(0.W), n))
  arbiter.io.in.zip(io.request).foreach{case(i, r) =>
    i.bits := 0.U
    i.valid := r
  }
  arbiter.io.out.ready := true.B
  io.granted := arbiter.io.chosen
}