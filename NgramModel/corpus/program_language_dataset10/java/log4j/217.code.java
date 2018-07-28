package org.apache.log4j.pattern;
import org.apache.log4j.spi.LoggingEvent;
public class RelativeTimePatternConverter extends LoggingEventPatternConverter {
  private CachedTimestamp lastTimestamp = new CachedTimestamp(0, "");
  public RelativeTimePatternConverter() {
    super("Time", "time");
  }
  public static RelativeTimePatternConverter newInstance(
    final String[] options) {
    return new RelativeTimePatternConverter();
  }
  public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
    long timestamp = event.timeStamp;
    if (!lastTimestamp.format(timestamp, toAppendTo)) {
      final String formatted =
        Long.toString(timestamp - LoggingEvent.getStartTime());
      toAppendTo.append(formatted);
      lastTimestamp = new CachedTimestamp(timestamp, formatted);
    }
  }
  private static final class CachedTimestamp {
    private final long timestamp;
    private final String formatted;
    public CachedTimestamp(long timestamp, final String formatted) {
      this.timestamp = timestamp;
      this.formatted = formatted;
    }
    public boolean format(long newTimestamp, final StringBuffer toAppendTo) {
      if (newTimestamp == timestamp) {
        toAppendTo.append(formatted);
        return true;
      }
      return false;
    }
  }
}
