package org.apache.solr.schema;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.SortField;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.OrdFieldSource;
import org.apache.solr.util.DateMathParser;
import java.util.Map;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.ParseException;
@Deprecated
public final class LegacyDateField extends DateField {
  public String toInternal(String val) {
    final int len=val.length();
    if (val.charAt(len-1) == Z) {
      return val.substring(0,len-1);
    }
    return toInternal(parseMath(null, val));
  }
  protected DateFormat getThreadLocalDateFormat() {
    return fmtThreadLocal.get();
  }
  protected String formatDate(Date d) {
    return getThreadLocalDateFormat().format(d);
  }
  private static ThreadLocalDateFormat fmtThreadLocal
    = new ThreadLocalDateFormat();
  private static class ThreadLocalDateFormat extends ThreadLocal<DateFormat> {
    DateFormat proto;
    public ThreadLocalDateFormat() {
      super();
      SimpleDateFormat tmp =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
      tmp.setTimeZone(UTC);
      proto = tmp;
    }
    protected DateFormat initialValue() {
      return (DateFormat) proto.clone();
    }
  }
}
