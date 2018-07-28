package org.apache.log4j.helpers;
import junit.framework.TestCase;
public class LogLogTest extends TestCase {
  public LogLogTest(final String testName) {
    super(testName);
  }
  public void testDebugKey() {
    assertEquals("log4j.debug", LogLog.DEBUG_KEY);
  }
  public void testConfigDebugKey() {
    assertEquals("log4j.configDebug", LogLog.CONFIG_DEBUG_KEY);
  }
}
