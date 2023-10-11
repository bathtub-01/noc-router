package mesh_network

import chisel3._
import chisel3.util._

/*  The result of route computing, indicating where the flit should 
    be directed next. 
*/
object RouteTarget extends ChiselEnum {
  val NORTH = Value
  val SOUTH = Value
  val WEST = Value
  val EAST = Value
  val LOCAL = Value
}

/*  This module computes the output port for head flits.
    It only contains combinational logic.
    X-first dimension-ordered routing is used.
 */
class RouteComputeUnit(x: Int, y: Int) extends Module {
  val io = IO(new Bundle {
    val flit_dest = Input(new Coordinate)
    val route_target = Output(RouteTarget())
  })
  import RouteTarget._
  when(io.flit_dest.x =/= x.U) {
    io.route_target := Mux(io.flit_dest.x > x.U, EAST, WEST)
  }.elsewhen(io.flit_dest.y =/= y.U) {
    io.route_target := Mux(io.flit_dest.y > y.U, NORTH, SOUTH)
  }.otherwise {
    io.route_target := LOCAL
  }
}