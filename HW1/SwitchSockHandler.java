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
        System.out.println("[Receiver]Sock is ready to receive:");
        byte[] packetBuffer = new byte[65536];
        DatagramPacket responsePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
        sock.receive(responsePacket);
        parseResponse(responsePacket);
      }
    }
    catch (Exception e) {
      System.err.println("Exception caught:" + e);
    }
  }

  private void parseResponse(DatagramPacket responsePacket){
    byte[] responseData = responsePacket.getData();
    String registerResponse= new String(responseData, 0, responsePacket.getLength());

    System.out.println("[Receiver]Response:"+registerResponse);
    String[] words = registerResponse.split(" ");

    if(words[0].equals("2")){
      System.out.println("[Receiver]Receive KEEP_ALIVE from "+ words[1]);
      int id = Integer.parseInt(words[1]);
      Switch.neighborStatus.put(id,0);
      ArrayList<String> nodeInfo = Switch.neighbors.get(id);
      nodeInfo.set(0,responsePacket.getAddress().getHostAddress());
      nodeInfo.set(1,Integer.toString(responsePacket.getPort()));
      Switch.neighbors.put(id,nodeInfo);
    }
    else if(words[1].equals("3")){

    }
  }

}
