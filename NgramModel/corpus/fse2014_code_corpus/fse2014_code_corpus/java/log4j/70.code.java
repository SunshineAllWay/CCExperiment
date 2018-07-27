package org.apache.log4j;
import org.apache.log4j.spi.LoggerFactory;
public class Logger extends Category {
  private static final String FQCN = Logger.class.getName();
  protected
  Logger(String name) {
    super(name);
  }
  static
  public
  Logger getLogger(String name) {
    return LogManager.getLogger(name);
  }
  static
  public
  Logger getLogger(Class clazz) {
    return LogManager.getLogger(clazz.getName());
  }
  public
  static
  Logger getRootLogger() {
    return LogManager.getRootLogger();
  }
  public
  static
  Logger getLogger(String name, LoggerFactory factory) {
    return LogManager.getLogger(name, factory);
  }
    public void trace(Object message) {
      if (repository.isDisabled(Level.TRACE_INT)) {
        return;
      }
      if (Level.TRACE.isGreaterOrEqual(this.getEffectiveLevel())) {
        forcedLog(FQCN, Level.TRACE, message, null);
      }
    }
    public void trace(Object message, Throwable t) {
      if (repository.isDisabled(Level.TRACE_INT)) {
        return;
      }
      if (Level.TRACE.isGreaterOrEqual(this.getEffectiveLevel())) {
        forcedLog(FQCN, Level.TRACE, message, t);
      }
    }
    public boolean isTraceEnabled() {
        if (repository.isDisabled(Level.TRACE_INT)) {
            return false;
          }
          return Level.TRACE.isGreaterOrEqual(this.getEffectiveLevel());
    }
}
