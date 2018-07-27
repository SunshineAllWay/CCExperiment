package org.apache.log4j;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.helpers.PatternConverter;
public class PatternLayout extends Layout {
  public final static String DEFAULT_CONVERSION_PATTERN ="%m%n";
  public final static String TTCC_CONVERSION_PATTERN
                                             = "%r [%t] %p %c %x - %m%n";
  protected final int BUF_SIZE = 256;
  protected final int MAX_CAPACITY = 1024;
  private StringBuffer sbuf = new StringBuffer(BUF_SIZE);
  private String pattern;
  private PatternConverter head;
  public PatternLayout() {
    this(DEFAULT_CONVERSION_PATTERN);
  }
  public PatternLayout(String pattern) {
    this.pattern = pattern;
    head = createPatternParser((pattern == null) ? DEFAULT_CONVERSION_PATTERN :
			     pattern).parse();
  }
  public
  void setConversionPattern(String conversionPattern) {
    pattern = conversionPattern;
    head = createPatternParser(conversionPattern).parse();
  }
  public
  String getConversionPattern() {
    return pattern;
  }
  public
  void activateOptions() {
  }
  public
  boolean ignoresThrowable() {
    return true;
  }
  protected PatternParser createPatternParser(String pattern) {
    return new PatternParser(pattern);
  }
  public String format(LoggingEvent event) {
    if(sbuf.capacity() > MAX_CAPACITY) {
      sbuf = new StringBuffer(BUF_SIZE);
    } else {
      sbuf.setLength(0);
    }
    PatternConverter c = head;
    while(c != null) {
      c.format(sbuf, event);
      c = c.next;
    }
    return sbuf.toString();
  }
}
