import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Switch {
   static int activeFlag = 1;
   static int switchID;
   static int switchPort;

   static Hashtable<Integer, ArrayList<String>> neighbors = new Hashtable<Integer, ArrayList<String>>();

   public static void main(String []args){
     switchID = Integer.parseInt(args[0]);
     switchPort = Integer.parseInt(args[1]);

     System.out.println("Switch initiated! ID:" + Integer.toString(switchID));
     System.out.println("Port:" + Integer.toString(switchPort));

     String controllerHostname = new String ("127.0.0.1");
     System.out.println ("Attemping to connect to controller " +
 controllerHostname + " on port 5000.");

     Socket controllerSocket = null;
     PrintWriter controllerOut = null;
     BufferedReader controllerIn = null;

     try {
         controllerSocket = new Socket(controllerHostname, 5000);
         controllerOut = new PrintWriter(controllerSocket.getOutputStream(), true);
         controllerIn = new BufferedReader(new InputStreamReader(
                                     controllerSocket.getInputStream()));

     } catch (UnknownHostException e) {
         System.err.println("Don't know about controller: " + controllerHostname);
         System.exit(1);
     } catch (IOException e) {
         System.err.println("Couldn't get IO for "
                            + "the connection to: " + controllerHostname);
         System.exit(1);
     }

     // must be registered before added into network
     int switchRegistered = 0;
     // request look like: "id 127.0.0.1 2000 1" <-- id, host, port, flag(live or not?)
     // TODO: Keep sending request if there is no response from controller
     controllerOut.write( Integer.toString(switchID) + " " + "127.0.0.1" +  " "
                        + Integer.toString(switchPort) + " " + Integer.toString(activeFlag));
     while(switchRegistered!=1){
       try{
         String line = controllerIn.readLine();
         if(processReceiveResponse(line)){
           switchRegistered = 1;
         }
       } catch (Exception e) {
           System.err.println("Exception caught:" + e);
       }
     }

     // switch registered!
     System.out.println ("Switch " + Integer.toString(switchID) + " registered to controller " +
 controllerHostname);
   }

   // one neighbor format: "id1 127.0.0.1 2000 1 id2 ....."
   public static boolean processReceiveResponse(String response) throws IOException {
     String[] words = response.split(" ");
     if(words.length%4!=0){
       return false;
     }

     for(int i = 0; i < words.length/4 ; i++){
       String id = words[i*4+0];
       ArrayList<String> nodeInfo = new ArrayList<String>(3);
       nodeInfo.set(0,words[i*4+1]);
       nodeInfo.set(1,words[i*4+2]);
       nodeInfo.set(2,words[i*4+3]);
       neighbors.put(Integer.parseInt(id), nodeInfo);
     }

     return true;
   }
}
