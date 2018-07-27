package org.apache.log4j.varia;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;
public class ExternallyRolledFileAppender extends RollingFileAppender {
  static final public String ROLL_OVER = "RollOver";
  static final public String OK = "OK";
  int port = 0;
  HUP hup;
  public
  ExternallyRolledFileAppender() {
  }
  public
  void setPort(int port) {
    this.port = port;
  }
  public
  int getPort() {
    return port;
  }
  public
  void activateOptions() {
    super.activateOptions();
    if(port != 0) {
      if(hup != null) {
	hup.interrupt();
      }
      hup = new HUP(this, port);
      hup.setDaemon(true);
      hup.start();
    }
  }
}
class HUP extends Thread {
  int port;
  ExternallyRolledFileAppender er;
  HUP(ExternallyRolledFileAppender er, int port) {
    this.er = er;
    this.port = port;
  }
  public
  void run() {
    while(!isInterrupted()) {
      try {
	ServerSocket serverSocket = new ServerSocket(port);
	while(true) {
	  Socket socket = serverSocket.accept();
	  LogLog.debug("Connected to client at " + socket.getInetAddress());
	  new Thread(new HUPNode(socket, er), "ExternallyRolledFileAppender-HUP").start();
	}
      } catch(InterruptedIOException e) {
        Thread.currentThread().interrupt();
	    e.printStackTrace();
      } catch(IOException e) {
	    e.printStackTrace();
      } catch(RuntimeException e) {
	    e.printStackTrace();
      }
    }
  }
}
class HUPNode implements Runnable {
  Socket socket;
  DataInputStream dis;
  DataOutputStream dos;
  ExternallyRolledFileAppender er;
  public
  HUPNode(Socket socket, ExternallyRolledFileAppender er) {
    this.socket = socket;
    this.er = er;
    try {
      dis = new DataInputStream(socket.getInputStream());
      dos = new DataOutputStream(socket.getOutputStream());
    } catch(InterruptedIOException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    } catch(RuntimeException e) {
      e.printStackTrace();
    }
  }
  public void run() {
    try {
      String line = dis.readUTF();
      LogLog.debug("Got external roll over signal.");
      if(ExternallyRolledFileAppender.ROLL_OVER.equals(line)) {
	synchronized(er) {
	  er.rollOver();
	}
	dos.writeUTF(ExternallyRolledFileAppender.OK);
      }
      else {
	dos.writeUTF("Expecting [RollOver] string.");
      }
      dos.close();
    } catch(InterruptedIOException e) {
      Thread.currentThread().interrupt();
      LogLog.error("Unexpected exception. Exiting HUPNode.", e);
    } catch(IOException e) {
      LogLog.error("Unexpected exception. Exiting HUPNode.", e);
    } catch(RuntimeException e) {
      LogLog.error("Unexpected exception. Exiting HUPNode.", e);
    }
  }
}
