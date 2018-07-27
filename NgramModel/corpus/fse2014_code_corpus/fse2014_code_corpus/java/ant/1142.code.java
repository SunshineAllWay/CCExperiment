package org.apache.tools.ant.util;
import java.util.Calendar;
import java.util.TimeZone;
import junit.framework.TestCase;
public class DateUtilsTest extends TestCase {
    public DateUtilsTest(String s) {
        super(s);
    }
    public void testElapsedTime(){
        String text = DateUtils.formatElapsedTime(50*1000);
        assertEquals("50 seconds", text);
        text = DateUtils.formatElapsedTime(65*1000);
        assertEquals("1 minute 5 seconds", text);
        text = DateUtils.formatElapsedTime(120*1000);
        assertEquals("2 minutes 0 seconds", text);
        text = DateUtils.formatElapsedTime(121*1000);
        assertEquals("2 minutes 1 second", text);
    }
    public void testLongElapsedTime(){
        assertEquals("2926 minutes 13 seconds",
                     DateUtils.formatElapsedTime(1000 * 175573));
        assertEquals("153722867280912 minutes 55 seconds",
                     DateUtils.formatElapsedTime(Long.MAX_VALUE));
    }
    public void testDateTimeISO(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002,1,23,10,11,12);
        String text = DateUtils.format(cal.getTime(),
                DateUtils.ISO8601_DATETIME_PATTERN);
        assertEquals("2002-02-23T09:11:12", text);
    }
    public void testDateISO(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002,1,23);
        String text = DateUtils.format(cal.getTime(),
                DateUtils.ISO8601_DATE_PATTERN);
        assertEquals("2002-02-23", text);
    }
    public void testTimeISODate(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002,1,23, 21, 11, 12);
        String text = DateUtils.format(cal.getTime(),
                DateUtils.ISO8601_TIME_PATTERN);
        assertEquals("20:11:12", text);
    }
    public void testTimeISO(){
        long ms = (20*3600 + 11*60 + 12)*1000;
        String text = DateUtils.format(ms,
                DateUtils.ISO8601_TIME_PATTERN);
        assertEquals("20:11:12", text);
    }
    public void testPhaseOfMoon() {
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002, 2, 27);
        assertEquals(4, DateUtils.getPhaseOfMoon(cal));
        cal.set(2002, 2, 12);
        assertEquals(0, DateUtils.getPhaseOfMoon(cal));
    }
}
