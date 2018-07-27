package org.apache.tools.ant.types.selectors;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.TimeComparison;
import org.apache.tools.ant.util.FileUtils;
public class DateSelector extends BaseExtendSelector {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private long millis = -1;
    private String dateTime = null;
    private boolean includeDirs = false;
    private long granularity = 0;
    private String pattern;
    private TimeComparison when = TimeComparison.EQUAL;
    public static final String MILLIS_KEY = "millis";
    public static final String DATETIME_KEY = "datetime";
    public static final String CHECKDIRS_KEY = "checkdirs";
    public static final String GRANULARITY_KEY = "granularity";
    public static final String WHEN_KEY = "when";
    public static final String PATTERN_KEY = "pattern";
    public DateSelector() {
        granularity = FILE_UTILS.getFileTimestampGranularity();
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("{dateselector date: ");
        buf.append(dateTime);
        buf.append(" compare: ").append(when.getValue());
        buf.append(" granularity: ");
        buf.append(granularity);
        if (pattern != null) {
            buf.append(" pattern: ").append(pattern);
        }
        buf.append("}");
        return buf.toString();
    }
    public void setMillis(long millis) {
        this.millis = millis;
    }
    public long getMillis() {
        if (dateTime != null) {
            validate();
        }
        return millis;
    }
    public void setDatetime(String dateTime) {
        this.dateTime = dateTime;
        millis = -1;
    }
    public void setCheckdirs(boolean includeDirs) {
        this.includeDirs = includeDirs;
    }
    public void setGranularity(int granularity) {
        this.granularity = granularity;
    }
    public void setWhen(TimeComparisons tcmp) {
        setWhen((TimeComparison) tcmp);
    }
    public void setWhen(TimeComparison t) {
        when = t;
    }
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    public void setParameters(Parameter[] parameters) {
        super.setParameters(parameters);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                String paramname = parameters[i].getName();
                if (MILLIS_KEY.equalsIgnoreCase(paramname)) {
                    try {
                        setMillis(Long.parseLong(parameters[i].getValue()));
                    } catch (NumberFormatException nfe) {
                        setError("Invalid millisecond setting "
                                + parameters[i].getValue());
                    }
                } else if (DATETIME_KEY.equalsIgnoreCase(paramname)) {
                    setDatetime(parameters[i].getValue());
                } else if (CHECKDIRS_KEY.equalsIgnoreCase(paramname)) {
                    setCheckdirs(Project.toBoolean(parameters[i].getValue()));
                } else if (GRANULARITY_KEY.equalsIgnoreCase(paramname)) {
                    try {
                        setGranularity(Integer.parseInt(parameters[i].getValue()));
                    } catch (NumberFormatException nfe) {
                        setError("Invalid granularity setting "
                            + parameters[i].getValue());
                    }
                } else if (WHEN_KEY.equalsIgnoreCase(paramname)) {
                    setWhen(new TimeComparison(parameters[i].getValue()));
                } else if (PATTERN_KEY.equalsIgnoreCase(paramname)) {
                    setPattern(parameters[i].getValue());
                } else {
                    setError("Invalid parameter " + paramname);
                }
            }
        }
    }
    public void verifySettings() {
        if (dateTime == null && millis < 0) {
            setError("You must provide a datetime or the number of "
                    + "milliseconds.");
        } else if (millis < 0 && dateTime != null) {
            DateFormat df = ((pattern == null)
                ? DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, DateFormat.SHORT, Locale.US)
                : new SimpleDateFormat(pattern));
            try {
                setMillis(df.parse(dateTime).getTime());
                if (millis < 0) {
                    setError("Date of " + dateTime
                        + " results in negative milliseconds value"
                        + " relative to epoch (January 1, 1970, 00:00:00 GMT).");
                }
            } catch (ParseException pe) {
                setError("Date of " + dateTime
                        + " Cannot be parsed correctly. It should be in"
                        + ((pattern == null)
                        ? " MM/DD/YYYY HH:MM AM_PM" : pattern) + " format.");
            }
        }
    }
    public boolean isSelected(File basedir, String filename, File file) {
        validate();
        return (file.isDirectory() && !includeDirs)
            || when.evaluate(file.lastModified(), millis, granularity);
    }
    public static class TimeComparisons extends TimeComparison {
    }
}
