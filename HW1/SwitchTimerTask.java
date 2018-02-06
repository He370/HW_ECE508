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


class SwitchTimerTask extends TimerTask {
  DatagramSocket sender;
  String hostName = Switch.controllerHostname;
  SwitchTimerTask(DatagramSocket s) {
      this.sender = s;
  }
  public void run() {

    System.out.println("Sending..." + hostName);
    Set<Integer> ids = Switch.neighbors.keySet();
    for(Integer id : ids){
      try{
        ArrayList<String> nodeInfo = Switch.neighbors.get(id);

        if(!nodeInfo.get(1).equals("NULL")){
          System.out.println("Sending KEEP_ALIVE to node "+ nodeInfo.get(0) + " | " + nodeInfo.get(1));

          InetAddress nodeHost = InetAddress.getByName(nodeInfo.get(1));
          int nodePort = Integer.parseInt(nodeInfo.get(2));

          String keepAlive = "2 " + Integer.toString(Switch.switchID) + " EOF\n";
          byte[] buffer = keepAlive.getBytes();
          DatagramPacket  dp = new DatagramPacket(buffer , buffer.length , nodeHost , nodePort);
          this.sender.send(dp);
        }

      } catch (Exception e) {
          System.err.println("Exception caught:" + e);
          System.exit(1);
      }

    }
  }
}
