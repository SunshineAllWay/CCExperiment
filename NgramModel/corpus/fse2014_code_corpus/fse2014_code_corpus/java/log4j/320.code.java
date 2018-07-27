package org.apache.log4j.pattern;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.pattern.CachedDateFormat;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;
public final class CachedDateFormatTest
    extends TestCase {
  public CachedDateFormatTest(String name) {
    super(name);
  }
  private static DateFormat createAbsoluteTimeDateFormat(TimeZone timeZone) {
      DateFormat df = new SimpleDateFormat("HH:mm:ss,SSS");
      df.setTimeZone(timeZone);
      return df;
  }
  private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
  private static final TimeZone CHICAGO = TimeZone.getTimeZone(
      "America/Chicago");
  public void test1() {
    DateFormat gmtFormat = new CachedDateFormat(createAbsoluteTimeDateFormat(GMT), 1000);
    long ticks = 12601L * 86400000L;
    Date jul1 = new Date(ticks);
    assertEquals("00:00:00,000", gmtFormat.format(jul1));
    Date plus8ms = new Date(ticks + 8);
    assertEquals("00:00:00,008", gmtFormat.format(plus8ms));
    Date plus17ms = new Date(ticks + 17);
    assertEquals("00:00:00,017", gmtFormat.format(plus17ms));
    Date plus237ms = new Date(ticks + 237);
    assertEquals("00:00:00,237", gmtFormat.format(plus237ms));
    Date plus1415ms = new Date(ticks + 1415);
    assertEquals("00:00:01,415", gmtFormat.format(plus1415ms));
  }
  public void test2() {
      Date jul2 = new Date(12602L * 86400000L);
      DateFormat gmtFormat = new CachedDateFormat(createAbsoluteTimeDateFormat(GMT), 1000);
      DateFormat chicagoFormat = new CachedDateFormat(createAbsoluteTimeDateFormat(CHICAGO), 1000);
      assertEquals("00:00:00,000", gmtFormat.format(jul2));
      assertEquals("19:00:00,000", chicagoFormat.format(jul2));
      assertEquals("00:00:00,000", gmtFormat.format(jul2));
  }
  public void test3() {
    DateFormat gmtFormat = new CachedDateFormat(
       createAbsoluteTimeDateFormat(GMT), 1000);
    long ticks = -7L * 86400000L;
    Date jul1 = new Date(ticks + 8);
    assertEquals("00:00:00,008", gmtFormat.format(jul1));
    Date plus8ms = new Date(ticks + 16);
    assertEquals("00:00:00,016", gmtFormat.format(plus8ms));
    Date plus17ms = new Date(ticks + 23);
    assertEquals("00:00:00,023", gmtFormat.format(plus17ms));
    Date plus237ms = new Date(ticks + 245);
    assertEquals("00:00:00,245", gmtFormat.format(plus237ms));
    Date plus1415ms = new Date(ticks + 1423);
    assertEquals("00:00:01,423", gmtFormat.format(plus1415ms));
  }
  public void test4() {
    SimpleDateFormat baseFormat =
         new SimpleDateFormat("EEE, MMM dd, HH:mm:ss.SSS z", Locale.ENGLISH);
    DateFormat cachedFormat = new CachedDateFormat(baseFormat, 1000);
    long ticks = 11141L * 86400000L;
    Date jul1 = new Date(ticks);
    assertEquals(baseFormat.format(jul1), cachedFormat.format(jul1));
    Date plus8ms = new Date(ticks + 8);
    baseFormat.format(plus8ms);
    cachedFormat.format(plus8ms);
    assertEquals(baseFormat.format(plus8ms), cachedFormat.format(plus8ms));
    Date plus17ms = new Date(ticks + 17);
    assertEquals(baseFormat.format(plus17ms), cachedFormat.format(plus17ms));
    Date plus237ms = new Date(ticks + 237);
    assertEquals(baseFormat.format(plus237ms), cachedFormat.format(plus237ms));
    Date plus1415ms = new Date(ticks + 1415);
    assertEquals(baseFormat.format(plus1415ms), cachedFormat.format(plus1415ms));
  }
  public void test5() {
    Locale thai = new Locale("th", "TH");
    SimpleDateFormat baseFormat =
         new SimpleDateFormat("EEE, MMM dd, HH:mm:ss.SSS z", thai);
    DateFormat cachedFormat = new CachedDateFormat(baseFormat, 1000);
    long ticks = 11141L * 86400000L;
    String sx;
    Date jul1 = new Date(ticks);
    sx = cachedFormat.format(jul1);
    System.out.println(baseFormat.format(jul1));
    System.out.println(sx);
    assertEquals(baseFormat.format(jul1), sx);
    sx = cachedFormat.format(jul1);
    System.out.println(baseFormat.format(jul1));
    System.out.println(sx);
    assertEquals(baseFormat.format(jul1), sx);
    Date plus8ms = new Date(ticks + 8);
    sx = cachedFormat.format(plus8ms);
    System.out.println(baseFormat.format(plus8ms));
    System.out.println(sx);
    assertEquals(baseFormat.format(plus8ms), sx);
    Date plus17ms = new Date(ticks + 17);
    assertEquals(baseFormat.format(plus17ms), cachedFormat.format(plus17ms));
    Date plus237ms = new Date(ticks + 237);
    assertEquals(baseFormat.format(plus237ms), cachedFormat.format(plus237ms));
    Date plus1415ms = new Date(ticks + 1415);
    assertEquals(baseFormat.format(plus1415ms), cachedFormat.format(plus1415ms));
  }
  public void test6() {
    assertNotNull(new CachedDateFormat(new SimpleDateFormat(), 1000).getNumberFormat());
  }
  public void test8() {
    DateFormat baseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    baseFormat.setTimeZone(GMT);
    DateFormat cachedFormat = new CachedDateFormat(baseFormat, 1000);
    Date jul4 = new Date(12603L * 86400000L);
    assertEquals("2004-07-04 00:00:00,000", cachedFormat.format(jul4));
    cachedFormat.setTimeZone(TimeZone.getTimeZone("GMT-6"));
    assertEquals("2004-07-03 18:00:00,000", cachedFormat.format(jul4));
  }
  public void test9() {
    DateFormat baseFormat = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss,SS z", Locale.US);
    DateFormat cachedFormat = new CachedDateFormat(baseFormat, 1000);
    TimeZone cet = TimeZone.getTimeZone("GMT+1");
    cachedFormat.setTimeZone(cet);
    Calendar c = Calendar.getInstance();
    c.set(2004, Calendar.DECEMBER, 12, 20, 0);
    c.set(Calendar.SECOND, 37);
    c.set(Calendar.MILLISECOND, 23);
    c.setTimeZone(cet);
    String expected = baseFormat.format(c.getTime());
    String s = cachedFormat.format(c.getTime());
    assertEquals(expected, s);
    c.set(2005, Calendar.JANUARY, 1, 0, 0);
    c.set(Calendar.SECOND, 13);
    c.set(Calendar.MILLISECOND, 905);
    expected = baseFormat.format(c.getTime());
    s = cachedFormat.format(c.getTime());
    assertEquals(expected, s);
  }
  public void test10() {
    DateFormat baseFormat = new SimpleDateFormat("MMMM SSS EEEEEE", Locale.US);
    DateFormat cachedFormat = new CachedDateFormat(baseFormat, 1000);
    TimeZone cet = TimeZone.getTimeZone("GMT+1");
    cachedFormat.setTimeZone(cet);
    Calendar c = Calendar.getInstance();
    c.set(2004, Calendar.OCTOBER, 5, 20, 0);
    c.set(Calendar.SECOND, 37);
    c.set(Calendar.MILLISECOND, 23);
    c.setTimeZone(cet);
    String expected = baseFormat.format(c.getTime());
    String s = cachedFormat.format(c.getTime());
    assertEquals(expected, s);
    c.set(2004, Calendar.NOVEMBER, 1, 0, 0);
    c.set(Calendar.MILLISECOND, 23);
    expected = baseFormat.format(c.getTime());
    s = cachedFormat.format(c.getTime());
    assertEquals(expected, s);
    c.set(Calendar.MILLISECOND, 984);
    expected = baseFormat.format(c.getTime());
    s = cachedFormat.format(c.getTime());
    assertEquals(expected, s);
  }
  public void test11() {
     String badPattern = "ss,SS0";
     SimpleDateFormat simpleFormat = new SimpleDateFormat(badPattern);
     SimpleDateFormat baseFormat = new SimpleDateFormat(badPattern);
     DateFormat gmtFormat = new CachedDateFormat(simpleFormat, 1000);
     gmtFormat.setTimeZone(GMT);
     baseFormat.setTimeZone(GMT);
     long ticks = 11142L * 86400000L;
     Date jul2 = new Date(ticks + 120);
     String expected = baseFormat.format(jul2);
     assertEquals(expected, gmtFormat.format(jul2));
     jul2.setTime(ticks + 87);
     expected = baseFormat.format(jul2);
     assertEquals(expected, gmtFormat.format(jul2));
  }
  public void test12() {
     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
     long ticks = 11142L * 86400000L;
     String formatted = df.format(new Date(ticks));
     int millisecondStart = CachedDateFormat.findMillisecondStart(ticks, formatted, df);
     assertEquals(20, millisecondStart);     
  }
  public void test13() {
     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
     long ticks = 11142L * 86400000L;
     String formatted = df.format(new Date(ticks));
     int millisecondStart = CachedDateFormat.findMillisecondStart(ticks, formatted, df);
     assertEquals(CachedDateFormat.NO_MILLISECONDS, millisecondStart);     
  }
  public void test14() {
     SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss,SSS");
     long ticks = 11142L * 86400000L;
     String formatted = df.format(new Date(ticks));
     int millisecondStart = CachedDateFormat.findMillisecondStart(ticks, formatted, df);
     assertEquals(9, millisecondStart);     
  }
  public void test15() {
     SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss,S");
     long ticks = 11142L * 86400000L;
     String formatted = df.format(new Date(ticks));
     int millisecondStart = CachedDateFormat.findMillisecondStart(ticks, formatted, df);
     assertEquals(CachedDateFormat.UNRECOGNIZED_MILLISECONDS, millisecondStart);     
  }
  public void test16() {
     SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss,SS");
     long ticks = 11142L * 86400000L;
     String formatted = df.format(new Date(ticks));
     int millisecondStart = CachedDateFormat.findMillisecondStart(ticks, formatted, df);
     assertEquals(CachedDateFormat.UNRECOGNIZED_MILLISECONDS, millisecondStart);     
  }
  public void test17() {
      Date jul2 = new Date(12602L * 86400000L);
      String badPattern = "HH:mm:ss,SSS HH:mm:ss,SSS";
      SimpleDateFormat simpleFormat = new SimpleDateFormat(badPattern);
      simpleFormat.setTimeZone(GMT);
      DateFormat cachedFormat = new CachedDateFormat(simpleFormat, 1000);
      String s = cachedFormat.format(jul2);
      assertEquals("00:00:00,000 00:00:00,000", s);
      jul2.setTime(jul2.getTime() + 120);
      assertEquals("00:00:00,120 00:00:00,120", simpleFormat.format(jul2));
      s = cachedFormat.format(jul2);
      int maxValid = CachedDateFormat.getMaximumCacheValidity(badPattern);
      assertEquals(1, maxValid);
  }
  public static Test xsuite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new CachedDateFormatTest("test5"));
    return suite;
  }
}
