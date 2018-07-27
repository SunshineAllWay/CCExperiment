package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.search.function.DocValues;
import org.apache.lucene.search.FieldCache;
import java.io.IOException;
import java.util.Map;
public class FloatFieldSource extends FieldCacheSource {
  protected FieldCache.FloatParser parser;
  public FloatFieldSource(String field) {
    this(field, null);
  }
  public FloatFieldSource(String field, FieldCache.FloatParser parser) {
    super(field);
    this.parser = parser;
  }
  public String description() {
    return "float(" + field + ')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final float[] arr = (parser==null) ?
            cache.getFloats(reader, field) :
            cache.getFloats(reader, field, parser);
    return new DocValues() {
      public float floatVal(int doc) {
        return arr[doc];
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
        return description() + '=' + floatVal(doc);
      }
    };
  }
  public boolean equals(Object o) {
    if (o.getClass() !=  FloatFieldSource.class) return false;
    FloatFieldSource other = (FloatFieldSource)o;
    return super.equals(other)
           && this.parser==null ? other.parser==null :
              this.parser.getClass() == other.parser.getClass();
  }
  public int hashCode() {
    int h = parser==null ? Float.class.hashCode() : parser.getClass().hashCode();
    h += super.hashCode();
    return h;
  };
}