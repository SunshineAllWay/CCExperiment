package org.apache.log4j;
import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
public class MyPatternParser extends PatternParser {
  int counter = 0;
  public
  MyPatternParser(String pattern) {
    super(pattern);
  }
  public
  void finalizeConverter(char c) {
    if (c == '#') {
      addConverter(new UserDirPatternConverter(formattingInfo));
      currentLiteral.setLength(0);
    } else {
      super.finalizeConverter(c);
    }
  }
  private class UserDirPatternConverter extends PatternConverter {
    UserDirPatternConverter(FormattingInfo formattingInfo) {
      super(formattingInfo);
    }
    public
    String convert(LoggingEvent event) {
      return String.valueOf(++counter);
    }
  }  
}
