package mesh_network

import chisel3._
import chisel3.util._

class DeMux extends Module {
  import NetworkConfig._
  val io = IO(new Bundle {
    val in_flit = Flipped(Decoupled(new Flit))
    val out_flits = Vec(virtual_channels, Decoupled(new Flit))
  })
  io.in_flit.ready := true.B
  io.out_flits.foreach{ o =>
    o.valid := false.B
    o.bits := DontCare
  }
  when(io.in_flit.fire) {
    io.out_flits(io.in_flit.bits.header.vc_id).bits := io.in_flit.bits
    io.out_flits(io.in_flit.bits.header.vc_id).valid := true.B
  }
}

class InputBuffers extends Module {
  import NetworkConfig._
  val io = IO(new Bundle {
    val in_flit = Flipped(Decoupled(new Flit))
    val out_flits = Vec(virtual_channels, Decoupled(new Flit))
    val room = Output(Vec(virtual_channels, 
                            UInt(log2Ceil(buffer_depth + 1).W)))
  })
  val deMux = Module(new DeMux)
  var i = -1
  val buffers = Seq.fill(virtual_channels){
    i = i + 1
    Queue(deMux.io.out_flits(i), buffer_depth, useSyncReadMem = true)
  }
  val roomReg = RegInit(VecInit.fill(virtual_channels)(buffer_depth.U))

  io.in_flit <> deMux.io.in_flit
  buffers.zip(io.out_flits).foreach{ case (b, o) =>
    b <> o
  }

  for(i <- 0 until virtual_channels) {
    when(buffers(i).fire && !deMux.io.out_flits(i).fire) {
      roomReg(i) := roomReg(i) + 1.U
    }
    when(deMux.io.out_flits(i).fire && !buffers(i).fire) {
      roomReg(i) := roomReg(i) - 1.U
    }
  }
  io.room := roomReg
}