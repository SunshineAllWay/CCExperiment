package org.apache.tools.ant.taskdefs;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;
import org.apache.tools.ant.types.EnumeratedAttribute;
public class WaitFor extends ConditionBase {
    public static final long ONE_MILLISECOND = 1L;
    public static final long ONE_SECOND = 1000L;
    public static final long ONE_MINUTE = ONE_SECOND * 60L;
    public static final long ONE_HOUR   = ONE_MINUTE * 60L;
    public static final long ONE_DAY    = ONE_HOUR * 24L;
    public static final long ONE_WEEK   = ONE_DAY * 7L;
    public static final long DEFAULT_MAX_WAIT_MILLIS = ONE_MINUTE * 3L;
    public static final long DEFAULT_CHECK_MILLIS = 500L;
    private long maxWait = DEFAULT_MAX_WAIT_MILLIS;
    private long maxWaitMultiplier = ONE_MILLISECOND;
    private long checkEvery = DEFAULT_CHECK_MILLIS;
    private long checkEveryMultiplier = ONE_MILLISECOND;
    private String timeoutProperty;
    public WaitFor() {
        super("waitfor");
    }
    public WaitFor(String taskName) {
        super(taskName);
    }
    public void setMaxWait(long time) {
        maxWait = time;
    }
    public void setMaxWaitUnit(Unit unit) {
        maxWaitMultiplier = unit.getMultiplier();
    }
    public void setCheckEvery(long time) {
        checkEvery = time;
    }
    public void setCheckEveryUnit(Unit unit) {
        checkEveryMultiplier = unit.getMultiplier();
    }
    public void setTimeoutProperty(String p) {
        timeoutProperty = p;
    }
    public void execute() throws BuildException {
        if (countConditions() > 1) {
            throw new BuildException("You must not nest more than one "
                                     + "condition into "
                                     + getTaskName());
        }
        if (countConditions() < 1) {
            throw new BuildException("You must nest a condition into "
                                     + getTaskName());
        }
        Condition c = (Condition) getConditions().nextElement();
        try {
            long maxWaitMillis = calculateMaxWaitMillis();
            long checkEveryMillis = calculateCheckEveryMillis();
            long start = System.currentTimeMillis();
            long end = start + maxWaitMillis;
            while (System.currentTimeMillis() < end) {
                if (c.eval()) {
                    processSuccess();
                    return;
                }
                Thread.sleep(checkEveryMillis);
            }
        } catch (InterruptedException e) {
            log("Task " + getTaskName()
                    + " interrupted, treating as timed out.");
        }
        processTimeout();
    }
    public long calculateCheckEveryMillis() {
        return checkEvery * checkEveryMultiplier;
    }
    public long calculateMaxWaitMillis() {
        return maxWait * maxWaitMultiplier;
    }
    protected void processSuccess() {
        log(getTaskName() + ": condition was met", Project.MSG_VERBOSE);
    }
    protected void processTimeout() {
        log(getTaskName() + ": timeout", Project.MSG_VERBOSE);
        if (timeoutProperty != null) {
            getProject().setNewProperty(timeoutProperty, "true");
        }
    }
    public static class Unit extends EnumeratedAttribute {
        public static final String MILLISECOND = "millisecond";
        public static final String SECOND = "second";
        public static final String MINUTE = "minute";
        public static final String HOUR = "hour";
        public static final String DAY = "day";
        public static final String WEEK = "week";
        private static final String[] UNITS = {
            MILLISECOND, SECOND, MINUTE, HOUR, DAY, WEEK
        };
        private Map timeTable = new HashMap();
        public Unit() {
            timeTable.put(MILLISECOND, new Long(1L));
            timeTable.put(SECOND,      new Long(ONE_SECOND));
            timeTable.put(MINUTE,      new Long(ONE_MINUTE));
            timeTable.put(HOUR,        new Long(ONE_HOUR));
            timeTable.put(DAY,         new Long(ONE_DAY));
            timeTable.put(WEEK,        new Long(ONE_WEEK));
        }
        public long getMultiplier() {
            String key = getValue().toLowerCase(Locale.ENGLISH);
            Long l = (Long) timeTable.get(key);
            return l.longValue();
        }
        public String[] getValues() {
            return UNITS;
        }
    }
}
