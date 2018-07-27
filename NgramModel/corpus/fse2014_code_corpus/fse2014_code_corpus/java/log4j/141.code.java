package org.apache.log4j.lf5.util;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
public class DateFormatManager {
  private TimeZone _timeZone = null;
  private Locale _locale = null;
  private String _pattern = null;
  private DateFormat _dateFormat = null;
  public DateFormatManager() {
    super();
    configure();
  }
  public DateFormatManager(TimeZone timeZone) {
    super();
    _timeZone = timeZone;
    configure();
  }
  public DateFormatManager(Locale locale) {
    super();
    _locale = locale;
    configure();
  }
  public DateFormatManager(String pattern) {
    super();
    _pattern = pattern;
    configure();
  }
  public DateFormatManager(TimeZone timeZone, Locale locale) {
    super();
    _timeZone = timeZone;
    _locale = locale;
    configure();
  }
  public DateFormatManager(TimeZone timeZone, String pattern) {
    super();
    _timeZone = timeZone;
    _pattern = pattern;
    configure();
  }
  public DateFormatManager(Locale locale, String pattern) {
    super();
    _locale = locale;
    _pattern = pattern;
    configure();
  }
  public DateFormatManager(TimeZone timeZone, Locale locale, String pattern) {
    super();
    _timeZone = timeZone;
    _locale = locale;
    _pattern = pattern;
    configure();
  }
  public synchronized TimeZone getTimeZone() {
    if (_timeZone == null) {
      return TimeZone.getDefault();
    } else {
      return _timeZone;
    }
  }
  public synchronized void setTimeZone(TimeZone timeZone) {
    _timeZone = timeZone;
    configure();
  }
  public synchronized Locale getLocale() {
    if (_locale == null) {
      return Locale.getDefault();
    } else {
      return _locale;
    }
  }
  public synchronized void setLocale(Locale locale) {
    _locale = locale;
    configure();
  }
  public synchronized String getPattern() {
    return _pattern;
  }
  public synchronized void setPattern(String pattern) {
    _pattern = pattern;
    configure();
  }
  public synchronized String getOutputFormat() {
    return _pattern;
  }
  public synchronized void setOutputFormat(String pattern) {
    _pattern = pattern;
    configure();
  }
  public synchronized DateFormat getDateFormatInstance() {
    return _dateFormat;
  }
  public synchronized void setDateFormatInstance(DateFormat dateFormat) {
    _dateFormat = dateFormat;
  }
  public String format(Date date) {
    return getDateFormatInstance().format(date);
  }
  public String format(Date date, String pattern) {
    DateFormat formatter = null;
    formatter = getDateFormatInstance();
    if (formatter instanceof SimpleDateFormat) {
      formatter = (SimpleDateFormat) (formatter.clone());
      ((SimpleDateFormat) formatter).applyPattern(pattern);
    }
    return formatter.format(date);
  }
  public Date parse(String date) throws ParseException {
    return getDateFormatInstance().parse(date);
  }
  public Date parse(String date, String pattern) throws ParseException {
    DateFormat formatter = null;
    formatter = getDateFormatInstance();
    if (formatter instanceof SimpleDateFormat) {
      formatter = (SimpleDateFormat) (formatter.clone());
      ((SimpleDateFormat) formatter).applyPattern(pattern);
    }
    return formatter.parse(date);
  }
  private synchronized void configure() {
    _dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL,
        DateFormat.FULL,
        getLocale());
    _dateFormat.setTimeZone(getTimeZone());
    if (_pattern != null) {
      ((SimpleDateFormat) _dateFormat).applyPattern(_pattern);
    }
  }
}
