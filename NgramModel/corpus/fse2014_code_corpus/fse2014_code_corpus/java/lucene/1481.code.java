package org.apache.lucene.document;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import org.apache.lucene.search.NumericRangeQuery; 
import org.apache.lucene.util.NumericUtils; 
public class DateTools {
  private final static TimeZone GMT = TimeZone.getTimeZone("GMT");
  private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.US);
  private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyyMM", Locale.US);
  private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);
  private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("yyyyMMddHH", Locale.US);
  private static final SimpleDateFormat MINUTE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
  private static final SimpleDateFormat SECOND_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
  private static final SimpleDateFormat MILLISECOND_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
  static {
    YEAR_FORMAT.setTimeZone(GMT);
    MONTH_FORMAT.setTimeZone(GMT);
    DAY_FORMAT.setTimeZone(GMT);
    HOUR_FORMAT.setTimeZone(GMT);
    MINUTE_FORMAT.setTimeZone(GMT);
    SECOND_FORMAT.setTimeZone(GMT);
    MILLISECOND_FORMAT.setTimeZone(GMT);
  }
  private static final Calendar calInstance = Calendar.getInstance(GMT);
  private DateTools() {}
  public static synchronized String dateToString(Date date, Resolution resolution) {
    return timeToString(date.getTime(), resolution);
  }
  public static synchronized String timeToString(long time, Resolution resolution) {
    calInstance.setTimeInMillis(round(time, resolution));
    Date date = calInstance.getTime();
    if (resolution == Resolution.YEAR) {
      return YEAR_FORMAT.format(date);
    } else if (resolution == Resolution.MONTH) {
      return MONTH_FORMAT.format(date);
    } else if (resolution == Resolution.DAY) {
      return DAY_FORMAT.format(date);
    } else if (resolution == Resolution.HOUR) {
      return HOUR_FORMAT.format(date);
    } else if (resolution == Resolution.MINUTE) {
      return MINUTE_FORMAT.format(date);
    } else if (resolution == Resolution.SECOND) {
      return SECOND_FORMAT.format(date);
    } else if (resolution == Resolution.MILLISECOND) {
      return MILLISECOND_FORMAT.format(date);
    }
    throw new IllegalArgumentException("unknown resolution " + resolution);
  }
  public static synchronized long stringToTime(String dateString) throws ParseException {
    return stringToDate(dateString).getTime();
  }
  public static synchronized Date stringToDate(String dateString) throws ParseException {
    if (dateString.length() == 4) {
      return YEAR_FORMAT.parse(dateString);
    } else if (dateString.length() == 6) {
      return MONTH_FORMAT.parse(dateString);
    } else if (dateString.length() == 8) {
      return DAY_FORMAT.parse(dateString);
    } else if (dateString.length() == 10) {
      return HOUR_FORMAT.parse(dateString);
    } else if (dateString.length() == 12) {
      return MINUTE_FORMAT.parse(dateString);
    } else if (dateString.length() == 14) {
      return SECOND_FORMAT.parse(dateString);
    } else if (dateString.length() == 17) {
      return MILLISECOND_FORMAT.parse(dateString);
    }
    throw new ParseException("Input is not valid date string: " + dateString, 0);
  }
  public static synchronized Date round(Date date, Resolution resolution) {
    return new Date(round(date.getTime(), resolution));
  }
  public static synchronized long round(long time, Resolution resolution) {
    calInstance.setTimeInMillis(time);
    if (resolution == Resolution.YEAR) {
      calInstance.set(Calendar.MONTH, 0);
      calInstance.set(Calendar.DAY_OF_MONTH, 1);
      calInstance.set(Calendar.HOUR_OF_DAY, 0);
      calInstance.set(Calendar.MINUTE, 0);
      calInstance.set(Calendar.SECOND, 0);
      calInstance.set(Calendar.MILLISECOND, 0);
    } else if (resolution == Resolution.MONTH) {
      calInstance.set(Calendar.DAY_OF_MONTH, 1);
      calInstance.set(Calendar.HOUR_OF_DAY, 0);
      calInstance.set(Calendar.MINUTE, 0);
      calInstance.set(Calendar.SECOND, 0);
      calInstance.set(Calendar.MILLISECOND, 0);
    } else if (resolution == Resolution.DAY) {
      calInstance.set(Calendar.HOUR_OF_DAY, 0);
      calInstance.set(Calendar.MINUTE, 0);
      calInstance.set(Calendar.SECOND, 0);
      calInstance.set(Calendar.MILLISECOND, 0);
    } else if (resolution == Resolution.HOUR) {
      calInstance.set(Calendar.MINUTE, 0);
      calInstance.set(Calendar.SECOND, 0);
      calInstance.set(Calendar.MILLISECOND, 0);
    } else if (resolution == Resolution.MINUTE) {
      calInstance.set(Calendar.SECOND, 0);
      calInstance.set(Calendar.MILLISECOND, 0);
    } else if (resolution == Resolution.SECOND) {
      calInstance.set(Calendar.MILLISECOND, 0);
    } else if (resolution == Resolution.MILLISECOND) {
    } else {
      throw new IllegalArgumentException("unknown resolution " + resolution);
    }
    return calInstance.getTimeInMillis();
  }
  public static class Resolution {
    public static final Resolution YEAR = new Resolution("year");
    public static final Resolution MONTH = new Resolution("month");
    public static final Resolution DAY = new Resolution("day");
    public static final Resolution HOUR = new Resolution("hour");
    public static final Resolution MINUTE = new Resolution("minute");
    public static final Resolution SECOND = new Resolution("second");
    public static final Resolution MILLISECOND = new Resolution("millisecond");
    private String resolution;
    private Resolution() {
    }
    private Resolution(String resolution) {
      this.resolution = resolution;
    }
    @Override
    public String toString() {
      return resolution;
    }
  }
}
