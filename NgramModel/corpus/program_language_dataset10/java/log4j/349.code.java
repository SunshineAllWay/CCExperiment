package org.apache.log4j.xml;
import junit.framework.TestCase;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.VectorAppender;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableRenderer;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.ThrowableRendererSupport;
import org.apache.log4j.util.Compare;
import org.apache.log4j.util.ControlFilter;
import org.apache.log4j.util.Filter;
import org.apache.log4j.util.ISO8601Filter;
import org.apache.log4j.util.JunitTestRunnerFilter;
import org.apache.log4j.util.LineNumberFilter;
import org.apache.log4j.util.SunReflectFilter;
import org.apache.log4j.util.Transformer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
public class DOMTestCase extends TestCase {
  static String TEMP_A1 = "output/temp.A1";
  static String TEMP_A2 = "output/temp.A2";
  static String FILTERED_A1 = "output/filtered.A1";
  static String FILTERED_A2 = "output/filtered.A2";
  static String EXCEPTION1 = "java.lang.Exception: Just testing";
  static String EXCEPTION2 = "\\s*at .*\\(.*\\)";
  static String EXCEPTION3 = "\\s*at .*\\(Native Method\\)";
  static String EXCEPTION4 = "\\s*at .*\\(.*Compiled Code\\)";
  static String EXCEPTION5 = "\\s*at .*\\(.*libgcj.*\\)";
  static String TEST1_1A_PAT = 
                       "(TRACE|DEBUG|INFO |WARN |ERROR|FATAL) \\w*\\.\\w* - Message \\d";
  static String TEST1_1B_PAT = "(TRACE|DEBUG|INFO |WARN |ERROR|FATAL) root - Message \\d";
  static String TEST1_2_PAT = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3} "+
                        "\\[main]\\ (TRACE|DEBUG|INFO|WARN|ERROR|FATAL) .* - Message \\d";
  Logger root; 
  Logger logger;
  public DOMTestCase(String name) {
    super(name);
  }
  public void setUp() {
    root = Logger.getRootLogger();
    logger = Logger.getLogger(DOMTestCase.class);
  }
  public void tearDown() {  
    root.getLoggerRepository().resetConfiguration();
  }
  public void test1() throws Exception {
    DOMConfigurator.configure("input/xml/DOMTestCase1.xml");
    common();
    ControlFilter cf1 = new ControlFilter(new String[]{TEST1_1A_PAT, TEST1_1B_PAT, 
					       EXCEPTION1, EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
    ControlFilter cf2 = new ControlFilter(new String[]{TEST1_2_PAT, 
					       EXCEPTION1, EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
    Transformer.transform(
      TEMP_A1, FILTERED_A1,
      new Filter[] {
        cf1, new LineNumberFilter(), new SunReflectFilter(),
        new JunitTestRunnerFilter()
      });
    Transformer.transform(
      TEMP_A2, FILTERED_A2,
      new Filter[] {
        cf2, new LineNumberFilter(), new ISO8601Filter(),
        new SunReflectFilter(), new JunitTestRunnerFilter()
      });
    assertTrue(Compare.compare(FILTERED_A1, "witness/dom.A1.1"));
    assertTrue(Compare.compare(FILTERED_A2, "witness/dom.A2.1"));
  }
  public void test4() throws Exception {
    DOMConfigurator.configure("input/xml/DOMTest4.xml");
    common();
    ControlFilter cf1 = new ControlFilter(new String[]{TEST1_1A_PAT, TEST1_1B_PAT, 
					       EXCEPTION1, EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
    ControlFilter cf2 = new ControlFilter(new String[]{TEST1_2_PAT, 
					       EXCEPTION1, EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
    Transformer.transform(
      TEMP_A1 + ".4", FILTERED_A1 + ".4",
      new Filter[] {
        cf1, new LineNumberFilter(), new SunReflectFilter(),
        new JunitTestRunnerFilter()
      });
    Transformer.transform(
      TEMP_A2 + ".4", FILTERED_A2 + ".4",
      new Filter[] {
        cf2, new LineNumberFilter(), new ISO8601Filter(),
        new SunReflectFilter(), new JunitTestRunnerFilter()
      });
    assertTrue(Compare.compare(FILTERED_A1 + ".4", "witness/dom.A1.4"));
    assertTrue(Compare.compare(FILTERED_A2 + ".4", "witness/dom.A2.4"));
  }
  void common() {
    String oldThreadName = Thread.currentThread().getName();
    Thread.currentThread().setName("main");
    int i = -1;
    logger.trace("Message " + ++i);
    root.trace("Message " + i);  
    logger.debug("Message " + ++i);
    root.debug("Message " + i);        
    logger.info ("Message " + ++i);
    root.info("Message " + i);        
    logger.warn ("Message " + ++i);
    root.warn("Message " + i);        
    logger.error("Message " + ++i);
    root.error("Message " + i);
    logger.log(Level.FATAL, "Message " + ++i);
    root.log(Level.FATAL, "Message " + i);    
    Exception e = new Exception("Just testing");
    logger.debug("Message " + ++i, e);
    root.debug("Message " + i, e);
    logger.error("Message " + ++i, e);
    root.error("Message " + i, e);    
    Thread.currentThread().setName(oldThreadName);
  }
  private static class CustomLogger extends Logger {
      public CustomLogger(final String name) {
          super(name);
      }
  }
  public static class CustomLoggerFactory implements LoggerFactory {
      private boolean additivity;
      public CustomLoggerFactory() {
          additivity = true;
      }
      public Logger makeNewLoggerInstance(final String name) {
          Logger logger = new CustomLogger(name);
          assertFalse(additivity);
          return logger;
      }
      public void setAdditivity(final boolean newVal) {
          additivity = newVal;
      }
  }
  public static class CustomErrorHandler implements ErrorHandler {
      public CustomErrorHandler() {}
      public void activateOptions() {}
      public void setLogger(final Logger logger) {}
      public void error(String message, Exception e, int errorCode) {}
      public void error(String message) {}
      public void error(String message, Exception e, int errorCode, LoggingEvent event) {}
      public void setAppender(Appender appender) {}
      public void setBackupAppender(Appender appender) {}
  }
  public void testCategoryFactory1() {
      DOMConfigurator.configure("input/xml/categoryfactory1.xml");
      Logger logger1 = Logger.getLogger("org.apache.log4j.xml.DOMTestCase.testCategoryFactory1.1");
      assertTrue(logger1 instanceof CustomLogger);
      Logger logger2 = Logger.getLogger("org.apache.log4j.xml.DOMTestCase.testCategoryFactory1.2");
      assertFalse(logger2 instanceof CustomLogger);
  }
    public void testCategoryFactory2() {
        DOMConfigurator.configure("input/xml/categoryfactory2.xml");
        Logger logger1 = Logger.getLogger("org.apache.log4j.xml.DOMTestCase.testCategoryFactory2.1");
        assertTrue(logger1 instanceof CustomLogger);
        Logger logger2 = Logger.getLogger("org.apache.log4j.xml.DOMTestCase.testCategoryFactory2.2");
        assertFalse(logger2 instanceof CustomLogger);
    }
  public void testLoggerFactory1() {
      DOMConfigurator.configure("input/xml/loggerfactory1.xml");
      Logger logger1 = Logger.getLogger("org.apache.log4j.xml.DOMTestCase.testLoggerFactory1.1");
      assertTrue(logger1 instanceof CustomLogger);
      Logger logger2 = Logger.getLogger("org.apache.log4j.xml.DOMTestCase.testLoggerFactory1.2");
      assertFalse(logger2 instanceof CustomLogger);
  }
  public void testReset() throws Exception {
      VectorAppender appender = new VectorAppender();
      appender.setName("V1");
      Logger.getRootLogger().addAppender(appender);
      DOMConfigurator.configure("input/xml/testReset.xml");
      assertNull(Logger.getRootLogger().getAppender("V1"));
  }
  public void testConfigureAndWatch() throws Exception {
    DOMConfigurator.configureAndWatch("input/xml/DOMTestCase1.xml");
    assertNotNull(Logger.getRootLogger().getAppender("A1"));
  }
  public void testOverrideSubst() {
      DOMConfigurator configurator = new DOMConfigurator() {
          protected String subst(final String value) {
              if ("output/temp.A1".equals(value)) {
                  return "output/subst-test.A1";
              }
              return value;
          }
      };
      configurator.doConfigure("input/xml/DOMTestCase1.xml", LogManager.getLoggerRepository());
      FileAppender a1 = (FileAppender) Logger.getRootLogger().getAppender("A1");
      String file = a1.getFile();
      assertEquals("output/subst-test.A1", file);
  }
    public static class MockThrowableRenderer implements ThrowableRenderer, OptionHandler {
        private boolean activated = false;
        private boolean showVersion = true;
        public MockThrowableRenderer() {
        }
        public void activateOptions() {
            activated = true;
        }
        public boolean isActivated() {
            return activated;
        }
        public String[] doRender(final Throwable t) {
            return new String[0];
        }
        public void setShowVersion(boolean v) {
            showVersion = v;
        }
        public boolean getShowVersion() {
            return showVersion;
        }
    }
    public void testThrowableRenderer1() {
        DOMConfigurator.configure("input/xml/throwableRenderer1.xml");
        ThrowableRendererSupport repo = (ThrowableRendererSupport) LogManager.getLoggerRepository();
        MockThrowableRenderer renderer = (MockThrowableRenderer) repo.getThrowableRenderer();
        LogManager.resetConfiguration();
        assertNotNull(renderer);
        assertEquals(true, renderer.isActivated());
        assertEquals(false, renderer.getShowVersion());
    }
    public void testJarURL() throws IOException {
        File input = new File("input/xml/defaultInit.xml");
        System.out.println(input.getAbsolutePath());
        InputStream is = new FileInputStream(input);
        File dir = new File("output");
        dir.mkdirs();
        File file = new File("output/xml.jar");
        ZipOutputStream zos =
            new ZipOutputStream(new FileOutputStream(file));
        zos.putNextEntry(new ZipEntry("log4j.xml"));
        int len;
        byte[] buf = new byte[1024];
        while ((len = is.read(buf)) > 0) {
            zos.write(buf, 0, len);
        }
        zos.closeEntry();
        zos.close();
        URL url = new URL("jar:" + file.toURL() + "!/log4j.xml");
        DOMConfigurator.configure(url);
        assertTrue(file.delete());
        assertFalse(file.exists());
    }
}
