package org.apache.log4j.spi;
import org.apache.log4j.Appender;
import org.apache.log4j.Category;
public interface HierarchyEventListener {
  public
  void addAppenderEvent(Category cat, Appender appender);
  public
  void removeAppenderEvent(Category cat, Appender appender);
}
