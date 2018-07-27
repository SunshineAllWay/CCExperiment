package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.search.function.DocValues;
import org.apache.lucene.search.FieldCache;
import java.io.IOException;
import java.util.Map;
public class IntFieldSource extends FieldCacheSource {
  FieldCache.IntParser parser;
  public IntFieldSource(String field) {
    this(field, null);
  }
  public IntFieldSource(String field, FieldCache.IntParser parser) {
    super(field);
    this.parser = parser;
  }
  public String description() {
    return "int(" + field + ')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final int[] arr = (parser==null) ?
            cache.getInts(reader, field) :
            cache.getInts(reader, field, parser);
    return new DocValues() {
      public float floatVal(int doc) {
        return (float)arr[doc];
      }
      public int intVal(int doc) {
        return (int)arr[doc];
      }
      public long longVal(int doc) {
        return (long)arr[doc];
      }
      public double doubleVal(int doc) {
        return (double)arr[doc];
      }
      public String strVal(int doc) {
        return Float.toString(arr[doc]);
      }
      public String toString(int doc) {
        return description() + '=' + intVal(doc);
      }
      @Override
      public ValueSourceScorer getRangeScorer(IndexReader reader, String lowerVal, String upperVal, boolean includeLower, boolean includeUpper) {
        int lower,upper;
        if (lowerVal==null) {
          lower = Integer.MIN_VALUE;
        } else {
          lower = Integer.parseInt(lowerVal);
          if (!includeLower && lower < Integer.MAX_VALUE) lower++;
        }
         if (upperVal==null) {
          upper = Integer.MAX_VALUE;
        } else {
          upper = Integer.parseInt(upperVal);
          if (!includeUpper && upper > Integer.MIN_VALUE) upper--;
        }
        final int ll = lower;
        final int uu = upper;
        return new ValueSourceScorer(reader, this) {
          @Override
          public boolean matchesValue(int doc) {
            int val = arr[doc];
            return val >= ll && val <= uu;
          }
        };
      }
    };
  }
  public boolean equals(Object o) {
    if (o.getClass() !=  IntFieldSource.class) return false;
    IntFieldSource other = (IntFieldSource)o;
    return super.equals(other)
           && this.parser==null ? other.parser==null :
              this.parser.getClass() == other.parser.getClass();
  }
  public int hashCode() {
    int h = parser==null ? Integer.class.hashCode() : parser.getClass().hashCode();
    h += super.hashCode();
    return h;
  };
}
