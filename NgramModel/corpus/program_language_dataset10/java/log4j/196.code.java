package org.apache.log4j.pattern;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;
public final class DatePatternConverter extends LoggingEventPatternConverter {
  private static final String ABSOLUTE_FORMAT = "ABSOLUTE";
  private static final String ABSOLUTE_TIME_PATTERN = "HH:mm:ss,SSS";
  private static final String DATE_AND_TIME_FORMAT = "DATE";
  private static final String DATE_AND_TIME_PATTERN = "dd MMM yyyy HH:mm:ss,SSS";
  private static final String ISO8601_FORMAT = "ISO8601";
  private static final String ISO8601_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
  private final CachedDateFormat df;
  private static class DefaultZoneDateFormat extends DateFormat {
     private static final long serialVersionUID = 1;
    private final DateFormat dateFormat;
    public DefaultZoneDateFormat(final DateFormat format) {
        dateFormat = format;
    }
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(date, toAppendTo, fieldPosition);
    }
    public Date parse(String source, ParsePosition pos) {
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.parse(source, pos);
    }
  }
  private DatePatternConverter(final String[] options) {
    super("Date", "date");
    String patternOption;
    if ((options == null) || (options.length == 0)) {
      patternOption = null;
    } else {
      patternOption = options[0];
    }
    String pattern;
    if (
      (patternOption == null)
        || patternOption.equalsIgnoreCase(ISO8601_FORMAT)) {
      pattern = ISO8601_PATTERN;
    } else if (patternOption.equalsIgnoreCase(ABSOLUTE_FORMAT)) {
      pattern = ABSOLUTE_TIME_PATTERN;
    } else if (patternOption.equalsIgnoreCase(DATE_AND_TIME_FORMAT)) {
      pattern = DATE_AND_TIME_PATTERN;
    } else {
      pattern = patternOption;
    }
    int maximumCacheValidity = 1000;
    DateFormat simpleFormat = null;
    try {
      simpleFormat = new SimpleDateFormat(pattern);
      maximumCacheValidity = CachedDateFormat.getMaximumCacheValidity(pattern);
    } catch (IllegalArgumentException e) {
        LogLog.warn(
          "Could not instantiate SimpleDateFormat with pattern "
          + patternOption, e);
      simpleFormat = new SimpleDateFormat(ISO8601_PATTERN);
    }
    if ((options != null) && (options.length > 1)) {
      TimeZone tz = TimeZone.getTimeZone((String) options[1]);
      simpleFormat.setTimeZone(tz);
    } else {
      simpleFormat = new DefaultZoneDateFormat(simpleFormat);
    }
    df = new CachedDateFormat(simpleFormat, maximumCacheValidity);
  }
  public static DatePatternConverter newInstance(
    final String[] options) {
    return new DatePatternConverter(options);
  }
  public void format(final LoggingEvent event, final StringBuffer output) {
    synchronized(this) {
    	df.format(event.timeStamp, output);
    }
  }
  public void format(final Object obj, final StringBuffer output) {
    if (obj instanceof Date) {
      format((Date) obj, output);
    }
    super.format(obj, output);
  }
  public void format(final Date date, final StringBuffer toAppendTo) {
    synchronized(this) {
    	df.format(date.getTime(), toAppendTo);
    }
  }
}
