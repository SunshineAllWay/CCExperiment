package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.ValueSource;
import java.io.IOException;
import java.util.Map;
public class OrdFieldSource extends ValueSource {
  protected String field;
  public OrdFieldSource(String field) {
    this.field = field;
  }
  public String description() {
    return "ord(" + field + ')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    return new StringIndexDocValues(this, reader, field) {
      protected String toTerm(String readableValue) {
        return readableValue;
      }
      public float floatVal(int doc) {
        return (float)order[doc];
      }
      public int intVal(int doc) {
        return order[doc];
      }
      public long longVal(int doc) {
        return (long)order[doc];
      }
      public double doubleVal(int doc) {
        return (double)order[doc];
      }
      public String strVal(int doc) {
        return Integer.toString(order[doc]);
      }
      public String toString(int doc) {
        return description() + '=' + intVal(doc);
      }
    };
  }
  public boolean equals(Object o) {
    return o.getClass() == OrdFieldSource.class && this.field.equals(((OrdFieldSource)o).field);
  }
  private static final int hcode = OrdFieldSource.class.hashCode();
  public int hashCode() {
    return hcode + field.hashCode();
  };
}
