package com.systemsunion.LoggingServer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.apache.log4j.Category;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Priority;
import org.apache.log4j.NDC;
public class SocketNode2 implements Runnable {
  Socket socket;
  ObjectInputStream ois;
  static Category cat = Category.getInstance(SocketNode2.class.getName());
  public
  SocketNode2(Socket socket) {
	this.socket = socket;
	try {
	  ois = new ObjectInputStream(socket.getInputStream());
	}
	catch(Exception e) {
	  cat.error("Could not open ObjectInputStream to "+socket, e);
	}
  }
  public void run() {
	LoggingEvent event;
	Category remoteCategory;
	String strClientName;
	InetAddress addr = socket.getInetAddress();
	strClientName = addr.getHostName();
	if(strClientName == null || strClientName.length() == 0)
	{
		strClientName = addr.getHostAddress();
	}
	try {
	  while(true) {
	event = (LoggingEvent) ois.readObject();
	if(event.ndc != null)
	{
		event.ndc = strClientName + ":" + event.ndc;
	}
	else
	{
		event.ndc = strClientName;
	}
	remoteCategory = Category.getInstance(event.categoryName);
	remoteCategory.callAppenders(event);
	  }
	}
	catch(java.io.EOFException e) {
	  cat.info("Caught java.io.EOFException will close conneciton.", e);
	}
	catch(java.net.SocketException e) {
	  cat.info("Caught java.net.SocketException, will close conneciton.", e);
	}
	catch(Exception e) {
	  cat.error("Unexpected exception. Closing conneciton.", e);
	}
	try {
	  ois.close();
	}
	catch(Exception e) {
	  cat.info("Could not close connection.", e);
	}
  }
}
