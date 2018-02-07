Team:
Lindun He  email: he370@purdue.edu
Peilun Qi  email: qi66@purdue.edu

(1) Compile & Run:

Method 1:

Compile and run controller:
./c.sh

Compile and run Switch:
./s.sh id port

Method 2:

Compile controller:
javac udp_server.java
jar cvfm udp_server.jar manifest.txt udp_server.class DijkstraAlgorithm.class Edge.class Graph.class Read.class Vertex.class

Run controller:
java -cp udp_server.jar udp_server

Compile Switch:
javac Switch.java
jar cvfm Switch.jar manifest_switch.txt Switch.class SwitchSockHandler.class SwitchTimerTask.class

Run Switch:
java -cp Switch.jar Switch 1 2000

(2) Switch Console CMD :
each switch has a console to interact with

1> change mode
"m 0" console mode
"m 1" print topo info
"m 2" print sender info
"m 3" print receiver info
"m 4" print all

2> print table
"p nt" print neighbors info
"p rt" print routing table

3> block link
"b id" block the link to Switch whose ID is id
