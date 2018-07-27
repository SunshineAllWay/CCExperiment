package org.apache.log4j.helpers;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.DateFormat;
public class AbsoluteTimeDateFormat extends DateFormat {
   private static final long serialVersionUID = -388856345976723342L;
  public final static String ABS_TIME_DATE_FORMAT = "ABSOLUTE";
  public final static String DATE_AND_TIME_DATE_FORMAT = "DATE";
  public final static String ISO8601_DATE_FORMAT = "ISO8601";
  public
  AbsoluteTimeDateFormat() {
    setCalendar(Calendar.getInstance());
  }
  public
  AbsoluteTimeDateFormat(TimeZone timeZone) {
    setCalendar(Calendar.getInstance(timeZone));
  }
  private static long   previousTime;
  private static char[] previousTimeWithoutMillis = new char[9]; 
  public
  StringBuffer format(Date date, StringBuffer sbuf,
		      FieldPosition fieldPosition) {
    long now = date.getTime();
    int millis = (int)(now % 1000);
    if ((now - millis) != previousTime || previousTimeWithoutMillis[0] == 0) {
      calendar.setTime(date);
      int start = sbuf.length();
      int hour = calendar.get(Calendar.HOUR_OF_DAY);
      if(hour < 10) {
	sbuf.append('0');
      }
      sbuf.append(hour);
      sbuf.append(':');
      int mins = calendar.get(Calendar.MINUTE);
      if(mins < 10) {
	sbuf.append('0');
      }
      sbuf.append(mins);
      sbuf.append(':');
      int secs = calendar.get(Calendar.SECOND);
      if(secs < 10) {
	sbuf.append('0');
      }
      sbuf.append(secs);
      sbuf.append(',');      
      sbuf.getChars(start, sbuf.length(), previousTimeWithoutMillis, 0);
      previousTime = now - millis;
    }
    else {
      sbuf.append(previousTimeWithoutMillis);
    }
    if(millis < 100) 
      sbuf.append('0');
    if(millis < 10) 
      sbuf.append('0');
    sbuf.append(millis);
    return sbuf;
  }
  public
  Date parse(String s, ParsePosition pos) {
    return null;
  }  
}
