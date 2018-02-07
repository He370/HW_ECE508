import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Handler;
import java.util.LinkedList;
//import org.junit.experimental.theories.Theories;
//import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;

//import Dijkstra.Edge;
//import Dijkstra.Vertex;
//import ReadFile.Read;

public class udp_server {
	public static List<Vertex> nodes = new ArrayList<Vertex>();
	public static List<Edge> edges = new ArrayList<Edge>();
	public static Hashtable<String, HashSet<String>> neighbors;
	public static String[] hostAddress;
	public static String[] port;
	public static String[] alive;
	public static int count = 0;
	public static void main(String args[]) throws IOException
    {
		Read reader = new Read(nodes, edges);
		neighbors = reader.getNeighbors();
		hostAddress = new String[neighbors.size()];
		for(int i = 0; i < neighbors.size(); i++) {
			hostAddress[i] = "NULL";
		}
		port = new String[neighbors.size()];
		for(int i = 0; i < neighbors.size(); i++) {
			port[i] = "NULL";
		}
		alive = new String[neighbors.size()];
		for(int i = 0; i < neighbors.size(); i++) {
			alive[i] = "0";
		}
        DatagramSocket sock = null;

        try
        {

            sock = new DatagramSocket(5000);
            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            echo("Server socket created. Waiting for incoming data...");


            while(true)
            {
                sock.receive(incoming);
                byte[] data = incoming.getData();
                String s = new String(data, 0, incoming.getLength());
                echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
                String[] message = s.split(" ");
                String answer = "";

								echo("msg:"+message[0]);

                if(message[0].equals("0")) {
                	answer += "1" + " ";
                	String id = message[1];

					hostAddress[Integer.valueOf(id) - 0] = incoming.getAddress().getHostAddress();
                	port[Integer.valueOf(id) - 0] = String.valueOf(incoming.getPort());
                	alive[Integer.valueOf(id) - 0] = "1";
                	HashSet<String> neighbor = neighbors.get(id);
                	for(String n: neighbor) {
                		int n_id = Integer.valueOf(n);
                        answer = answer + n + " ";
                		answer = answer + hostAddress[n_id] + " ";
                		answer = answer + port[n_id] + " ";
                		answer = answer + alive[n_id] + " ";
                	}
                	answer += "EOF";
				echo("ans:" + answer);
                	DatagramPacket dp = new DatagramPacket(answer.getBytes() , answer.getBytes().length , incoming.getAddress() , incoming.getPort());
                  sock.send(dp);
                }
                String all = "1";
                for(String a: alive) {
                	if(a.equals("0")) {
                		all = "0";
                	}
                }
                if(count == 0 && all.equals("1")) {
                	ArrayList<String> routing_tab = reader.read();
                    for(String tab:routing_tab){
                         System.out.println(tab);
                    }
                   
                	for(int i = 0; i < routing_tab.size(); i++) {
                		String ans = "3" + " ";
                		ans += routing_tab.get(i);
                		ans += "EOF";
                        System.out.println(ans);
                        System.out.println(InetAddress.getByName(hostAddress[i]));
                		DatagramPacket dp = new DatagramPacket(ans.getBytes() , ans.getBytes().length , InetAddress.getByName(hostAddress[i]) , Integer.valueOf(port[i]));
                        System.out.println(Integer.valueOf(port[i]));
                        sock.send(dp);
                	}
                    count++;
                }
                 /////////////////////////////////////////////////////////////////////////////////////////

                if(count > 0 && message[0].equals("4")) {
                    System.out.println("receiving dis_connect message");
                    String id = message[1];
                    ArrayList<String> unreach = new ArrayList<>();

                    for(int i = 1; i * 2 < message.length - 1; i++) {
                        int index = 2 * i;
                        if(message[index + 1].equals("0")) {
                            unreach.add(message[index]);
                        }
                    }
                    if(unreach != null || unreach.size() > 0) {
                        for(String destination: unreach) {
                            for(int i = 0; i < edges.size(); i++) {
                                String start = edges.get(i).getDestination().getId();
                                String end = edges.get(i).getSource().getId();
                                if(((start.equals(id)) && (end.equals(destination)))||((start.equals(destination)) && (end.equals(id)))){
                                    edges.remove(i);
                                }
                            }
                        }
                        ArrayList<String> routing_tab = new ArrayList<>();
                        Graph graph = new Graph(nodes, edges);
                         for(int i = 0; i < nodes.size() ; i++) {
                                DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
                                dijkstra.execute(nodes.get(i));
                                routing_tab.add("");
                                for(int j = 0; j < nodes.size(); j++) {
                                    LinkedList<Vertex> path = dijkstra.getPath(nodes.get(j));
                                    if(path == null) {
                                        String mes = routing_tab.get(i)  + "-1"+ " ";
                                        routing_tab.set(i, mes);
                                        System.out.println("the path from source " + i +" to destination " + j + "is: ");
                                        System.out.println("null");
                                        
                                    }
                                    else if(path.size() > 0){
                                        String mes = routing_tab.get(i)  + path.get(1).getId()+ " ";
                                        routing_tab.set(i, mes);
                                        System.out.println("the path from source " + i +" to destination " + j + "is: ");
                                        for (Vertex vertex : path) {
                                            System.out.print("Node: "+ vertex.getId());
                                            System.out.print(" ");   
                                        }
                                        System.out.println(""); 
                                    }
                                }
                            }
                         for(int i = 0; i < routing_tab.size(); i++) {
                            String ans = "3" + " ";
                            ans += routing_tab.get(i);
                            ans += "EOF";
                            System.out.println(ans);
                            DatagramPacket dp = new DatagramPacket(ans.getBytes() , ans.getBytes().length , InetAddress.getByName(hostAddress[i]) , Integer.valueOf(port[i]));
                             sock.send(dp);
                        }
                        count++; 
                           
                    }
                    
                }
                //////////////////////////////////////////////////////////////////////////////////////////////
            }
        }

        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
    }

    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
    }
}
