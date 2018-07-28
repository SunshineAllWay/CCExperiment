package org.apache.log4j;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.pattern.BridgePatternConverter;
import org.apache.log4j.spi.LoggingEvent;
public class EnhancedPatternLayout extends Layout {
  public static final String DEFAULT_CONVERSION_PATTERN = "%m%n";
  public static final String TTCC_CONVERSION_PATTERN =
    "%r [%t] %p %c %x - %m%n";
  protected final int BUF_SIZE = 256;
  protected final int MAX_CAPACITY = 1024;
  public static final String PATTERN_RULE_REGISTRY = "PATTERN_RULE_REGISTRY";
  private PatternConverter head;
  private String conversionPattern;
  private boolean handlesExceptions;
  public EnhancedPatternLayout() {
    this(DEFAULT_CONVERSION_PATTERN);
  }
  public EnhancedPatternLayout(final String pattern) {
    this.conversionPattern = pattern;
    head = createPatternParser(
            (pattern == null) ? DEFAULT_CONVERSION_PATTERN : pattern).parse();
    if (head instanceof BridgePatternConverter) {
        handlesExceptions = !((BridgePatternConverter) head).ignoresThrowable();
    } else {
        handlesExceptions = false;
    }
  }
  public void setConversionPattern(final String conversionPattern) {
    this.conversionPattern =
      OptionConverter.convertSpecialChars(conversionPattern);
      head = createPatternParser(this.conversionPattern).parse();
      if (head instanceof BridgePatternConverter) {
          handlesExceptions = !((BridgePatternConverter) head).ignoresThrowable();
      } else {
          handlesExceptions = false;
      }
  }
  public String getConversionPattern() {
    return conversionPattern;
  }
    protected org.apache.log4j.helpers.PatternParser createPatternParser(String pattern) {
      return new org.apache.log4j.pattern.BridgePatternParser(pattern);
    }
  public void activateOptions() {
  }
  public String format(final LoggingEvent event) {
      StringBuffer buf = new StringBuffer();
      for(PatternConverter c = head;
          c != null;
          c = c.next) {
          c.format(buf, event);
      }
      return buf.toString();
  }
  public boolean ignoresThrowable() {
    return !handlesExceptions;
  }
}
