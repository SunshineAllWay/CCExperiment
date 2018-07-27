package org.apache.log4j.nt;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;
import org.apache.log4j.BasicConfigurator;
public class NTEventLogAppenderTest extends TestCase {
  public void tearDown() {
    LogManager.shutdown();
  }
  public void testSimple() {
    BasicConfigurator.configure(new NTEventLogAppender());
    Logger logger = Logger.getLogger("org.apache.log4j.nt.NTEventLogAppenderTest");
    int i  = 0;
    logger.debug( "Message " + i++);
    logger.info( "Message " + i++);
    logger.warn( "Message " + i++);
    logger.error( "Message " + i++);
    logger.log(Level.FATAL, "Message " + i++);
    logger.debug("Message " + i++,  new Exception("Just testing."));
  }
}
