package org.apache.log4j.spi;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.util.SerializationTestHelper;
import org.apache.log4j.Priority;
import org.apache.log4j.Category;
public class LoggingEventTest extends TestCase {
  public LoggingEventTest(final String name) {
    super(name);
  }
  public void testSerializationSimple() throws Exception {
    Logger root = Logger.getRootLogger();
    LoggingEvent event =
      new LoggingEvent(
        root.getClass().getName(), root, Level.INFO, "Hello, world.", null);
    int[] skip = new int[] { 352, 353, 354, 355, 356 };
    SerializationTestHelper.assertSerializationEquals(
      "witness/serialization/simple.bin", event, skip, 237);
  }
  public void testSerializationWithException() throws Exception {
    Logger root = Logger.getRootLogger();
    Exception ex = new Exception("Don't panic");
    LoggingEvent event =
      new LoggingEvent(
        root.getClass().getName(), root, Level.INFO, "Hello, world.", ex);
    int[] skip = new int[] { 352, 353, 354, 355, 356 };
    SerializationTestHelper.assertSerializationEquals(
      "witness/serialization/exception.bin", event, skip, 237);
  }
  public void testSerializationWithLocation() throws Exception {
    Logger root = Logger.getRootLogger();
    LoggingEvent event =
      new LoggingEvent(
        root.getClass().getName(), root, Level.INFO, "Hello, world.", null);
    event.getLocationInformation();
    int[] skip = new int[] { 352, 353, 354, 355, 356 };
    SerializationTestHelper.assertSerializationEquals(
      "witness/serialization/location.bin", event, skip, 237);
  }
  public void testSerializationNDC() throws Exception {
    Logger root = Logger.getRootLogger();
    NDC.push("ndc test");
    LoggingEvent event =
      new LoggingEvent(
        root.getClass().getName(), root, Level.INFO, "Hello, world.", null);
    int[] skip = new int[] { 352, 353, 354, 355, 356 };
    SerializationTestHelper.assertSerializationEquals(
      "witness/serialization/ndc.bin", event, skip, 237);
    }
  public void testSerializationMDC() throws Exception {
    Logger root = Logger.getRootLogger();
    MDC.put("mdckey", "mdcvalue");
    LoggingEvent event =
      new LoggingEvent(
        root.getClass().getName(), root, Level.INFO, "Hello, world.", null);
    int[] skip = new int[] { 352, 353, 354, 355, 356 };
    SerializationTestHelper.assertSerializationEquals(
      "witness/serialization/mdc.bin", event, skip, 237);
  }
  public void testDeserializationSimple() throws Exception {
    Object obj =
      SerializationTestHelper.deserializeStream(
        "witness/serialization/simple.bin");
    assertTrue(obj instanceof LoggingEvent);
    LoggingEvent event = (LoggingEvent) obj;
    assertEquals("Hello, world.", event.getMessage());
    assertEquals(Level.INFO, event.getLevel());
  }
  public void testDeserializationWithException() throws Exception {
    Object obj =
      SerializationTestHelper.deserializeStream(
        "witness/serialization/exception.bin");
    assertTrue(obj instanceof LoggingEvent);
    LoggingEvent event = (LoggingEvent) obj;
    assertEquals("Hello, world.", event.getMessage());
    assertEquals(Level.INFO, event.getLevel());
  }
  public void testDeserializationWithLocation() throws Exception {
    Object obj =
      SerializationTestHelper.deserializeStream(
        "witness/serialization/location.bin");
    assertTrue(obj instanceof LoggingEvent);
    LoggingEvent event = (LoggingEvent) obj;
    assertEquals("Hello, world.", event.getMessage());
    assertEquals(Level.INFO, event.getLevel());
  }
  public void testFQNOfCategoryClass() {
      Category root = Logger.getRootLogger();
      Priority info = Level.INFO;
      String catName = Logger.class.toString();
      LoggingEvent event =
        new LoggingEvent(
          catName, root, info, "Hello, world.", null);
      assertEquals(catName, event.fqnOfCategoryClass);
  }
  public void testLevel() {
      Category root = Logger.getRootLogger();
      Priority info = Level.INFO;
      String catName = Logger.class.toString();
      LoggingEvent event =
        new LoggingEvent(
          catName, root, 0L,  info, "Hello, world.", null);
      Priority error = Level.ERROR;
      event.level = error;
      assertEquals(Level.ERROR, event.level);
  }
  public void testLocationInfoNoFQCN() {
      Category root = Logger.getRootLogger();
	  Priority level = Level.INFO;
      LoggingEvent event =
        new LoggingEvent(
          null, root, 0L,  level, "Hello, world.", null);
      LocationInfo info = event.getLocationInformation();
	  assertNotNull(info);
	  if (info != null) {
	     assertEquals("?", info.getLineNumber());
		 assertEquals("?", info.getClassName());
		 assertEquals("?", info.getFileName());
		 assertEquals("?", info.getMethodName());
	  }
  }
    private static class BadMessage {
        public BadMessage() {
        }
        public String toString() {
            throw new RuntimeException();
        }
    }
    public void testBadMessage() {
        Category root = Logger.getRootLogger();
        Priority info = Level.INFO;
        String catName = Logger.class.toString();
        BadMessage msg = new BadMessage();
        LoggingEvent event =
          new LoggingEvent(
            catName, root, 0L,  info, msg, null);
        event.getRenderedMessage();
    }
}
