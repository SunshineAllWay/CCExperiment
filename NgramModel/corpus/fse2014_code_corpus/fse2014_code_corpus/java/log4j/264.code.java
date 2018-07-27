package org.apache.log4j.performance;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.AppenderSkeleton;
public class NullAppender extends AppenderSkeleton {
  public static String s;
  public String t;
  public
  NullAppender() {}
  public
  NullAppender(Layout layout) {
    this.layout = layout;
  }
  public
  void close() {}
  public
  void doAppend(LoggingEvent event) {
    if(layout != null) {
      t = layout.format(event);
      s = t;
    }
  }
  public
  void append(LoggingEvent event) {
  }
  public
  boolean requiresLayout() {
    return true;
  }
}
