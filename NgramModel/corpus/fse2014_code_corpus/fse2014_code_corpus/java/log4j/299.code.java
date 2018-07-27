package org.apache.log4j;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;
import java.util.Vector;
public final class VectorErrorHandler implements ErrorHandler {
  private Logger logger;
  private Appender appender;
  private Appender backupAppender;
  private final Vector errors = new Vector();
  public VectorErrorHandler() {
  }
  public void setLogger(final Logger logger) {
    this.logger = logger;
  }
  public Logger getLogger() {
    return logger;
  }
  public void activateOptions() {
  }
  public void error(
    final String message, final Exception e, final int errorCode) {
    error(message, e, errorCode, null);
  }
  public void error(final String message) {
    error(message, null, -1, null);
  }
  public void error(
    final String message, final Exception e, final int errorCode,
    final LoggingEvent event) {
    errors.addElement(
      new Object[] { message, e, new Integer(errorCode), event });
  }
  public String getMessage(final int index) {
    return (String) ((Object[]) errors.elementAt(index))[0];
  }
  public Exception getException(final int index) {
    return (Exception) ((Object[]) errors.elementAt(index))[1];
  }
  public int getErrorCode(final int index) {
    return ((Integer) ((Object[]) errors.elementAt(index))[2]).intValue();
  }
  public LoggingEvent getEvent(final int index) {
    return (LoggingEvent) ((Object[]) errors.elementAt(index))[3];
  }
  public int size() {
    return errors.size();
  }
  public void setAppender(final Appender appender) {
    this.appender = appender;
  }
  public Appender getAppender() {
    return appender;
  }
  public void setBackupAppender(final Appender appender) {
    this.backupAppender = appender;
  }
  public Appender getBackupAppender() {
    return backupAppender;
  }
}
