package org.apache.tools.ant.util;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
public final class DateUtils {
    private static final int ONE_SECOND = 1000;
    private static final int ONE_MINUTE = 60;
    private static final int ONE_HOUR = 60;
    private static final int TEN = 10;
    public static final String ISO8601_DATETIME_PATTERN
            = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ISO8601_DATE_PATTERN
            = "yyyy-MM-dd";
    public static final String ISO8601_TIME_PATTERN
            = "HH:mm:ss";
    public static final DateFormat DATE_HEADER_FORMAT
        = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.US);
    private static final DateFormat DATE_HEADER_FORMAT_INT
    = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.US);
    private static final MessageFormat MINUTE_SECONDS
            = new MessageFormat("{0}{1}");
    private static final double[] LIMITS = {0, 1, 2};
    private static final String[] MINUTES_PART = {"", "1 minute ", "{0,number,###############} minutes "};
    private static final String[] SECONDS_PART = {"0 seconds", "1 second", "{1,number} seconds"};
    private static final ChoiceFormat MINUTES_FORMAT =
            new ChoiceFormat(LIMITS, MINUTES_PART);
    private static final ChoiceFormat SECONDS_FORMAT =
            new ChoiceFormat(LIMITS, SECONDS_PART);
    static {
        MINUTE_SECONDS.setFormat(0, MINUTES_FORMAT);
        MINUTE_SECONDS.setFormat(1, SECONDS_FORMAT);
    }
    private DateUtils() {
    }
    public static String format(long date, String pattern) {
        return format(new Date(date), pattern);
    }
    public static String format(Date date, String pattern) {
        DateFormat df = createDateFormat(pattern);
        return df.format(date);
    }
    public static String formatElapsedTime(long millis) {
        long seconds = millis / ONE_SECOND;
        long minutes = seconds / ONE_MINUTE;
        Object[] args = {new Long(minutes), new Long(seconds % ONE_MINUTE)};
        return MINUTE_SECONDS.format(args);
    }
    private static DateFormat createDateFormat(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(gmt);
        sdf.setLenient(true);
        return sdf;
    }
    public static int getPhaseOfMoon(Calendar cal) {
        int dayOfTheYear = cal.get(Calendar.DAY_OF_YEAR);
        int yearInMetonicCycle = ((cal.get(Calendar.YEAR) - 1900) % 19) + 1;
        int epact = (11 * yearInMetonicCycle + 18) % 30;
        if ((epact == 25 && yearInMetonicCycle > 11) || epact == 24) {
            epact++;
        }
        return (((((dayOfTheYear + epact) * 6) + 11) % 177) / 22) & 7;
    }
    public static String getDateForHeader() {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        int offset = tz.getOffset(cal.get(Calendar.ERA),
                                  cal.get(Calendar.YEAR),
                                  cal.get(Calendar.MONTH),
                                  cal.get(Calendar.DAY_OF_MONTH),
                                  cal.get(Calendar.DAY_OF_WEEK),
                                  cal.get(Calendar.MILLISECOND));
        StringBuffer tzMarker = new StringBuffer(offset < 0 ? "-" : "+");
        offset = Math.abs(offset);
        int hours = offset / (ONE_HOUR * ONE_MINUTE * ONE_SECOND);
        int minutes = offset / (ONE_MINUTE * ONE_SECOND) - ONE_HOUR * hours;
        if (hours < TEN) {
            tzMarker.append("0");
        }
        tzMarker.append(hours);
        if (minutes < TEN) {
            tzMarker.append("0");
        }
        tzMarker.append(minutes);
        synchronized (DATE_HEADER_FORMAT_INT) {
            return DATE_HEADER_FORMAT_INT.format(cal.getTime()) + tzMarker.toString();
        }
    }
    public static Date parseDateFromHeader(String datestr) throws ParseException {
        synchronized (DATE_HEADER_FORMAT_INT) {
            return DATE_HEADER_FORMAT_INT.parse(datestr);
        }
    }
    public static Date parseIso8601DateTime(String datestr)
        throws ParseException {
        return new SimpleDateFormat(ISO8601_DATETIME_PATTERN).parse(datestr);
    }
    public static Date parseIso8601Date(String datestr) throws ParseException {
        return new SimpleDateFormat(ISO8601_DATE_PATTERN).parse(datestr);
    }
    public static Date parseIso8601DateTimeOrDate(String datestr)
        throws ParseException {
        try {
            return parseIso8601DateTime(datestr);
        } catch (ParseException px) {
            return parseIso8601Date(datestr);
        }
    }
}
