// Name: Lindun He
// email:he370@purdue.edu
// Description: This file is a basic switch implementation for registration.
//              It sends a REGISTER_REQUEST to Controller when it is initiated.
//              Then it parses the REGISTER_RESPONSE, which is a list of neighbors,
//              from controller and stores neighbors' info in hashtable.
// Example Input: $java Switch 6 2006
// Console Output:
//    Switch initiated! ID:6
//    Port:2006
//    Attemping to connect to controller 127.0.0.1 on port 5000.
//    Neighbor0: ID:5 Name:127.0.0.1 Port:2005
//    Neighbor1: ID:3 Name:127.0.0.1 Port:2002
//    Neighbor2: ID:1 Name:127.0.0.1 Port:2000
//    Neighbor3: ID:4 Name:127.0.0.1 Port:2004
//    Neighbor4: ID:2 Name:127.0.0.1 Port:2001
//    Switch 6 registered to controller 127.0.0.1
//

import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Switch {
   private static int activeFlag = 1;
   private static int switchID;
   private static int switchPort;

   private static DatagramSock receiverSock = null;
   private static DatagramSock senderSock = null;

   private static String controllerHostname;

   private static Hashtable<Integer, ArrayList<String>> neighbors = new Hashtable<Integer, ArrayList<String>>();

   public static void main(String []args) throws Exception{
     switchID = Integer.parseInt(args[0]);
     switchPort = Integer.parseInt(args[1]);

     System.out.println("Switch initiated! ID:" + Integer.toString(switchID));
     System.out.println("Port:" + Integer.toString(switchPort));

     controllerHostname = new String ("127.0.0.1");
     System.out.println ("Attemping to connect to controller " +
 controllerHostname + " on port 5000.");

     try {
       receiverSock =  new DatagramSocket(switchPort);
       senderSock =  new DatagramSocket();

       InetAddress controllerHost = InetAddress.getByName(controllerHostname);

       // must be registered before everything
       int switchRegistered = 0;
       while(switchRegistered!=1){
         try{
           // request look like: "id 127.0.0.1 2000 1 EOF/n" <-- id, host, port, flag(live or not?)
           String registerRequest = Integer.toString(switchID)+" "+"127.0.0.1"+" "+Integer.toString(switchPort)+" "+Integer.toString(activeFlag)+" EOF\n";
           byte[] buffer = registerRequest.getBytes();
           DatagramPacket  dp = new DatagramPacket(buffer , buffer.length , controllerHost , 5000);
           senderSock.send(dp);

           byte[] packetBuffer = new byte[65536];
           DatagramPacket responsePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
           sock.receive(responsePacket);

           byte[] responseData = responsePacket.getData();
           String registerResponse= new String(responseData, 0, responsePacket.getLength());
           if(processReceiveResponse(registerResponse)==true){
             switchRegistered = 1;
           }
         } catch (Exception e) {
             System.err.println("Exception caught:" + e);
             System.exit(1);
         }
       }

     } catch (IOException e) {
         System.err.println("IOException " + e);
         System.exit(1);
     }

     // switch registered!
     System.out.println ("Switch " + Integer.toString(switchID) + " registered to controller " + controllerHostname);

     BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
     try
     {
         while(true)
         {
             //take input and send the packet
             System.out.println ("[Switch Console ID("+ Integer.toString(switchID) +")]>>");
             String command = (String)cin.readLine();

             //echo the details of incoming data - client ip : client port - client message
             System.out.println ("[Switch Console ID("+ Integer.toString(switchID) +")]>>");s
         }
     }

     catch(IOException e)
     {
         System.err.println("IOException " + e);
     }

   }

   // neighbors format: "id1 127.0.0.1 2000 1 id2 ..... EOF/n"
   public static boolean processReceiveResponse(String response) throws IOException {
     String[] words = response.split(" ");
     if((words.length-1)%4!=0){
       return false;
     }

     for(int i = 0; i < (words.length-1)/4 ; i+=1){
       String id = words[i*4];
       ArrayList<String> nodeInfo = new ArrayList<String>();
       System.out.println ("Neighbor"+ Integer.toString(i)+": ID:" + words[i*4] + " Name:"
                          + words[(i*4)+1] + " Port:" + words[(i*4)+2]);
       nodeInfo.add(0,words[(i*4)+1]);
       nodeInfo.add(1,words[(i*4)+2]);
       nodeInfo.add(2,words[(i*4)+3]);
       neighbors.put(Integer.parseInt(id), nodeInfo);
     }

     return true;
   }
}
