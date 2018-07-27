package org.apache.tools.ant.taskdefs;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
public class Tstamp extends Task {
    private Vector customFormats = new Vector();
    private String prefix = "";
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        if (!this.prefix.endsWith(".")) {
            this.prefix += ".";
        }
    }
    public void execute() throws BuildException {
        try {
            Date d = new Date();
            Enumeration i = customFormats.elements();
            while (i.hasMoreElements()) {
                CustomFormat cts = (CustomFormat) i.nextElement();
                cts.execute(getProject(), d, getLocation());
            }
            SimpleDateFormat dstamp = new SimpleDateFormat ("yyyyMMdd");
            setProperty("DSTAMP", dstamp.format(d));
            SimpleDateFormat tstamp = new SimpleDateFormat ("HHmm");
            setProperty("TSTAMP", tstamp.format(d));
            SimpleDateFormat today
                = new SimpleDateFormat ("MMMM d yyyy", Locale.US);
            setProperty("TODAY", today.format(d));
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
    public CustomFormat createFormat() {
        CustomFormat cts = new CustomFormat();
        customFormats.addElement(cts);
        return cts;
    }
    private void setProperty(String name, String value) {
        getProject().setNewProperty(prefix + name, value);
    }
    public class CustomFormat {
        private TimeZone timeZone;
        private String propertyName;
        private String pattern;
        private String language;
        private String country;
        private String variant;
        private int offset = 0;
        private int field = Calendar.DATE;
        public CustomFormat() {
        }
        public void setProperty(String propertyName) {
            this.propertyName = propertyName;
        }
        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
        public void setLocale(String locale) {
            StringTokenizer st = new StringTokenizer(locale, " \t\n\r\f,");
            try {
                language = st.nextToken();
                if (st.hasMoreElements()) {
                    country = st.nextToken();
                    if (st.hasMoreElements()) {
                        variant = st.nextToken();
                        if (st.hasMoreElements()) {
                            throw new BuildException("bad locale format",
                                                      getLocation());
                        }
                    }
                } else {
                    country = "";
                }
            } catch (NoSuchElementException e) {
                throw new BuildException("bad locale format", e,
                                         getLocation());
            }
        }
        public void setTimezone(String id) {
            timeZone = TimeZone.getTimeZone(id);
        }
        public void setOffset(int offset) {
            this.offset = offset;
        }
        public void setUnit(String unit) {
            log("DEPRECATED - The setUnit(String) method has been deprecated."
                + " Use setUnit(Tstamp.Unit) instead.");
            Unit u = new Unit();
            u.setValue(unit);
            field = u.getCalendarField();
        }
        public void setUnit(Unit unit) {
            field = unit.getCalendarField();
        }
        public void execute(Project project, Date date, Location location) {
            if (propertyName == null) {
                throw new BuildException("property attribute must be provided",
                                         location);
            }
            if (pattern == null) {
                throw new BuildException("pattern attribute must be provided",
                                         location);
            }
            SimpleDateFormat sdf;
            if (language == null) {
                sdf = new SimpleDateFormat(pattern);
            } else if (variant == null) {
                sdf = new SimpleDateFormat(pattern,
                                           new Locale(language, country));
            } else {
                sdf = new SimpleDateFormat(pattern,
                                           new Locale(language, country,
                                                      variant));
            }
            if (offset != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(field, offset);
                date = calendar.getTime();
            }
            if (timeZone != null) {
                sdf.setTimeZone(timeZone);
            }
            Tstamp.this.setProperty(propertyName, sdf.format(date));
        }
    }
    public static class Unit extends EnumeratedAttribute {
        private static final String MILLISECOND = "millisecond";
        private static final String SECOND = "second";
        private static final String MINUTE = "minute";
        private static final String HOUR = "hour";
        private static final String DAY = "day";
        private static final String WEEK = "week";
        private static final String MONTH = "month";
        private static final String YEAR = "year";
        private static final String[] UNITS = {
                                                MILLISECOND,
                                                SECOND,
                                                MINUTE,
                                                HOUR,
                                                DAY,
                                                WEEK,
                                                MONTH,
                                                YEAR
                                              };
        private Map calendarFields = new HashMap();
        public Unit() {
            calendarFields.put(MILLISECOND,
                               new Integer(Calendar.MILLISECOND));
            calendarFields.put(SECOND, new Integer(Calendar.SECOND));
            calendarFields.put(MINUTE, new Integer(Calendar.MINUTE));
            calendarFields.put(HOUR, new Integer(Calendar.HOUR_OF_DAY));
            calendarFields.put(DAY, new Integer(Calendar.DATE));
            calendarFields.put(WEEK, new Integer(Calendar.WEEK_OF_YEAR));
            calendarFields.put(MONTH, new Integer(Calendar.MONTH));
            calendarFields.put(YEAR, new Integer(Calendar.YEAR));
        }
        public int getCalendarField() {
            String key = getValue().toLowerCase(Locale.ENGLISH);
            Integer i = (Integer) calendarFields.get(key);
            return i.intValue();
        }
        public String[] getValues() {
            return UNITS;
        }
    }
}
