package org.apache.log4j.helpers;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import java.text.FieldPosition;
import java.text.ParsePosition;
public class ISO8601DateFormat extends AbsoluteTimeDateFormat {
  private static final long serialVersionUID = -759840745298755296L;
  public
  ISO8601DateFormat() {
  }
  public
  ISO8601DateFormat(TimeZone timeZone) {
    super(timeZone);
  }
  static private long   lastTime;
  static private char[] lastTimeString = new char[20];
  public
  StringBuffer format(Date date, StringBuffer sbuf,
		      FieldPosition fieldPosition) {
    long now = date.getTime();
    int millis = (int)(now % 1000);
    if ((now - millis) != lastTime || lastTimeString[0] == 0) {
      calendar.setTime(date);
      int start = sbuf.length();
      int year =  calendar.get(Calendar.YEAR);
      sbuf.append(year);
      String month;
      switch(calendar.get(Calendar.MONTH)) {
      case Calendar.JANUARY: month = "-01-"; break;
      case Calendar.FEBRUARY: month = "-02-";  break;
      case Calendar.MARCH: month = "-03-"; break;
      case Calendar.APRIL: month = "-04-";  break;
      case Calendar.MAY: month = "-05-"; break;
      case Calendar.JUNE: month = "-06-";  break;
      case Calendar.JULY: month = "-07-"; break;
      case Calendar.AUGUST: month = "-08-";  break;
      case Calendar.SEPTEMBER: month = "-09-"; break;
      case Calendar.OCTOBER: month = "-10-"; break;
      case Calendar.NOVEMBER: month = "-11-";  break;
      case Calendar.DECEMBER: month = "-12-";  break;
      default: month = "-NA-"; break;
      }
      sbuf.append(month);
      int day = calendar.get(Calendar.DAY_OF_MONTH);
      if(day < 10)
	sbuf.append('0');
      sbuf.append(day);
      sbuf.append(' ');
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
      sbuf.getChars(start, sbuf.length(), lastTimeString, 0);
      lastTime = now - millis;
    }
    else {
      sbuf.append(lastTimeString);
    }
    if (millis < 100)
      sbuf.append('0');
    if (millis < 10)
      sbuf.append('0');
    sbuf.append(millis);
    return sbuf;
  }
  public
  Date parse(java.lang.String s, ParsePosition pos) {
    return null;
  }
}
