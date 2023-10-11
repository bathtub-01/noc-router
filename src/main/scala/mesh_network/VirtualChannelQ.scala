package mesh_network

import chisel3._
import chisel3.util._

/*  This module maintais free VCs of the downstream router
    at each output direction.
    Logically it is a FIFO, but we implement it as a arbiter
    since it is easier. (implement it by a FIFO requires
    insert initial elements in the FIFO manually)
 */
class VirtualChannelQ extends Module {
  import NetworkConfig._
  val io = IO(new Bundle {
    val enq = Flipped(Decoupled(UInt(log2Ceil(virtual_channels).W)))
    val deq = Decoupled(UInt(log2Ceil(virtual_channels).W))
  })
  val arbiter = Module(new RRArbiter(UInt(0.W), virtual_channels))
  val elements = RegInit(VecInit.fill(virtual_channels)(true.B))

  arbiter.io.in.zip(elements).foreach{case(a, e) =>
    a.bits := 0.U
    a.valid := e
    when(a.fire) {
      e := false.B
    }
  }
  io.enq.ready := !elements(io.enq.bits)
  when(io.enq.fire) {
    elements(io.enq.bits) := true.B
  }
  io.deq.valid := arbiter.io.out.valid
  io.deq.bits := arbiter.io.chosen
  arbiter.io.out.ready := io.deq.ready
}