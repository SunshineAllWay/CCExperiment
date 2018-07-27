package org.apache.log4j.net;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;
public class SocketNode implements Runnable {
  Socket socket;
  LoggerRepository hierarchy;
  ObjectInputStream ois;
  static Logger logger = Logger.getLogger(SocketNode.class);
  public SocketNode(Socket socket, LoggerRepository hierarchy) {
    this.socket = socket;
    this.hierarchy = hierarchy;
    try {
      ois = new ObjectInputStream(
                         new BufferedInputStream(socket.getInputStream()));
    } catch(InterruptedIOException e) {
      Thread.currentThread().interrupt();
      logger.error("Could not open ObjectInputStream to "+socket, e);
    } catch(IOException e) {
      logger.error("Could not open ObjectInputStream to "+socket, e);
    } catch(RuntimeException e) {
      logger.error("Could not open ObjectInputStream to "+socket, e);
    }
  }
  public void run() {
    LoggingEvent event;
    Logger remoteLogger;
    try {
      if (ois != null) {
          while(true) {
	        event = (LoggingEvent) ois.readObject();
	        remoteLogger = hierarchy.getLogger(event.getLoggerName());
	        if(event.getLevel().isGreaterOrEqual(remoteLogger.getEffectiveLevel())) {
	        remoteLogger.callAppenders(event);
	      }
        }
      }
    } catch(java.io.EOFException e) {
      logger.info("Caught java.io.EOFException closing conneciton.");
    } catch(java.net.SocketException e) {
      logger.info("Caught java.net.SocketException closing conneciton.");
    } catch(InterruptedIOException e) {
      Thread.currentThread().interrupt();
      logger.info("Caught java.io.InterruptedIOException: "+e);
      logger.info("Closing connection.");
    } catch(IOException e) {
      logger.info("Caught java.io.IOException: "+e);
      logger.info("Closing connection.");
    } catch(Exception e) {
      logger.error("Unexpected exception. Closing conneciton.", e);
    } finally {
      if (ois != null) {
         try {
            ois.close();
         } catch(Exception e) {
            logger.info("Could not close connection.", e);
         }
      }
      if (socket != null) {
        try {
          socket.close();
        } catch(InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch(IOException ex) {
        }
      }
    }
  }
}
