package org.apache.log4j.lf5;
public interface LogRecordFilter {
  public boolean passes(LogRecord record);
}
