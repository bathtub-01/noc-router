package mesh_network

import chisel3._
import chisel3.util._

class FlitAndTarget extends Bundle {
  val flit = new Flit
  val target = RouteTarget()
}

/*  First stage logic for all VCs of a input physical port.
    It is located right behind each input buffer.
    It contains route compute and switch allocation (1).
    It select the winner flit of switch allocation (1).
    `winner_target` is only meaningful when
    the winner is a head or single flit.
 */
class FirstStageLogic(x: Int, y: Int) extends Module {
  import NetworkConfig._
  val io = IO(new Bundle {
    val in_flits = Vec(virtual_channels, Flipped(Decoupled(new Flit))) // connect to input buffer
    val free_vc = Input(Vec(5, Bool())) // indicates if there are free VCs in five output ports
    val stall = Input(Vec(virtual_channels, Bool())) // outside module determines if a VC is stalled based on credit
    val winner_flit = Decoupled(new Flit)
    val winner_vc = Output(UInt(log2Ceil(virtual_channels).W)) // which local vc the winner belongs to
    val winner_target = Output(RouteTarget())
  })
  val rcUnits = Seq.fill(virtual_channels)(Module(new RouteComputeUnit(x, y)))
  val arbiter = Module(new RRArbiter(new FlitAndTarget, virtual_channels))
  
  arbiter.io.in.zip(io.in_flits).foreach{case(a, i) =>
    i.ready := false.B
    a.valid := false.B
    a.bits.flit := i.bits 
  }
  arbiter.io.in.zip(rcUnits).foreach{case(a, rc) =>
    a.bits.target := rc.io.route_target
  }

  for(i <- 0 until virtual_channels) {
    val in_flit = io.in_flits(i)
    rcUnits(i).io.flit_dest := in_flit.bits.load.asTypeOf(new HeadFlitLoad).dest
    when(in_flit.valid) {
      when(in_flit.bits.header.flit_type === FlitTypes.head ||
           in_flit.bits.header.flit_type === FlitTypes.single) {
        when(io.free_vc(rcUnits(i).io.route_target.asUInt)) {
          // participate in arbitration
          in_flit.ready := arbiter.io.in(i).ready
          arbiter.io.in(i).valid := in_flit.valid
        }
      }.otherwise { // for body and tail flits
        when(!io.stall(i)) {
          // participate in arbitration
          in_flit.ready := arbiter.io.in(i).ready
          arbiter.io.in(i).valid := in_flit.valid
        }
      }
    }
  }

  io.winner_flit.valid := arbiter.io.out.valid
  io.winner_flit.bits := arbiter.io.out.bits.flit
  arbiter.io.out.ready := io.winner_flit.ready
  io.winner_target := arbiter.io.out.bits.target
  io.winner_vc := arbiter.io.chosen
}