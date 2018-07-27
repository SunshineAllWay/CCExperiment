package org.apache.log4j.pattern;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
public final class LineSeparatorPatternConverter
  extends LoggingEventPatternConverter {
  private static final LineSeparatorPatternConverter INSTANCE =
    new LineSeparatorPatternConverter();
  private final String lineSep;
  private LineSeparatorPatternConverter() {
    super("Line Sep", "lineSep");
    lineSep = Layout.LINE_SEP;
  }
  public static LineSeparatorPatternConverter newInstance(
    final String[] options) {
    return INSTANCE;
  }
  public void format(LoggingEvent event, final StringBuffer toAppendTo) {
    toAppendTo.append(lineSep);
  }
  public void format(final Object obj, final StringBuffer toAppendTo) {
    toAppendTo.append(lineSep);
  }
}
