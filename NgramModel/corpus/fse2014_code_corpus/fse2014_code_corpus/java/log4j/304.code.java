package org.apache.log4j.defaultInit;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import java.util.Enumeration;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
public class TestCase3 extends TestCase {
  public TestCase3(String name) {
    super(name);
  }
  public void setUp() {
  }
  public void tearDown() {
    LogManager.shutdown();
  }
  public void propertiesTest() {
    Logger root = Logger.getRootLogger();
    boolean rootIsConfigured = root.getAllAppenders().hasMoreElements();
    assertTrue(rootIsConfigured);
    Enumeration e = root.getAllAppenders();
    Appender appender = (Appender) e.nextElement();
    assertEquals(appender.getName(), "D3");
  }
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new TestCase3("propertiesTest"));
    return suite;
  }
}
