package org.apache.solr.common.util;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
public class DateUtil {
  public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
  public static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
  public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
  private static final Collection<String> DEFAULT_HTTP_CLIENT_PATTERNS = Arrays.asList(
          PATTERN_ASCTIME, PATTERN_RFC1036, PATTERN_RFC1123);
  private static final Date DEFAULT_TWO_DIGIT_YEAR_START;
  static {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2000, Calendar.JANUARY, 1, 0, 0);
    DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();
  }
  private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
  public static final Collection<String> DEFAULT_DATE_FORMATS = new ArrayList<String>();
  static {
    DEFAULT_DATE_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss'Z'");
    DEFAULT_DATE_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss");
    DEFAULT_DATE_FORMATS.add("yyyy-MM-dd");
    DEFAULT_DATE_FORMATS.add("yyyy-MM-dd hh:mm:ss");
    DEFAULT_DATE_FORMATS.add("yyyy-MM-dd HH:mm:ss");
    DEFAULT_DATE_FORMATS.add("EEE MMM d hh:mm:ss z yyyy");
    DEFAULT_DATE_FORMATS.addAll(DEFAULT_HTTP_CLIENT_PATTERNS);
  }
  public static Date parseDate(String d) throws ParseException {
    return parseDate(d, DEFAULT_DATE_FORMATS);
  }
  public static Date parseDate(String d, Collection<String> fmts) throws ParseException {
    if (d.endsWith("Z") && d.length() > 20) {
      return getThreadLocalDateFormat().parse(d);
    }
    return parseDate(d, fmts, null);
  }
  public static Date parseDate(
          String dateValue,
          Collection<String> dateFormats,
          Date startDate
  ) throws ParseException {
    if (dateValue == null) {
      throw new IllegalArgumentException("dateValue is null");
    }
    if (dateFormats == null) {
      dateFormats = DEFAULT_HTTP_CLIENT_PATTERNS;
    }
    if (startDate == null) {
      startDate = DEFAULT_TWO_DIGIT_YEAR_START;
    }
    if (dateValue.length() > 1
            && dateValue.startsWith("'")
            && dateValue.endsWith("'")
            ) {
      dateValue = dateValue.substring(1, dateValue.length() - 1);
    }
    SimpleDateFormat dateParser = null;
    Iterator formatIter = dateFormats.iterator();
    while (formatIter.hasNext()) {
      String format = (String) formatIter.next();
      if (dateParser == null) {
        dateParser = new SimpleDateFormat(format, Locale.US);
        dateParser.setTimeZone(GMT);
        dateParser.set2DigitYearStart(startDate);
      } else {
        dateParser.applyPattern(format);
      }
      try {
        return dateParser.parse(dateValue);
      } catch (ParseException pe) {
      }
    }
    throw new ParseException("Unable to parse the date " + dateValue, 0);
  }
  public static DateFormat getThreadLocalDateFormat() {
    return fmtThreadLocal.get();
  }
  public static TimeZone UTC = TimeZone.getTimeZone("UTC");
  private static ThreadLocalDateFormat fmtThreadLocal = new ThreadLocalDateFormat();
  private static class ThreadLocalDateFormat extends ThreadLocal<DateFormat> {
    DateFormat proto;
    public ThreadLocalDateFormat() {
      super();
      SimpleDateFormat tmp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      tmp.setTimeZone(UTC);
      proto = tmp;
    }
    @Override
    protected DateFormat initialValue() {
      return (DateFormat) proto.clone();
    }
  }
}