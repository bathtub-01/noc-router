This is an implementation of an on-chip network router, which can be used to construct a 2-D mesh network.

Some features of this design:

1. Flit-based wormhole flow control with configurable virtual channels
2. Using credit-based traffic throttling
3. Routing with simple X-first schemes
4. 2-stage pipelining, removing virtual channel allocation with virtual channel selection

You can view [my blog](https://bathtub-01.github.io/posts/implementing-an-on-chip-network-router-with-good-practices/) for more details.

## Usage

To use this project, you should have `sbt` installed.

To generate Verilog for a 2×2 example mesh network, simply use the command:
```
$ sbt run
```
The Verilog will be placed under `./generated`.

To run the test for above network, use the command:
```
$ sbt 'testOnly mesh_network.NetworkExampleSpec'
```
The test uses a state machine to inject packets into the network. Refer code in `./test/scala/mesh_network/RouterSpec.scala` if you want to modify the test. The tester will generate waveforms under `./test_run_dir`. You can use open-sourced tools such as [gtkwave](https://gtkwave.sourceforge.net) to view the waveform.
