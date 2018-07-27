package org.apache.log4j.varia;
import junit.framework.TestCase;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RFATestCase;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
public class ERFATestCase extends TestCase {
  public ERFATestCase(final String name) {
    super(name);
  }
  public void tearDown() {
      LogManager.resetConfiguration();
  }
  public void testConstructor() {
      ExternallyRolledFileAppender appender =
              new ExternallyRolledFileAppender();
      assertEquals(0, appender.getPort());
  }
  void sendMessage(int port, final String msg, final String expectedResponse) throws IOException {
      Socket socket = new Socket((String) null, port);
      DataInputStream reader = new DataInputStream(socket.getInputStream());
      DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
      writer.writeUTF(msg);
      String response = reader.readUTF();
      assertEquals(expectedResponse, response);
      reader.close();
      writer.close();
      socket.close();
  }
  public void testRollover() throws IOException {
      ExternallyRolledFileAppender erfa =
              new ExternallyRolledFileAppender();
      int port = 5500;
      Logger logger = Logger.getLogger(RFATestCase.class);
      Logger root = Logger.getRootLogger();
      PatternLayout layout = new PatternLayout("%m\n");
      erfa.setLayout(layout);
      erfa.setAppend(false);
      erfa.setMaxBackupIndex(2);
      erfa.setPort(port);
      erfa.setFile("output/ERFA-test2.log");
      try {
        erfa.activateOptions();
      } catch(SecurityException ex) {
          return;
      }
      try {
         Thread.sleep(100);
      } catch(InterruptedException ex) {
      }
      root.addAppender(erfa);
      for (int i = 0; i < 55; i++) {
        if (i < 10) {
          logger.debug("Hello---" + i);
        } else if (i < 100) {
          logger.debug("Hello--" + i);
        }
        if ((i % 10) == 9) {
            try {
                sendMessage(port, "RollOver", "OK");
            } catch(SecurityException ex) {
                return;
            }
        }
      }
      try {
        sendMessage(port,
              "That's all folks.",
              "Expecting [RollOver] string.");
      } catch(SecurityException ex) {
          return;
      }
      assertTrue(new File("output/ERFA-test2.log").exists());
      assertTrue(new File("output/ERFA-test2.log.1").exists());
      assertTrue(new File("output/ERFA-test2.log.2").exists());
      assertFalse(new File("output/ERFA-test2.log.3").exists());
  }
}
