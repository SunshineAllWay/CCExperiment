package org.apache.log4j.spi;
public abstract class Filter implements OptionHandler {
  public Filter next;
  public static final int DENY    = -1;
  public static final int NEUTRAL = 0;
  public static final int ACCEPT  = 1;
  public
  void activateOptions() {
  }
  abstract
  public
  int decide(LoggingEvent event);
  public void setNext(Filter next) {
    this.next = next;
  }
  public Filter getNext() {
        return next;
  }
}
