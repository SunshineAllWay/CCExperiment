package org.apache.solr.schema;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.QParser;
import org.apache.solr.search.function.*;
import org.apache.solr.util.DateMathParser;
import java.io.IOException;
import java.text.*;
import java.util.*;
public class DateField extends FieldType {
  public static TimeZone UTC = TimeZone.getTimeZone("UTC");
  protected static final TimeZone MATH_TZ = UTC;
  protected static final Locale MATH_LOCALE = Locale.US;
  protected static final TimeZone CANONICAL_TZ = UTC;
  protected static final Locale CANONICAL_LOCALE = Locale.US;
  protected void init(IndexSchema schema, Map<String,String> args) {
  }
  protected static String NOW = "NOW";
  protected static char Z = 'Z';
  public String toInternal(String val) {
    return toInternal(parseMath(null, val));
  }
  public Date parseMath(Date now, String val) {
    String math = null;
    final DateMathParser p = new DateMathParser(MATH_TZ, MATH_LOCALE);
    if (null != now) p.setNow(now);
    if (val.startsWith(NOW)) {
      math = val.substring(NOW.length());
    } else {
      final int zz = val.indexOf(Z);
      if (0 < zz) {
        math = val.substring(zz+1);
        try {
          p.setNow(parseDate(val.substring(0,zz+1)));
        } catch (ParseException e) {
          throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
                                   "Invalid Date in Date Math String:'"
                                   +val+'\'',e);
        }
      } else {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
                                 "Invalid Date String:'" +val+'\'');
      }
    }
    if (null == math || math.equals("")) {
      return p.getNow();
    }
    try {
      return p.parseMath(math);
    } catch (ParseException e) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
                               "Invalid Date Math String:'" +val+'\'',e);
    }
  }
  public String toInternal(Date val) {
    return formatDate(val);
  }
  public String indexedToReadable(String indexedForm) {
    return indexedForm + Z;
  }
  public String toExternal(Fieldable f) {
    return indexedToReadable(f.stringValue());
  }
  public Date toObject(String indexedForm) throws java.text.ParseException {
    return parseDate(indexedToReadable(indexedForm));
  }
  @Override
  public Date toObject(Fieldable f) {
    try {
      return parseDate( toExternal(f) );
    }
    catch( ParseException ex ) {
      throw new RuntimeException( ex );
    }
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    return getStringSort(field,reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    return new OrdFieldSource(field.name);
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeDate(name, toExternal(f));
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    writer.writeDate(name, toExternal(f));
  }
  protected DateFormat getThreadLocalDateFormat() {
    return fmtThreadLocal.get();
  }
  protected String formatDate(Date d) {
    return fmtThreadLocal.get().format(d);
  }
  public String toExternal(Date d) {
    return fmtThreadLocal.get().format(d) + 'Z';  
  }
   protected Date parseDate(String s) throws ParseException {
     return fmtThreadLocal.get().parse(s);
   }
   public Date parseDateLenient(String s, SolrQueryRequest req) throws ParseException {
     try {
       return fmtThreadLocal.get().parse(s);
     } catch (Exception e) {
       return DateUtil.parseDate(s);
     }
   }
  public Date parseMathLenient(Date now, String val, SolrQueryRequest req) {
    String math = null;
    final DateMathParser p = new DateMathParser(MATH_TZ, MATH_LOCALE);
    if (null != now) p.setNow(now);
    if (val.startsWith(NOW)) {
      math = val.substring(NOW.length());
    } else {
      final int zz = val.indexOf(Z);
      if (0 < zz) {
        math = val.substring(zz+1);
        try {
          p.setNow(parseDateLenient(val.substring(0,zz+1), req));
        } catch (ParseException e) {
          throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
                                   "Invalid Date in Date Math String:'"
                                   +val+'\'',e);
        }
      } else {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
                                 "Invalid Date String:'" +val+'\'');
      }
    }
    if (null == math || math.equals("")) {
      return p.getNow();
    }
    try {
      return p.parseMath(math);
    } catch (ParseException e) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
                               "Invalid Date Math String:'" +val+'\'',e);
    }
  }
  private final static ThreadLocalDateFormat fmtThreadLocal
    = new ThreadLocalDateFormat(new ISO8601CanonicalDateFormat());
  private static class ISO8601CanonicalDateFormat extends SimpleDateFormat {
    protected NumberFormat millisParser
      = NumberFormat.getIntegerInstance(CANONICAL_LOCALE);
    protected NumberFormat millisFormat = new DecimalFormat(".###", 
      new DecimalFormatSymbols(CANONICAL_LOCALE));
    public ISO8601CanonicalDateFormat() {
      super("yyyy-MM-dd'T'HH:mm:ss", CANONICAL_LOCALE);
      this.setTimeZone(CANONICAL_TZ);
    }
    public Date parse(String i, ParsePosition p) {
      Date d = super.parse(i, p);
      int milliIndex = p.getIndex();
      if (null != d &&
          -1 == p.getErrorIndex() &&
          milliIndex + 1 < i.length() &&
          '.' == i.charAt(milliIndex)) {
        p.setIndex( ++milliIndex ); 
        Number millis = millisParser.parse(i, p);
        if (-1 == p.getErrorIndex()) {
          int endIndex = p.getIndex();
            d = new Date(d.getTime()
                         + (long)(millis.doubleValue() *
                                  Math.pow(10, (3-endIndex+milliIndex))));
        }
      }
      return d;
    }
    public StringBuffer format(Date d, StringBuffer toAppendTo,
                               FieldPosition pos) {
      super.format(d, toAppendTo, pos);
      long millis = d.getTime() % 1000l;
      if (0l == millis) {
        return toAppendTo;
      }
      int posBegin = toAppendTo.length();
      toAppendTo.append(millisFormat.format(millis / 1000d));
      if (DateFormat.MILLISECOND_FIELD == pos.getField()) {
        pos.setBeginIndex(posBegin);
        pos.setEndIndex(toAppendTo.length());
      }
      return toAppendTo;
    }
    public Object clone() {
      ISO8601CanonicalDateFormat c
        = (ISO8601CanonicalDateFormat) super.clone();
      c.millisParser = NumberFormat.getIntegerInstance(CANONICAL_LOCALE);
      c.millisFormat = new DecimalFormat(".###", 
        new DecimalFormatSymbols(CANONICAL_LOCALE));
      return c;
    }
  }
  private static class ThreadLocalDateFormat extends ThreadLocal<DateFormat> {
    DateFormat proto;
    public ThreadLocalDateFormat(DateFormat d) {
      super();
      proto = d;
    }
    protected DateFormat initialValue() {
      return (DateFormat) proto.clone();
    }
  }
  @Override
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    return new DateFieldSource(field.getName(), field.getType());
  }
  public Query getRangeQuery(QParser parser, SchemaField sf, Date part1, Date part2, boolean minInclusive, boolean maxInclusive) {
    return new TermRangeQuery(
            sf.getName(),
            part1 == null ? null : toInternal(part1),
            part2 == null ? null : toInternal(part2),
            minInclusive, maxInclusive);
  }
}
class DateFieldSource extends FieldCacheSource {
  FieldType ft;
  public DateFieldSource(String name, FieldType ft) {
    super(name);
    this.ft = ft;
  }
  public String description() {
    return "date(" + field + ')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    return new StringIndexDocValues(this, reader, field) {
      protected String toTerm(String readableValue) {
        return ft.toInternal(readableValue);
      }
      public float floatVal(int doc) {
        return (float)intVal(doc);
      }
      public int intVal(int doc) {
        int ord=order[doc];
        return ord;
      }
      public long longVal(int doc) {
        return (long)intVal(doc);
      }
      public double doubleVal(int doc) {
        return (double)intVal(doc);
      }
      public String strVal(int doc) {
        int ord=order[doc];
        return ft.indexedToReadable(lookup[ord]);
      }
      public String toString(int doc) {
        return description() + '=' + intVal(doc);
      }
    };
  }
  public boolean equals(Object o) {
    return o instanceof DateFieldSource
            && super.equals(o);
  }
  private static int hcode = DateFieldSource.class.hashCode();
  public int hashCode() {
    return hcode + super.hashCode();
  };
}