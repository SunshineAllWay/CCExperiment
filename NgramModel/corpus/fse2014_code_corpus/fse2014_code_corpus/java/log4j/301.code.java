package org.apache.log4j.customLogger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.util.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
public class XLoggerTestCase extends TestCase {
  static String FILTERED = "output/filtered";
  static XLogger logger = (XLogger) XLogger.getLogger(XLoggerTestCase.class);
  public XLoggerTestCase(String name){
    super(name);
  }
  public void tearDown() {
    logger.getLoggerRepository().resetConfiguration();
  }
  public void test1()  throws Exception  { common(1); }
  public void test2()  throws Exception  { common(2); }
  void common(int number) throws Exception {
    DOMConfigurator.configure("input/xml/customLogger"+number+".xml");
    int i = -1;
    logger.trace("Message " + ++i);
    logger.debug("Message " + ++i);
    logger.warn ("Message " + ++i);
    logger.error("Message " + ++i);
    logger.fatal("Message " + ++i);
    Exception e = new Exception("Just testing");
    logger.debug("Message " + ++i, e);
    Transformer.transform(
      "output/temp", FILTERED,
      new Filter[] {
        new LineNumberFilter(), new SunReflectFilter(),
        new JunitTestRunnerFilter()
      });
    assertTrue(Compare.compare(FILTERED, "witness/customLogger."+number));
  }
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new XLoggerTestCase("test1"));
    suite.addTest(new XLoggerTestCase("test2"));
    return suite;
  }
}
