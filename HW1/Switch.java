import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.lang.Thread;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class Switch {
   private static int activeFlag = 1;
   public static int switchID;
   public static int switchPort;

   private static DatagramSocket udpSock = null;
   //private static DatagramSocket senderSock = null;

   public static String controllerHostname;

   public static Hashtable<Integer, ArrayList<String>> neighbors = new Hashtable<Integer, ArrayList<String>>();
   public static Hashtable<Integer, Integer> neighborStatus = new Hashtable<Integer, Integer>();
   public static Hashtable<Integer, Integer> blockStatus = new Hashtable<Integer, Integer>();
   public static Hashtable<Integer, Integer> routeTable = new Hashtable<Integer, Integer>();

   private static Timer timer;
   private static long period = 2*1000;

   public static int logType = 0;
   /* logType:
   0 : Console mode
   1 : Concise mode(RouteUpdate)
   2 : Sender mode
   3 : Receiver mode
   4 : All mode(with KEEP_ALIVE)
   */

   public static void main(String []args) throws Exception{
     switchID = Integer.parseInt(args[0]);
     switchPort = Integer.parseInt(args[1]);

     System.out.println("Switch initiating! ID:" + Integer.toString(switchID));
     System.out.println("Port:" + Integer.toString(switchPort));

     controllerHostname = new String ("127.0.0.1");
     System.out.println ("[Register]Attemping to connect to controller " + controllerHostname + " on port 5000.");

     try {
       udpSock =  new DatagramSocket(switchPort);
       //senderSock =  new DatagramSocket();

       InetAddress controllerHost = InetAddress.getByName(controllerHostname);

       // must be registered before everything
       int switchRegistered = 0;
       while(switchRegistered!=1){
         try{
           // request look like: "0 id 127.0.0.1 2000 1 EOF/n" <-- id, host, port, flag(live or not?)
           String registerRequest = "0 " + Integer.toString(switchID)+" "+"127.0.0.1"+" "+Integer.toString(switchPort)+" "+Integer.toString(activeFlag)+" EOF\n";
           byte[] buffer = registerRequest.getBytes();
           DatagramPacket  dp = new DatagramPacket(buffer , buffer.length , controllerHost , 5000);
           udpSock.send(dp);

           byte[] packetBuffer = new byte[65536];
           DatagramPacket responsePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
           udpSock.receive(responsePacket);

           byte[] responseData = responsePacket.getData();
           String registerResponse= new String(responseData, 0, responsePacket.getLength());
           if(processReceiveResponse(registerResponse)==true){
             switchRegistered = 1;
           }
         } catch (Exception e) {
             System.err.println("[Register]Exception caught:" + e);
             System.exit(1);
         }
       }

     } catch (IOException e) {
         System.err.println("[Register]IOException " + e);
         System.exit(1);
     }

     // switch registered!
     System.out.println ("[Register]Switch " + Integer.toString(switchID) + " registered to controller " + controllerHostname);

     // Timer Thread

     timer = new Timer();
     Calendar calendar= Calendar.getInstance();
     Date startTime = calendar.getTime();
     timer.schedule(new SwitchTimerTask(udpSock), startTime, period);

     // Receive Thread

     SwitchSockHandler receiveHandler = new SwitchSockHandler(udpSock);
     receiveHandler.start();

     // Switch Console

     BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
     try
     {
         while(true)
         {
             //take input and send the packet
             System.out.println ("[Console][ID("+ Integer.toString(switchID) +")]>>");
             String command = (String)cin.readLine();
             String[] words = command.split(" ");

             // change log type
             if(words[0].equals("m")){
               int mode = Integer.parseInt(words[1]);
               if(mode<0||mode>4){
                 System.out.println ("[Console]:Invalid Input");
               }
               else{
                 System.out.println ("[Console]:Switch to mode:" + words[1]);
                 logType = mode;
               }
             }

             // block link to ID
             if(words[0].equals("b")){
               int blockID = Integer.parseInt(words[1]);
               blockStatus.put(blockID,0);
               System.out.println ("[Console]:Blocked link to " + words[1]);
             }

             // print tables:

             if(words[0].equals("p")){
               if(words[1].equals("rt")){
                 printRoutingTable();
               }
               if(words[1].equals("nt")){
                 printNeighbors();
               }
             }

             //echo the details of incoming data - client ip : client port - client message
             //System.out.println ("[ConsoleID("+ Integer.toString(switchID) +")]>>");
         }
     }

     catch(IOException e)
     {
         System.err.println("[Console]IOException " + e);
     }

   }

   private static void printNeighbors(){
     Set<Integer> ids = neighbors.keySet();
     int i = 1;
     for(Integer id : ids){
       ArrayList<String> nodeInfo = neighbors.get(id);
       System.out.println ("Neighbor"+ Integer.toString(i)+": ID:" + Integer.toString(id) + " Name:"+ nodeInfo.get(0) + " Port:" + nodeInfo.get(1)
                              + " Status:" + Integer.toString(neighborStatus.get(id)) + " Blocked:" + Integer.toString(blockStatus.get(id)));
       i += 1;
     }
   }

   private static void printRoutingTable(){
     System.out.println ("Next Node | Dest Node");
     Set<Integer> ids = routeTable.keySet();
     for(Integer id : ids){
       System.out.println ( Integer.toString(routeTable.get(id)) +  "  |  " + Integer.toString(id));
     }
   }

   // neighbors format: "1 id1 127.0.0.1 2000 1 id2 ..... EOF/n"
   public static boolean processReceiveResponse(String response) throws IOException {
     String[] words = response.split(" ");

     System.out.println (response);

     if((!words[0].equals("1"))||(words.length-2)%4!=0){
       System.out.println ("[Register]Invalid REGISTER_RESPONSE string!");
       return false;
     }

     for(int i = 0; i < (words.length-2)/4 ; i+=1){
       String id = words[1+i*4];
       ArrayList<String> nodeInfo = new ArrayList<String>();
       System.out.println ("[Register]Neighbor"+ Integer.toString(i)+": ID:" + words[1+i*4] + " Name:"
                          + words[1+(i*4)+1] + " Port:" + words[1+(i*4)+2]);
       nodeInfo.add(0,words[1+(i*4)+1]);
       nodeInfo.add(1,words[1+(i*4)+2]);
       nodeInfo.add(2,words[1+(i*4)+3]);
       neighbors.put(Integer.parseInt(id), nodeInfo);
       blockStatus.put(Integer.parseInt(id), 1);

       if(words[1+(i*4)+3].equals("0")){
         neighborStatus.put(Integer.parseInt(id), 5);
       }
       else{
         neighborStatus.put(Integer.parseInt(id), 0);
       }
     }

     return true;
   }
}
