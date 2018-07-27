package org.apache.log4j.pattern;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;
public final class CachedDateFormat extends DateFormat {
  private static final long serialVersionUID = 1;
  public static final int NO_MILLISECONDS = -2;
  private static final String DIGITS = "0123456789";
  public static final int UNRECOGNIZED_MILLISECONDS = -1;
  private static final int MAGIC1 = 654;
  private static final String MAGICSTRING1 = "654";
  private static final int MAGIC2 = 987;
  private static final String MAGICSTRING2 = "987";
  private static final String ZERO_STRING = "000";
  private final DateFormat formatter;
  private int millisecondStart;
  private long slotBegin;
  private StringBuffer cache = new StringBuffer(50);
  private final int expiration;
  private long previousTime;
  private final Date tmpDate = new Date(0);
  public CachedDateFormat(final DateFormat dateFormat, final int expiration) {
    if (dateFormat == null) {
      throw new IllegalArgumentException("dateFormat cannot be null");
    }
    if (expiration < 0) {
      throw new IllegalArgumentException("expiration must be non-negative");
    }
    formatter = dateFormat;
    this.expiration = expiration;
    millisecondStart = 0;
    previousTime = Long.MIN_VALUE;
    slotBegin = Long.MIN_VALUE;
  }
  public static int findMillisecondStart(
    final long time, final String formatted, final DateFormat formatter) {
    long slotBegin = (time / 1000) * 1000;
    if (slotBegin > time) {
      slotBegin -= 1000;
    }
    int millis = (int) (time - slotBegin);
    int magic = MAGIC1;
    String magicString = MAGICSTRING1;
    if (millis == MAGIC1) {
      magic = MAGIC2;
      magicString = MAGICSTRING2;
    }
    String plusMagic = formatter.format(new Date(slotBegin + magic));
    if (plusMagic.length() != formatted.length()) {
      return UNRECOGNIZED_MILLISECONDS;
    } else {
      for (int i = 0; i < formatted.length(); i++) {
        if (formatted.charAt(i) != plusMagic.charAt(i)) {
          StringBuffer formattedMillis = new StringBuffer("ABC");
          millisecondFormat(millis, formattedMillis, 0);
          String plusZero = formatter.format(new Date(slotBegin));
          if (
            (plusZero.length() == formatted.length())
              && magicString.regionMatches(
                0, plusMagic, i, magicString.length())
              && formattedMillis.toString().regionMatches(
                0, formatted, i, magicString.length())
              && ZERO_STRING.regionMatches(
                0, plusZero, i, ZERO_STRING.length())) {
            return i;
          } else {
            return UNRECOGNIZED_MILLISECONDS;
          }
        }
      }
    }
    return NO_MILLISECONDS;
  }
  public StringBuffer format(
    Date date, StringBuffer sbuf, FieldPosition fieldPosition) {
    format(date.getTime(), sbuf);
    return sbuf;
  }
  public StringBuffer format(long now, StringBuffer buf) {
    if (now == previousTime) {
      buf.append(cache);
      return buf;
    }
    if (millisecondStart != UNRECOGNIZED_MILLISECONDS &&
        (now < (slotBegin + expiration)) && (now >= slotBegin)
          && (now < (slotBegin + 1000L))) {
        if (millisecondStart >= 0) {
          millisecondFormat((int) (now - slotBegin), cache, millisecondStart);
        }
        previousTime = now;
        buf.append(cache);
        return buf;
    }
    cache.setLength(0);
    tmpDate.setTime(now);
    cache.append(formatter.format(tmpDate));
    buf.append(cache);
    previousTime = now;
    slotBegin = (previousTime / 1000) * 1000;
    if (slotBegin > previousTime) {
      slotBegin -= 1000;
    }
    if (millisecondStart >= 0) {
      millisecondStart =
        findMillisecondStart(now, cache.toString(), formatter);
    }
    return buf;
  }
  private static void millisecondFormat(
    final int millis, final StringBuffer buf, final int offset) {
    buf.setCharAt(offset, DIGITS.charAt(millis / 100));
    buf.setCharAt(offset + 1, DIGITS.charAt((millis / 10) % 10));
    buf.setCharAt(offset + 2, DIGITS.charAt(millis % 10));
  }
  public void setTimeZone(final TimeZone timeZone) {
    formatter.setTimeZone(timeZone);
    previousTime = Long.MIN_VALUE;
    slotBegin = Long.MIN_VALUE;
  }
  public Date parse(String s, ParsePosition pos) {
    return formatter.parse(s, pos);
  }
  public NumberFormat getNumberFormat() {
    return formatter.getNumberFormat();
  }
  public static int getMaximumCacheValidity(final String pattern) {
    int firstS = pattern.indexOf('S');
    if ((firstS >= 0) && (firstS != pattern.lastIndexOf("SSS"))) {
      return 1;
    }
    return 1000;
  }
}
