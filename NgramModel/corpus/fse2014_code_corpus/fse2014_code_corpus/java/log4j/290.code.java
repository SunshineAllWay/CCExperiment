package org.apache.log4j;
import junit.framework.TestCase;
import java.util.Locale;
public class PriorityTest extends TestCase {
  public PriorityTest(final String name) {
    super(name);
  }
  public void testOffInt() {
    assertEquals(Integer.MAX_VALUE, Priority.OFF_INT);
  }
  public void testFatalInt() {
    assertEquals(50000, Priority.FATAL_INT);
  }
  public void testErrorInt() {
    assertEquals(40000, Priority.ERROR_INT);
  }
  public void testWarnInt() {
    assertEquals(30000, Priority.WARN_INT);
  }
  public void testInfoInt() {
    assertEquals(20000, Priority.INFO_INT);
  }
  public void testDebugInt() {
    assertEquals(10000, Priority.DEBUG_INT);
  }
  public void testAllInt() {
    assertEquals(Integer.MIN_VALUE, Priority.ALL_INT);
  }
  public void testFatal() {
    assertTrue(Priority.FATAL instanceof Level);
  }
  public void testERROR() {
    assertTrue(Priority.ERROR instanceof Level);
  }
  public void testWARN() {
    assertTrue(Priority.WARN instanceof Level);
  }
  public void testINFO() {
    assertTrue(Priority.INFO instanceof Level);
  }
  public void testDEBUG() {
    assertTrue(Priority.DEBUG instanceof Level);
  }
  public void testEqualsNull() {
    assertFalse(Priority.DEBUG.equals(null));
  }
  public void testEqualsLevel() {
    assertTrue(Priority.DEBUG.equals(Level.DEBUG));
  }
  public void testGetAllPossiblePriorities() {
    Priority[] priorities = Priority.getAllPossiblePriorities();
    assertEquals(5, priorities.length);
  }
  public void testToPriorityString() {
    assertTrue(Priority.toPriority("DEBUG") == Level.DEBUG);
  }
  public void testToPriorityInt() {
    assertTrue(Priority.toPriority(Priority.DEBUG_INT) == Level.DEBUG);
  }
  public void testToPriorityStringPriority() {
    assertTrue(Priority.toPriority("foo", Priority.DEBUG) == Priority.DEBUG);
  }
  public void testToPriorityIntPriority() {
    assertTrue(Priority.toPriority(17, Priority.DEBUG) == Priority.DEBUG);
  }
  public void testDotlessLowerI() {
      Priority level = Priority.toPriority("\u0131nfo");
      assertEquals("INFO", level.toString());
  }
  public void testDottedLowerI() {
      Locale defaultLocale = Locale.getDefault();
      Locale turkey = new Locale("tr", "TR");
      Locale.setDefault(turkey);
      Priority level = Priority.toPriority("info");
      Locale.setDefault(defaultLocale);
      assertEquals("INFO", level.toString());
  }
}
