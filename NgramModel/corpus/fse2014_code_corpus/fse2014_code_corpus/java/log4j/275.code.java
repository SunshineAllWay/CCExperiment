package org.apache.log4j;
import junit.framework.TestCase;
import java.io.File;
import java.lang.reflect.Method;
public class FileAppenderTest extends TestCase {
  public void testDirectoryCreation() {
    if (!System.getProperty("java.version").startsWith("1.1.")) {
      File newFile = new File("output/newdir/temp.log");
      newFile.delete();
      File newDir = new File("output/newdir");
      newDir.delete();
      org.apache.log4j.FileAppender wa = new org.apache.log4j.FileAppender();
      wa.setFile("output/newdir/temp.log");
      wa.setLayout(new PatternLayout("%m%n"));
      wa.activateOptions();
      assertTrue(new File("output/newdir/temp.log").exists());
    }
  }
  public void testGetThresholdReturnType() throws Exception {
    Method method = FileAppender.class.getMethod("getThreshold", (Class[]) null);
    assertTrue(method.getReturnType() == Priority.class);
  }
  public void testgetSetThreshold() {
    FileAppender appender = new FileAppender();
    Priority debug = Level.DEBUG;
    assertNull(appender.getThreshold());
    appender.setThreshold(debug);
    assertTrue(appender.getThreshold() == debug);
  }
  public void testIsAsSevereAsThreshold() {
    FileAppender appender = new FileAppender();
    Priority debug = Level.DEBUG;
    assertTrue(appender.isAsSevereAsThreshold(debug));
  }
}
