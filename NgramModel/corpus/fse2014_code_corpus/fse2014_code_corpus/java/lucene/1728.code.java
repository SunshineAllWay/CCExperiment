package org.apache.lucene.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.function.DocValues;
import java.io.IOException;
public class IntFieldSource extends FieldCacheSource {
  private FieldCache.IntParser parser;
  public IntFieldSource(String field) {
    this(field, null);
  }
  public IntFieldSource(String field, FieldCache.IntParser parser) {
    super(field);
    this.parser = parser;
  }
  @Override
  public String description() {
    return "int(" + super.description() + ')';
  }
  @Override
  public DocValues getCachedFieldValues (FieldCache cache, String field, IndexReader reader) throws IOException {
    final int[] arr = cache.getInts(reader, field, parser);
    return new DocValues() {
      @Override
      public float floatVal(int doc) { 
        return arr[doc]; 
      }
      @Override
      public  int intVal(int doc) { 
        return arr[doc]; 
      }
      @Override
      public String toString(int doc) { 
        return  description() + '=' + intVal(doc);  
      }
      @Override
      Object getInnerArray() {
        return arr;
      }
    };
  }
  @Override
  public boolean cachedFieldSourceEquals(FieldCacheSource o) {
    if (o.getClass() !=  IntFieldSource.class) {
      return false;
    }
    IntFieldSource other = (IntFieldSource)o;
    return this.parser==null ? 
      other.parser==null :
      this.parser.getClass() == other.parser.getClass();
  }
  @Override
  public int cachedFieldSourceHashCode() {
    return parser==null ? 
      Integer.class.hashCode() : parser.getClass().hashCode();
  }
}
