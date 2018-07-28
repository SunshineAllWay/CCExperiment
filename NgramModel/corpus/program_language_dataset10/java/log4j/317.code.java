package org.apache.log4j.net;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import junit.framework.TestCase;
public class TelnetAppenderTest extends TestCase {
  int port = 54353;
  ByteArrayOutputStream bo = new ByteArrayOutputStream();
  public class ReadThread extends Thread {
    public void run() {
      try {
        Socket s = new Socket("localhost", port);
        InputStream i = s.getInputStream();
        while (!Thread.interrupted()) {
          int c = i.read();
          if (c == -1)
            break;
          bo.write(c);
        }
        s.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  public void testIt() throws Exception {
    int oldActive = Thread.activeCount();
    TelnetAppender ta = new TelnetAppender();
    ta.setName("ta");
    ta.setPort(port);
    ta.setLayout(new PatternLayout("%p - %m"));
    ta.activateOptions();
    Logger l = Logger.getLogger("x");
    l.addAppender(ta);
    Thread t = new ReadThread();
    t.start();
    Thread.sleep(200);
    l.info("hi");
    Thread.sleep(1000);
    ta.close();
    Thread.sleep(200);
    t.interrupt();
    t.join();
    String s = bo.toString();
    assertTrue(s.endsWith("INFO - hi"));
    if(System.getProperty("java.vendor").indexOf("Free") == -1) {
        assertEquals(oldActive, Thread.activeCount());
    }
  }
}
