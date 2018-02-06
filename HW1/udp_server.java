import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Handler;
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
                	if(a == "0") {
                		all = "0";
                	}
                }
                if(count == 0 && all == "1") {
                	ArrayList<String> routing_tab = reader.read();
                	for(int i = 0; i < routing_tab.size(); i++) {
                		String ans = "3" + " ";
                		ans += routing_tab.get(i);
                		ans += "EOF";
                		DatagramPacket dp = new DatagramPacket(ans.getBytes() , ans.getBytes().length , InetAddress.getByName(hostAddress[i]) , Integer.valueOf(port[i]));
                        sock.send(dp);
                	}
                }
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
