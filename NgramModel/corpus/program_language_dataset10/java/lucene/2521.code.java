package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import java.io.IOException;
import java.util.Map;
public class DoubleFieldSource extends FieldCacheSource {
  protected FieldCache.DoubleParser parser;
  public DoubleFieldSource(String field) {
    this(field, null);
  }
  public DoubleFieldSource(String field, FieldCache.DoubleParser parser) {
    super(field);
    this.parser = parser;
  }
  public String description() {
    return "double(" + field + ')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final double[] arr = (parser == null) ?
            ((FieldCache) cache).getDoubles(reader, field) :
            ((FieldCache) cache).getDoubles(reader, field, parser);
    return new DocValues() {
      public float floatVal(int doc) {
        return (float) arr[doc];
      }
      public int intVal(int doc) {
        return (int) arr[doc];
      }
      public long longVal(int doc) {
        return (long) arr[doc];
      }
      public double doubleVal(int doc) {
        return arr[doc];
      }
      public String strVal(int doc) {
        return Double.toString(arr[doc]);
      }
      public String toString(int doc) {
        return description() + '=' + doubleVal(doc);
      }
      @Override
      public ValueSourceScorer getRangeScorer(IndexReader reader, String lowerVal, String upperVal, boolean includeLower, boolean includeUpper) {
        double lower,upper;
        if (lowerVal==null) {
          lower = Double.NEGATIVE_INFINITY;
        } else {
          lower = Double.parseDouble(lowerVal);
        }
         if (upperVal==null) {
          upper = Double.POSITIVE_INFINITY;
        } else {
          upper = Double.parseDouble(upperVal);
        }
        final double l = lower;
        final double u = upper;
        if (includeLower && includeUpper) {
          return new ValueSourceScorer(reader, this) {
            @Override
            public boolean matchesValue(int doc) {
              double docVal = doubleVal(doc);
              return docVal >= l && docVal <= u;
            }
          };
        }
        else if (includeLower && !includeUpper) {
          return new ValueSourceScorer(reader, this) {
            @Override
            public boolean matchesValue(int doc) {
              double docVal = doubleVal(doc);
              return docVal >= l && docVal < u;
            }
          };
        }
        else if (!includeLower && includeUpper) {
          return new ValueSourceScorer(reader, this) {
            @Override
            public boolean matchesValue(int doc) {
              double docVal = doubleVal(doc);
              return docVal > l && docVal <= u;
            }
          };
        }
        else {
          return new ValueSourceScorer(reader, this) {
            @Override
            public boolean matchesValue(int doc) {
              double docVal = doubleVal(doc);
              return docVal > l && docVal < u;
            }
          };
        }
      }
      };
  }
  public boolean equals(Object o) {
    if (o.getClass() != DoubleFieldSource.class) return false;
    DoubleFieldSource other = (DoubleFieldSource) o;
    return super.equals(other)
            && this.parser == null ? other.parser == null :
            this.parser.getClass() == other.parser.getClass();
  }
  public int hashCode() {
    int h = parser == null ? Double.class.hashCode() : parser.getClass().hashCode();
    h += super.hashCode();
    return h;
  }
}
