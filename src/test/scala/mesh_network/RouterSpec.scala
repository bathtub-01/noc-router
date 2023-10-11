package mesh_network

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

class NetworkExampleTop extends Module {
  import NetworkConfig._
  val io = IO(new Bundle{
    val start = Input(Bool())
    val local00_flit_out = Decoupled(new Flit)
    val local01_flit_out = Decoupled(new Flit)
    val local10_flit_out = Decoupled(new Flit)
    val local11_flit_out = Decoupled(new Flit)
  })
  
  object state extends ChiselEnum {
    val idle = Value
    val feed1 = Value
    val feed2 = Value
    val feed3 = Value
    val ending = Value
  }
  import state._
  val network = Module(new NetworkExample)
  val STM = RegInit(idle)

  network.io.local00 <> 0.U.asTypeOf(new RouterPort)
  network.io.local01 <> 0.U.asTypeOf(new RouterPort)
  network.io.local10 <> 0.U.asTypeOf(new RouterPort)
  network.io.local11 <> 0.U.asTypeOf(new RouterPort)

  io.local00_flit_out <> network.io.local00.flit_out
  io.local01_flit_out <> network.io.local01.flit_out
  io.local10_flit_out <> network.io.local10.flit_out
  io.local11_flit_out <> network.io.local11.flit_out

  network.io.local11.credit_in(0) := 8.U
  network.io.local11.credit_in(1) := 8.U

  switch(STM) {
    is(idle) {
      when(io.start) {
        STM := feed1
      }
    }
    is(feed1) {
      network.io.local00.flit_in.valid := true.B
      network.io.local00.flit_in.bits.header.flit_type := FlitTypes.head
      network.io.local00.flit_in.bits.header.vc_id := 1.U
      val load1 = Wire(new HeadFlitLoad)
      load1.source.x := 0.U
      load1.source.y := 0.U
      load1.dest.x := 1.U
      load1.dest.y := 1.U
      load1.data := BigInt("F00D11A", 16).U
      network.io.local00.flit_in.bits.load := load1.asTypeOf(UInt(flit_load_width.W))

      STM := feed2
    }
    is(feed2) {
      network.io.local00.flit_in.valid := true.B
      network.io.local00.flit_in.bits.header.flit_type := FlitTypes.body
      network.io.local00.flit_in.bits.header.vc_id := 1.U
      val load1 = Wire(new DataFlitLoad)
      load1.data := BigInt("F00D11B", 16).U
      network.io.local00.flit_in.bits.load := load1.asTypeOf(UInt(flit_load_width.W))
     
      STM := feed3
    }
    is(feed3) {
      network.io.local00.flit_in.valid := true.B
      network.io.local00.flit_in.bits.header.flit_type := FlitTypes.tail
      network.io.local00.flit_in.bits.header.vc_id := 1.U
      val load1 = Wire(new DataFlitLoad)
      load1.data := BigInt("F00D11C", 16).U
      network.io.local00.flit_in.bits.load := load1.asTypeOf(UInt(flit_load_width.W))

      network.io.local10.flit_in.valid := true.B
      network.io.local10.flit_in.bits.header.flit_type := FlitTypes.single
      network.io.local10.flit_in.bits.header.vc_id := 1.U
      val load2 = Wire(new HeadFlitLoad)
      load2.source.x := 1.U
      load2.source.y := 0.U
      load2.dest.x := 1.U
      load2.dest.y := 1.U
      load2.data := BigInt("F10D11A", 16).U
      network.io.local10.flit_in.bits.load := load2.asTypeOf(UInt(flit_load_width.W))

      STM := ending
    }
    is(ending) {/* nothing to do */}
  }
}

class NetworkExampleSpec extends AnyFreeSpec with ChiselScalatestTester {
  "Network should transmit" in {
    test(new NetworkExampleTop).withAnnotations(Seq(/* VerilatorBackendAnnotation,  */WriteVcdAnnotation)) { dut =>
      dut.clock.step(3)
      dut.io.start.poke(true.B)
      dut.io.local11_flit_out.ready.poke(true.B)
      dut.clock.step(20)
    }
  }
}

class RouterSpec extends AnyFreeSpec with ChiselScalatestTester {
  "Router should route" in {
    test(new Router(1, 1)).withAnnotations(Seq(/* VerilatorBackendAnnotation,  */WriteVcdAnnotation)) { dut =>
      // def pokeFlit(flit_type: FlitTypes.Type, vc_id: Int, data: String) = {
      //   dut.io.in_flit.bits.header.flit_type.poke(flit_type)
      //   dut.io.in_flit.bits.header.vc_id.poke(vc_id)
      //   dut.io.in_flit.bits.load.poke(BigInt(data, 16))
      //   dut.io.in_flit.valid.poke(true.B)
      //   dut.clock.step()
      //   dut.io.in_flit.valid.poke(false.B)
      // }
      import FlitTypes._
      // pokeFlit(head, 1, "DEAD")
      // pokeFlit(single, 0, "BAD")
      // pokeFlit(body, 1, "ACDC")
      // pokeFlit(tail, 1, "FEBA")
      dut.clock.step(3)
      dut.io.east_port.flit_in.valid.poke(true)
      dut.io.east_port.flit_in.bits.header.flit_type.poke(head)
      dut.io.east_port.flit_in.bits.header.vc_id.poke(1)

      dut.io.east_port.credit_in(0).poke(8.U)
      dut.io.east_port.credit_in(1).poke(8.U)
      dut.io.west_port.credit_in(0).poke(8.U)
      dut.io.west_port.credit_in(1).poke(8.U)
      dut.io.south_port.credit_in(0).poke(8.U)
      dut.io.south_port.credit_in(1).poke(8.U)
      dut.io.north_port.credit_in(0).poke(8.U)
      dut.io.north_port.credit_in(1).poke(8.U)
      dut.clock.step()
      dut.io.east_port.flit_in.valid.poke(false.B)
      dut.clock.step(10)
    }
  }
}