package org.apache.log4j.pattern;
import org.apache.log4j.spi.LoggingEvent;
public final class MessagePatternConverter extends LoggingEventPatternConverter {
  private static final MessagePatternConverter INSTANCE =
    new MessagePatternConverter();
  private MessagePatternConverter() {
    super("Message", "message");
  }
  public static MessagePatternConverter newInstance(
    final String[] options) {
    return INSTANCE;
  }
  public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
    toAppendTo.append(event.getRenderedMessage());
  }
}
