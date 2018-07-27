package org.apache.log4j.pattern;
import org.apache.log4j.spi.LoggingEvent;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.helpers.*;
public final class PropertiesPatternConverter
  extends LoggingEventPatternConverter {
  private final String option;
  private PropertiesPatternConverter(
    final String[] options) {
    super(
      ((options != null) && (options.length > 0))
      ? ("Property{" + options[0] + "}") : "Properties", "property");
    if ((options != null) && (options.length > 0)) {
      option = options[0];
    } else {
      option = null;
    }
  }
  public static PropertiesPatternConverter newInstance(
    final String[] options) {
    return new PropertiesPatternConverter(options);
  }
  public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
    if (option == null) {
      toAppendTo.append("{");
      try {
        Set keySet = MDCKeySetExtractor.INSTANCE.getPropertyKeySet(event);
          if (keySet != null) {
            for (Iterator i = keySet.iterator(); i.hasNext();) {
                Object item = i.next();
                Object val = event.getMDC(item.toString());
                toAppendTo.append("{").append(item).append(",").append(val).append(
                "}");
            }
          }
      } catch(Exception ex) {
              LogLog.error("Unexpected exception while extracting MDC keys", ex);
      }
      toAppendTo.append("}");
    } else {
      Object val = event.getMDC(option);
      if (val != null) {
        toAppendTo.append(val);
      }
    }
  }
}
