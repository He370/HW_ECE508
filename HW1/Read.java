import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.AbstractDocument.Content;

public class Read {
	List<Vertex> nodes;
	List<Edge> edges;
	public Read(List<Vertex> nodes, List<Edge> edges) {
		this.nodes = nodes;
		this.edges = edges;
	}
	public static int top = 0;
	public Hashtable<String, HashSet<String>> getNeighbors() throws IOException{
		Hashtable<String, HashSet<String>> neighbors = new Hashtable<>();
		ArrayList<String> answer = new ArrayList<>();
		String filePath = "test2.txt";
		 File file = new File(filePath);
		 FileInputStream inputStream = new FileInputStream(filePath);
	     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	     String str = null;
	        while((str = bufferedReader.readLine()) != null)
	        {
	        	answer.add(str);
	        }
	        top = Integer.valueOf(answer.get(0));
	        System.out.println(top);
	        for(int i = 1; i <= top; i++) {
	        	neighbors.put(String.valueOf(i), new HashSet<String>());
	        }
	        for(int r = 1; r < answer.size(); r++) {
	        	String[] row = answer.get(r).split(" ");
	        	String start = (row[0]) ;
	        	String end = (row[1]);
	        	neighbors.get(start).add(end);
	        	neighbors.get(end).add(start);
	        }
	        return neighbors;
	}
	public ArrayList<String> read() throws IOException {
		ArrayList<String> routing_tab = new ArrayList<>();
		ArrayList<String> answer = new ArrayList<>();
		String filePath = "test2.txt";
		 File file = new File(filePath);
		 FileInputStream inputStream = new FileInputStream(filePath);
	     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	     String str = null;
	        while((str = bufferedReader.readLine()) != null)
	        {
	        	answer.add(str);
	        }
	    top = Integer.valueOf(answer.get(0));
	    for(int i = 1; i <= top; i++) {
	        	Vertex location = new Vertex(String.valueOf(i), "Node_" + i);
	            nodes.add(location);
	    }
        for(int r = 1; r < answer.size(); r++) {
        	String[] row = answer.get(r).split(" ");
        	addLane(Integer.valueOf(row[0]), Integer.valueOf(row[1]), Integer.valueOf(row[3]));
        	addLane(Integer.valueOf(row[1]), Integer.valueOf(row[0]), Integer.valueOf(row[3]));
        }
        Graph graph = new Graph(nodes, edges);
        for(int i = 0; i < top ; i++) {
        	DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        	dijkstra.run_dij(nodes.get(i));
        	routing_tab.add("");
        	for(int j = 0; j < top; j++) {
       	        LinkedList<Vertex> path = dijkstra.getPath(nodes.get(j));
       	        if(path == null) {
       	        	String message = routing_tab.get(i)  + "-1"+ " ";
       	        	routing_tab.set(i, message);
       	        	System.out.println("the path from source " + (i + 1) +" to destination " + (j + 1) + "is: ");
       	        	System.out.println("null");

       	        }
       	        else if(path.size() > 0){
       	        	String message = routing_tab.get(i)  + path.get(1).getId()+ " ";
       	        	routing_tab.set(i, message);
       	        	System.out.println("the path from source " + (i + 1) +" to destination " + (j + 1) + "is: ");
       	        	for (Vertex vertex : path) {
		 	            System.out.print("Node: "+ vertex.getId());
		 	            System.out.print(" ");
		 	        }
       	        	System.out.println("");
       	        }
        	}
        }
        for(String tab:routing_tab){
                         System.out.println(tab);
                    }
        return routing_tab;
	}

	public void addLane(int sourceLocNo, int destLocNo,
	            int duration) {
	        Edge lane = new Edge(nodes.get(sourceLocNo - 1), nodes.get(destLocNo - 1), duration);
	        edges.add(lane);
	    }

}
