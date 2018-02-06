#!/bin/sh

javac udp_server.java
jar cvfm udp_server.jar manifest.txt udp_server.class DijkstraAlgorithm.class Edge.class Graph.class Read.class Vertex.class
java -cp udp_server.jar udp_server
