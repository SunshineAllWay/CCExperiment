package org.apache.log4j.helpers;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.MDC;
import org.apache.log4j.util.Compare;
public class PatternParserTestCase extends TestCase {
  static String OUTPUT_FILE   = "output/PatternParser";
  static String WITNESS_FILE  = "witness/PatternParser";
  static String msgPattern = "%m%n";
  Logger root; 
  Logger logger;
  public PatternParserTestCase(String name) {
    super(name);
  }
  public void setUp() {
    root = Logger.getRootLogger();
    root.removeAllAppenders();
  }
  public void tearDown() {  
    root.getLoggerRepository().resetConfiguration();
  }
  public void mdcPattern() throws Exception {
    String mdcMsgPattern1 = "%m : %X%n";
    String mdcMsgPattern2 = "%m : %X{key1}%n";
    String mdcMsgPattern3 = "%m : %X{key2}%n";
    String mdcMsgPattern4 = "%m : %X{key3}%n";
    String mdcMsgPattern5 = "%m : %X{key1},%X{key2},%X{key3}%n";
    PatternLayout layout = new PatternLayout(msgPattern);
    Appender appender = new FileAppender(layout, OUTPUT_FILE+"_mdc", false);
    root.addAppender(appender);
    root.setLevel(Level.DEBUG);
    root.debug("starting mdc pattern test");
    layout.setConversionPattern(mdcMsgPattern1);
    root.debug("empty mdc, no key specified in pattern");
    layout.setConversionPattern(mdcMsgPattern2);
    root.debug("empty mdc, key1 in pattern");
    layout.setConversionPattern(mdcMsgPattern3);
    root.debug("empty mdc, key2 in pattern");
    layout.setConversionPattern(mdcMsgPattern4);
    root.debug("empty mdc, key3 in pattern");
    layout.setConversionPattern(mdcMsgPattern5);
    root.debug("empty mdc, key1, key2, and key3 in pattern");
    MDC.put("key1", "value1");
    MDC.put("key2", "value2");
    layout.setConversionPattern(mdcMsgPattern1);
    root.debug("filled mdc, no key specified in pattern");
    layout.setConversionPattern(mdcMsgPattern2);
    root.debug("filled mdc, key1 in pattern");
    layout.setConversionPattern(mdcMsgPattern3);
    root.debug("filled mdc, key2 in pattern");
    layout.setConversionPattern(mdcMsgPattern4);
    root.debug("filled mdc, key3 in pattern");
    layout.setConversionPattern(mdcMsgPattern5);
    root.debug("filled mdc, key1, key2, and key3 in pattern");
    MDC.remove("key1");
    MDC.remove("key2");
    layout.setConversionPattern(msgPattern);
    root.debug("finished mdc pattern test");
    assertTrue(Compare.compare(OUTPUT_FILE+"_mdc", WITNESS_FILE+"_mdc"));
  }
  public static Test suite() {
    TestSuite suite = new TestSuite();
    if (!System.getProperty("java.version").startsWith("1.1.")) {
       suite.addTest(new PatternParserTestCase("mdcPattern"));
    }
    return suite;
  }
}
