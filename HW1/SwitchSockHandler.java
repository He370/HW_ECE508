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

class SwitchSockHandler extends Thread {
  DatagramSocket sock;
  SwitchSockHandler (DatagramSocket s) {
    this.sock = s;
  }

  public void run () {
    try {
      while (true) {
        if(Switch.logType==3||Switch.logType==4){
          System.out.println("[Receiver]Sock is ready to receive:");
        }
        byte[] packetBuffer = new byte[65536];
        DatagramPacket responsePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
        sock.receive(responsePacket);
        parseResponse(responsePacket);
      }
    }
    catch (Exception e) {
      System.err.println("[Receiver]Exception caught:" + e);
    }
  }

  private void parseResponse(DatagramPacket responsePacket){
    byte[] responseData = responsePacket.getData();
    String response= new String(responseData, 0, responsePacket.getLength());

    //System.out.println("[Receiver]Response:"+response);
    String[] words = response.split(" ");

    if(words[0].equals("2")){
      if(Switch.logType==3||Switch.logType==4){
        System.out.println("[Receiver]Receive KEEP_ALIVE from "+ words[1]);
      }
      int id = Integer.parseInt(words[1]);
      Switch.neighborStatus.put(id,0);
      ArrayList<String> nodeInfo = Switch.neighbors.get(id);
      nodeInfo.set(0,responsePacket.getAddress().getHostAddress());
      nodeInfo.set(1,Integer.toString(responsePacket.getPort()));
      Switch.neighbors.put(id,nodeInfo);
    }

    if(words[0].equals("3")){
      if(Switch.logType==1||Switch.logType==3){
        System.out.println("[Receiver]Receive ROUTE_UPDATE :"+ response);
      }
      int num = words.length-2;
      for(int i=1; i<num+1; i++){
        Switch.routeTable.put(i,Integer.parseInt(words[i]));
      }
    }

    if(words[0].equals("5")){
      int sourceID = Integer.parseInt(words[1]);
      int destID = Integer.parseInt(words[2]);
      if(destID==Switch.switchID){
        System.out.println ("[Console]:receive msg from node." + words[1] + " :" + words[3]);
      }
      else{
        int nextHop = Switch.routeTable.get(destID);
        if(nextHop==-1){
          System.out.println ("[Console]:Cannot find a way to send msg!");
        }
        else{
          try {
            ArrayList<String> nodeInfo = Switch.neighbors.get(nextHop);
            InetAddress nodeHost = InetAddress.getByName(nodeInfo.get(0));
            int nodePort = Integer.parseInt(nodeInfo.get(1));

            byte[] buffer = response.getBytes();
            DatagramPacket  dp = new DatagramPacket(buffer , buffer.length , nodeHost , nodePort);
            sock.send(dp);
            System.out.println ("[Console]:msg sent to hop:" + Integer.toString(nextHop));
          }
          catch (Exception e) {
            System.err.println("[Receiver]Exception caught:" + e);
          }
        }
      }
    }
  }

}
