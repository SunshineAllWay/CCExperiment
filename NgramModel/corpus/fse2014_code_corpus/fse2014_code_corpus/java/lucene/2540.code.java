package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.ValueSource;
import org.apache.lucene.search.FieldCache;
import java.io.IOException;
import java.util.Map;
public class ReverseOrdFieldSource extends ValueSource {
  public String field;
  public ReverseOrdFieldSource(String field) {
    this.field = field;
  }
  public String description() {
    return "rord("+field+')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final FieldCache.StringIndex sindex = FieldCache.DEFAULT.getStringIndex(reader, field);
    final int arr[] = sindex.order;
    final int end = sindex.lookup.length;
    return new DocValues() {
      public float floatVal(int doc) {
        return (float)(end - arr[doc]);
      }
      public int intVal(int doc) {
        return (int)(end - arr[doc]);
      }
      public long longVal(int doc) {
        return (long)(end - arr[doc]);
      }
      public double doubleVal(int doc) {
        return (double)(end - arr[doc]);
      }
      public String strVal(int doc) {
        return Integer.toString((end - arr[doc]));
      }
      public String toString(int doc) {
        return description() + '=' + strVal(doc);
      }
    };
  }
  public boolean equals(Object o) {
    if (o.getClass() !=  ReverseOrdFieldSource.class) return false;
    ReverseOrdFieldSource other = (ReverseOrdFieldSource)o;
    return this.field.equals(other.field);
  }
  private static final int hcode = ReverseOrdFieldSource.class.hashCode();
  public int hashCode() {
    return hcode + field.hashCode();
  };
}
