package org.apache.log4j;
import junit.framework.TestCase;
import org.apache.log4j.util.SerializationTestHelper;
import java.util.Locale;
public class LevelTest extends TestCase {
  public LevelTest(final String name) {
    super(name);
  }
  public void testSerializeINFO() throws Exception {
    int[] skip = new int[] {  };
    SerializationTestHelper.assertSerializationEquals(
      "witness/serialization/info.bin", Level.INFO, skip, Integer.MAX_VALUE);
  }
  public void testDeserializeINFO() throws Exception {
    Object obj =
      SerializationTestHelper.deserializeStream(
        "witness/serialization/info.bin");
    assertTrue(obj instanceof Level);
    Level info = (Level) obj;
    assertEquals("INFO", info.toString());
    if (!System.getProperty("java.version").startsWith("1.1.")) {
       assertTrue(obj == Level.INFO);
    }
  }
  public void testCustomLevelSerialization() throws Exception {
    CustomLevel custom = new CustomLevel();
    Object obj = SerializationTestHelper.serializeClone(custom);
    assertTrue(obj instanceof CustomLevel);
    CustomLevel clone = (CustomLevel) obj;
    assertEquals(Level.INFO.level, clone.level);
    assertEquals(Level.INFO.levelStr, clone.levelStr);
    assertEquals(Level.INFO.syslogEquivalent, clone.syslogEquivalent);
  }
  private static class CustomLevel extends Level {
    private static final long serialVersionUID = 1L;
    public CustomLevel() {
      super(
        Level.INFO.level, Level.INFO.levelStr, Level.INFO.syslogEquivalent);
    }
  }
  public void testTraceInt() {
      assertEquals(5000, Level.TRACE_INT);
  }
  public void testTrace() {
      assertEquals("TRACE", Level.TRACE.toString());
      assertEquals(5000, Level.TRACE.toInt());
      assertEquals(7, Level.TRACE.getSyslogEquivalent());
  }
  public void testIntToTrace() {
      Level trace = Level.toLevel(5000);
      assertEquals("TRACE", trace.toString());
  }
  public void testStringToTrace() {
        Level trace = Level.toLevel("TRACE");
        assertEquals("TRACE", trace.toString());
  }
  public void testLevelExtendsPriority() {
      assertTrue(Priority.class.isAssignableFrom(Level.class));
  }
  public void testOFF() {
    assertTrue(Level.OFF instanceof Level);
  }
    public void testFATAL() {
      assertTrue(Level.FATAL instanceof Level);
    }
    public void testERROR() {
      assertTrue(Level.ERROR instanceof Level);
    }
    public void testWARN() {
      assertTrue(Level.WARN instanceof Level);
    }
    public void testINFO() {
      assertTrue(Level.INFO instanceof Level);
    }
    public void testDEBUG() {
      assertTrue(Level.DEBUG instanceof Level);
    }
    public void testTRACE() {
      assertTrue(Level.TRACE instanceof Level);
    }
    public void testALL() {
      assertTrue(Level.ALL instanceof Level);
    }
    public void testSerialVersionUID() {
      assertEquals(3491141966387921974L, Level.serialVersionUID);
    }
  public void testIntToAll() {
      Level level = Level.toLevel(Level.ALL_INT);
      assertEquals("ALL", level.toString());
  }
  public void testIntToFatal() {
      Level level = Level.toLevel(Level.FATAL_INT);
      assertEquals("FATAL", level.toString());
  }
  public void testIntToOff() {
      Level level = Level.toLevel(Level.OFF_INT);
      assertEquals("OFF", level.toString());
  }
  public void testToLevelUnrecognizedInt() {
      Level level = Level.toLevel(17, Level.FATAL);
      assertEquals("FATAL", level.toString());
  }
  public void testToLevelNull() {
      Level level = Level.toLevel(null, Level.FATAL);
      assertEquals("FATAL", level.toString());
  }
  public void testDotlessLowerI() {
      Level level = Level.toLevel("\u0131nfo");
      assertEquals("INFO", level.toString());
  }
  public void testDottedLowerI() {
      Locale defaultLocale = Locale.getDefault();
      Locale turkey = new Locale("tr", "TR");
      Locale.setDefault(turkey);
      Level level = Level.toLevel("info");
      Locale.setDefault(defaultLocale);
      assertEquals("INFO", level.toString());
  }
}
