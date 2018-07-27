package org.apache.tools.ant.listener;
import org.apache.tools.ant.DefaultLogger;
public class TimestampedLogger extends DefaultLogger {
    public static final String SPACER = " - at ";
    protected String getBuildFailedMessage() {
        return super.getBuildFailedMessage() + SPACER + getTimestamp();
    }
    protected String getBuildSuccessfulMessage() {
        return super.getBuildSuccessfulMessage() + SPACER + getTimestamp();
    }
}
