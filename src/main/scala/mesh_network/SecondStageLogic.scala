package mesh_network

import chisel3._
import chisel3.util._

/*  Second stage logic for each output physical port.
    It contains switch allocation (2) and switch traversal.
    It returns the winner flit of switch allocation (2).
 */
class SecondStageLogic extends Module {
  val io = IO(new Bundle {
    val in_flits = Vec(5, Flipped(Decoupled(new Flit)))
    val winner_flit = Decoupled(new Flit)
  })
  import NetworkConfig._
  val arbiter = Module(new RRArbiter(new Flit, 5))
  arbiter.io.in.zip(io.in_flits).foreach{case(a, i) => a <> i}
  io.winner_flit <> arbiter.io.out
}