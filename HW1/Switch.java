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

public class Switch {
   private static int activeFlag = 1;
   private static int switchID;
   private static int switchPort;

   private static DatagramSocket receiverSock = null;
   private static DatagramSocket senderSock = null;

   private static String controllerHostname;

   private static Hashtable<Integer, ArrayList<String>> neighbors = new Hashtable<Integer, ArrayList<String>>();

   private static Timer timer;
   private static long period = 2*1000;

   public static void main(String []args) throws Exception{
     switchID = Integer.parseInt(args[0]);
     switchPort = Integer.parseInt(args[1]);

     System.out.println("Switch initiating! ID:" + Integer.toString(switchID));
     System.out.println("Port:" + Integer.toString(switchPort));

     controllerHostname = new String ("127.0.0.1");
     System.out.println ("Attemping to connect to controller " +
 controllerHostname + " on port 5000.");

     try {
       receiverSock =  new DatagramSocket(switchPort);
       senderSock =  new DatagramSocket();

       InetAddress controllerHost = InetAddress.getByName(controllerHostname);

       timer = new Timer();
       Calendar calendar= Calendar.getInstance();
       Date startTime = calendar.getTime();
       timer.schedule(new SwitchTimerTask(receiverSock,senderSock), startTime, period);

       // must be registered before everything
       int switchRegistered = 0;
       while(switchRegistered!=1){
         try{
           // request look like: "id 127.0.0.1 2000 1 EOF/n" <-- id, host, port, flag(live or not?)
           String registerRequest = "0 " + Integer.toString(switchID)+" "+"127.0.0.1"+" "+Integer.toString(switchPort)+" "+Integer.toString(activeFlag)+" EOF\n";
           byte[] buffer = registerRequest.getBytes();
           DatagramPacket  dp = new DatagramPacket(buffer , buffer.length , controllerHost , 5000);
           senderSock.send(dp);

           byte[] packetBuffer = new byte[65536];
           DatagramPacket responsePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
           receiverSock.receive(responsePacket);

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

     // Timer Thread



     // Switch Console

     BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
     try
     {
         while(true)
         {
             //take input and send the packet
             System.out.println ("[Switch Console ID("+ Integer.toString(switchID) +")]>>");
             String command = (String)cin.readLine();

             //echo the details of incoming data - client ip : client port - client message
             System.out.println ("[Switch Console ID("+ Integer.toString(switchID) +")]>>");
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

     if(words[0]!="0"||(words.length-2)%4!=0){
       System.out.println ("Invalid REGISTER_RESPONSE string!");
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

class SwitchTimerTask extends TimerTask {
  DatagramSocket receiver;
  DatagramSocket sender;
  SwitchTimerTask(DatagramSocket s1,DatagramSocket s2) {
      this.receiver = s1;
      this.sender = s2;
  }
  public void run() {
    System.out.println("Working...");
  }
}
