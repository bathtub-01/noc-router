package mesh_network

import chisel3._
import chisel3.util._

/*  
    (0, 1)    (1, 1)
    (0, 0)    (1, 0)

           N
           |
        W--|--E
           |
           S
 */

class NetworkExample extends Module {
  // TODO: change NetworkExample's io to a collection
  import NetworkConfig._
  val io = IO(new Bundle{
    val local00 = new RouterPort
    val local01 = new RouterPort
    val local10 = new RouterPort
    val local11 = new RouterPort
  })

  val router00 = Module(new Router(0, 0))
  val router01 = Module(new Router(0, 1))
  val router10 = Module(new Router(1, 0))
  val router11 = Module(new Router(1, 1))

  def routerConnectNull(r: Router) = {
    r.io.north_port <> 0.U.asTypeOf(new RouterPort)
    r.io.south_port <> 0.U.asTypeOf(new RouterPort)
    r.io.west_port <> 0.U.asTypeOf(new RouterPort)
    r.io.east_port <> 0.U.asTypeOf(new RouterPort)
  }
  def routerConnectRow(left: Router, right: Router) = {
    left.io.east_port.flit_in <> right.io.west_port.flit_out
    left.io.east_port.credit_in := right.io.west_port.credit_out
    right.io.west_port.flit_in <> left.io.east_port.flit_out
    right.io.west_port.credit_in := left.io.east_port.credit_out
  }
  def routerConnectCol(up: Router, down: Router) = {
    up.io.south_port.flit_in <> down.io.north_port.flit_out
    up.io.south_port.credit_in := down.io.north_port.credit_out
    down.io.north_port.flit_in <> up.io.south_port.flit_out
    down.io.north_port.credit_in := up.io.south_port.credit_out
  }

  routerConnectNull(router00)
  routerConnectNull(router01)
  routerConnectNull(router10)
  routerConnectNull(router11)

  routerConnectRow(router00, router10)
  routerConnectRow(router01, router11)
  routerConnectCol(router01, router00)
  routerConnectCol(router11, router10)

  io.local00 <> router00.io.local_port
  io.local01 <> router01.io.local_port
  io.local10 <> router10.io.local_port
  io.local11 <> router11.io.local_port
}