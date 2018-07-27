package org.apache.log4j.pattern;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
public final class LevelPatternConverter extends LoggingEventPatternConverter {
  private static final int TRACE_INT = 5000;
  private static final LevelPatternConverter INSTANCE =
    new LevelPatternConverter();
  private LevelPatternConverter() {
    super("Level", "level");
  }
  public static LevelPatternConverter newInstance(
    final String[] options) {
    return INSTANCE;
  }
  public void format(final LoggingEvent event, final StringBuffer output) {
    output.append(event.getLevel().toString());
  }
  public String getStyleClass(Object e) {
    if (e instanceof LoggingEvent) {
      int lint = ((LoggingEvent) e).getLevel().toInt();
      switch (lint) {
      case TRACE_INT:
        return "level trace";
      case Level.DEBUG_INT:
        return "level debug";
      case Level.INFO_INT:
        return "level info";
      case Level.WARN_INT:
        return "level warn";
      case Level.ERROR_INT:
        return "level error";
      case Level.FATAL_INT:
        return "level fatal";
      default:
        return "level " + ((LoggingEvent) e).getLevel().toString();
      }
    }
    return "level";
  }
}
