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

        byte[] responseData = responsePacket.getData();
        String registerResponse= new String(responseData, 0, responsePacket.getLength());
        parseResponse(registerResponse);
      }
    }
    catch (Exception e) {
      System.err.println("Exception caught:" + e);
    }
  }

  private void parseResponse(String s){
    System.out.println("[Receiver]Response:"+s);
  }

}
