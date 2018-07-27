package org.apache.log4j;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.LoggingEvent;
public abstract class Layout implements OptionHandler {
  public final static String LINE_SEP = System.getProperty("line.separator");
  public final static int LINE_SEP_LEN  = LINE_SEP.length();
  abstract
  public
  String format(LoggingEvent event);
  public
  String getContentType() {
    return "text/plain";
  }
  public
  String getHeader() {
    return null;
  }
  public
  String getFooter() {
    return null;
  }
  abstract
  public
  boolean ignoresThrowable();
}
