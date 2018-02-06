import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Controller {
		public static HashSet<Integer> id_set = new HashSet<>();
		public static HashSet<Message> register_switch = new HashSet<>();
	 public static void handleSocket(Socket socket) throws Exception {
   	  BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        StringBuilder sb = new StringBuilder();
	        String temp;
	        int index;
	        while ((temp=br.readLine()) != null) {
	            if ((index = temp.indexOf("EOF")) != -1) {
	               sb.append(temp.substring(0, index));
	               break;
	            }
	            sb.append(temp);
	        }
	        String result = sb.toString();
	        System.out.println(result);
	        // result look like: "id 127.1.1.0 2000 1" <-- id, host, port, flag(live or not?)
	        String[] words = result.split("\\s");
	        int id = Integer.valueOf(words[0]);
	        String host_name = words[1];
	        int port = Integer.valueOf(words[2]);
	        int flag = Integer.valueOf(words[3]);
	        if(!id_set.contains(id)){
	       	 Message switch_message = new Message(id, host_name, port, flag);
	       	 id_set.add(id);
	       	 register_switch.add(switch_message);
	        }
	        Writer writer = new OutputStreamWriter(socket.getOutputStream());
	        StringBuilder sb1 = new StringBuilder();
	        	for(Message node: register_switch) {
	        		if(node.id != id && (register_switch.size() >= 1)) {
	        			sb1.append(String.valueOf(node.id));
	            		sb1.append(' ');
	            		sb1.append(node.host);
	            		sb1.append(' ');
	            		sb1.append(String.valueOf(node.port));
	            		sb1.append(' ');
	            		sb1.append(String.valueOf(node.flag));
	            		sb1.append(' ');
	        		}
	        	}
	        String register_respond = sb1.toString();
	        System.out.println("Responding message from server is:" + register_respond);
	       	 writer.write(register_respond);
	            writer.write("EOF\n");
		         writer.flush();
		         writer.close();
		         br.close();
		         socket.close();
     }



   public static void main(String args[]) throws Exception {
	   int port = 5000;
	   int timeout = 100000;
     ServerSocket server = new ServerSocket(port);
     System.out.println("server" + server.getLocalPort() + "isWorking!");
      while (true) {
         //server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
         Socket socket = server.accept();
         //每接收到一个Socket就建立一个新的线程来处理它
         //new Thread(new Task(socket)).start();
         handleSocket(socket);
      }
   }
}
