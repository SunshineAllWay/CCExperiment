package org.apache.log4j;
import junit.framework.TestCase;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class RFATestCase extends TestCase {
  public RFATestCase(String name) {
    super(name);
  }
  public void tearDown() {
      LogManager.resetConfiguration();
  }
    public void test1() throws Exception {
     Logger logger = Logger.getLogger(RFATestCase.class);
      PropertyConfigurator.configure("input/RFA1.properties");
      for (int i = 0; i < 25; i++) {
        if (i < 10) {
          logger.debug("Hello---" + i);
        } else if (i < 100) {
          logger.debug("Hello--" + i);
        }
      }
      assertTrue(new File("output/RFA-test1.log").exists());
      assertTrue(new File("output/RFA-test1.log.1").exists());
    }
    public void test2() throws Exception {
      Logger logger = Logger.getLogger(RFATestCase.class);
      Logger root = Logger.getRootLogger();
      PatternLayout layout = new PatternLayout("%m\n");
      org.apache.log4j.RollingFileAppender rfa =
        new org.apache.log4j.RollingFileAppender();
      rfa.setName("ROLLING");
      rfa.setLayout(layout);
      rfa.setAppend(false);
      rfa.setMaxBackupIndex(3);
      rfa.setMaximumFileSize(100);
      rfa.setFile("output/RFA-test2.log");
      rfa.activateOptions();
      root.addAppender(rfa);
      for (int i = 0; i < 55; i++) {
        if (i < 10) {
          logger.debug("Hello---" + i);
        } else if (i < 100) {
          logger.debug("Hello--" + i);
        }
      }
      assertTrue(new File("output/RFA-test2.log").exists());
      assertTrue(new File("output/RFA-test2.log.1").exists());
      assertTrue(new File("output/RFA-test2.log.2").exists());
      assertTrue(new File("output/RFA-test2.log.3").exists());
      assertFalse(new File("output/RFA-test2.log.4").exists());
    }
    public void test2ParamConstructor() throws IOException {
        SimpleLayout layout = new SimpleLayout();
        RollingFileAppender appender =
                new RollingFileAppender(layout,"output/rfa_2param.log");
        assertEquals(1, appender.getMaxBackupIndex());
        assertEquals(10*1024*1024, appender.getMaximumFileSize());
    }
    public void test3ParamConstructor() throws IOException {
        SimpleLayout layout = new SimpleLayout();
        RollingFileAppender appender =
                new RollingFileAppender(layout,"output/rfa_3param.log", false);
        assertEquals(1, appender.getMaxBackupIndex());
    }
    public void testLockDotOne() throws Exception {
      Logger logger = Logger.getLogger(RFATestCase.class);
      Logger root = Logger.getRootLogger();
      PatternLayout layout = new PatternLayout("%m\n");
      org.apache.log4j.RollingFileAppender rfa =
        new org.apache.log4j.RollingFileAppender();
      rfa.setName("ROLLING");
      rfa.setLayout(layout);
      rfa.setAppend(false);
      rfa.setMaxBackupIndex(10);
      rfa.setMaximumFileSize(100);
      rfa.setFile("output/RFA-dot1.log");
      rfa.activateOptions();
      root.addAppender(rfa);
      new File("output/RFA-dot1.log.2").delete();
      FileWriter dot1 = new FileWriter("output/RFA-dot1.log.1");
      dot1.write("Locked file");
      FileWriter dot5 = new FileWriter("output/RFA-dot1.log.5");
      dot5.write("Unlocked file");
      dot5.close();
      for (int i = 0; i < 15; i++) {
        if (i < 10) {
          logger.debug("Hello---" + i);
        } else if (i < 100) {
          logger.debug("Hello--" + i);
        }
      }
      dot1.close();
      for (int i = 15; i < 25; i++) {
            logger.debug("Hello--" + i);
      }
      rfa.close();
      assertTrue(new File("output/RFA-dot1.log.7").exists());
      if (new File("output/RFA-dot1.log.2").length() < 15) {
          assertEquals(50, new File("output/RFA-dot1.log").length());
          assertEquals(200, new File("output/RFA-dot1.log.1").length());
      } else {
          assertTrue(new File("output/RFA-dot1.log").exists());
          assertTrue(new File("output/RFA-dot1.log.1").exists());
          assertTrue(new File("output/RFA-dot1.log.2").exists());
          assertTrue(new File("output/RFA-dot1.log.3").exists());
          assertFalse(new File("output/RFA-dot1.log.4").exists());
      }
    }
    public void testLockDotThree() throws Exception {
      Logger logger = Logger.getLogger(RFATestCase.class);
      Logger root = Logger.getRootLogger();
      PatternLayout layout = new PatternLayout("%m\n");
      org.apache.log4j.RollingFileAppender rfa =
        new org.apache.log4j.RollingFileAppender();
      rfa.setName("ROLLING");
      rfa.setLayout(layout);
      rfa.setAppend(false);
      rfa.setMaxBackupIndex(10);
      rfa.setMaximumFileSize(100);
      rfa.setFile("output/RFA-dot3.log");
      rfa.activateOptions();
      root.addAppender(rfa);
      new File("output/RFA-dot3.log.1").delete();
      new File("output/RFA-dot3.log.2").delete();
      new File("output/RFA-dot3.log.4").delete();
      FileWriter dot3 = new FileWriter("output/RFA-dot3.log.3");
      dot3.write("Locked file");
      FileWriter dot5 = new FileWriter("output/RFA-dot3.log.5");
      dot5.write("Unlocked file");
      dot5.close();
      for (int i = 0; i < 15; i++) {
        if (i < 10) {
          logger.debug("Hello---" + i);
        } else if (i < 100) {
          logger.debug("Hello--" + i);
        }
      }
      dot3.close();
      for (int i = 15; i < 35; i++) {
          logger.debug("Hello--" + i);
      }
      rfa.close();
      assertTrue(new File("output/RFA-dot3.log.8").exists());
      if (new File("output/RFA-dot3.log.5").exists()) {
          assertEquals(50, new File("output/RFA-dot3.log").length());
          assertEquals(100, new File("output/RFA-dot3.log.1").length());
          assertEquals(200, new File("output/RFA-dot3.log.2").length());
      } else {
          assertTrue(new File("output/RFA-dot3.log").exists());
          assertTrue(new File("output/RFA-dot3.log.1").exists());
          assertTrue(new File("output/RFA-dot3.log.2").exists());
          assertTrue(new File("output/RFA-dot3.log.3").exists());
          assertFalse(new File("output/RFA-dot3.log.4").exists());
      }
    }
}
