package org.apache.log4j;
import org.apache.log4j.helpers.PatternParser;
public class EnhancedMyPatternLayout extends EnhancedPatternLayout {
  public
  EnhancedMyPatternLayout() {
    this(DEFAULT_CONVERSION_PATTERN);
  }
  public
  EnhancedMyPatternLayout(String pattern) {
    super(pattern);
  }
  public
  PatternParser createPatternParser(String pattern) {
    return new MyPatternParser(
      pattern == null ? DEFAULT_CONVERSION_PATTERN : pattern);
  }
}
