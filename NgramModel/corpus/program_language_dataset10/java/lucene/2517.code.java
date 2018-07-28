package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import java.io.IOException;
import java.util.Map;
public class ByteFieldSource extends FieldCacheSource {
  FieldCache.ByteParser parser;
  public ByteFieldSource(String field) {
    this(field, null);
  }
  public ByteFieldSource(String field, FieldCache.ByteParser parser) {
    super(field);
    this.parser = parser;
  }
  public String description() {
    return "byte(" + field + ')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final byte[] arr = (parser == null) ?
            cache.getBytes(reader, field) :
            cache.getBytes(reader, field, parser);
    return new DocValues() {
      @Override
      public byte byteVal(int doc) {
        return (byte) arr[doc];
      }
      @Override
      public short shortVal(int doc) {
        return (short) arr[doc];
      }
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
        return (double) arr[doc];
      }
      public String strVal(int doc) {
        return Byte.toString(arr[doc]);
      }
      public String toString(int doc) {
        return description() + '=' + byteVal(doc);
      }
    };
  }
  public boolean equals(Object o) {
    if (o.getClass() != ByteFieldSource.class) return false;
    ByteFieldSource
            other = (ByteFieldSource) o;
    return super.equals(other)
            && this.parser == null ? other.parser == null :
            this.parser.getClass() == other.parser.getClass();
  }
  public int hashCode() {
    int h = parser == null ? Byte.class.hashCode() : parser.getClass().hashCode();
    h += super.hashCode();
    return h;
  }
}