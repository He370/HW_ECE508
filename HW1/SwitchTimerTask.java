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

    System.out.println("[Sender]Sending..." + hostName);
    Set<Integer> ids = Switch.neighbors.keySet();

    String topologyUpdate = "4 "+ Integer.toString(Switch.switchID) + " ";
    for(Integer id : ids){
      int time = Switch.neighborStatus.get(id);
      if(time>=5){
        topologyUpdate = topologyUpdate + Integer.toString(id) + " 0 ";
      }
      else{
        topologyUpdate = topologyUpdate + Integer.toString(id) + " 1 ";
        Switch.neighborStatus.put(id,time+1);
      }
    }
    topologyUpdate = topologyUpdate + "EOF\n";

    try{
      System.out.println("[Sender]Sending TopologyUpdate to Controller!");
      System.out.println("[Sender]TopologyUpdate:" + topologyUpdate);
      InetAddress nodeHost = InetAddress.getByName(hostName);
      int nodePort = 5000;

      byte[] buffer = topologyUpdate.getBytes();
      DatagramPacket  dp = new DatagramPacket(buffer , buffer.length , nodeHost , nodePort);
      this.sender.send(dp);
    } catch (Exception e) {
        System.err.println("[Sender]Exception caught:" + e);
        System.exit(1);
    }


    for(Integer id : ids){
      try{
        ArrayList<String> nodeInfo = Switch.neighbors.get(id);

        if(nodeInfo.get(1).equals("NULL")==false){
          System.out.println("[Sender]Sending KEEP_ALIVE to node "+ Integer.toString(id) + " | " + nodeInfo.get(0));
          InetAddress nodeHost = InetAddress.getByName(nodeInfo.get(0));
          int nodePort = Integer.parseInt(nodeInfo.get(1));

          String keepAlive = "2 " + Integer.toString(Switch.switchID)  +" EOF\n";
          byte[] buffer = keepAlive.getBytes();
          DatagramPacket  dp = new DatagramPacket(buffer , buffer.length , nodeHost , nodePort);
          this.sender.send(dp);
        }
      } catch (Exception e) {
          System.err.println("[Sender]Exception caught:" + e);
          System.exit(1);
      }

    }
  }
}
