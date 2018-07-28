package org.apache.lucene.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.function.DocValues;
import java.io.IOException;
public class ShortFieldSource extends FieldCacheSource {
  private FieldCache.ShortParser parser;
  public ShortFieldSource(String field) {
    this(field, null);
  }
  public ShortFieldSource(String field, FieldCache.ShortParser parser) {
    super(field);
    this.parser = parser;
  }
  @Override
  public String description() {
    return "short(" + super.description() + ')';
  }
  @Override
  public DocValues getCachedFieldValues (FieldCache cache, String field, IndexReader reader) throws IOException {
    final short[] arr = cache.getShorts(reader, field, parser);
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
    if (o.getClass() !=  ShortFieldSource.class) {
      return false;
    }
    ShortFieldSource other = (ShortFieldSource)o;
    return this.parser==null ? 
      other.parser==null :
      this.parser.getClass() == other.parser.getClass();
  }
  @Override
  public int cachedFieldSourceHashCode() {
    return parser==null ? 
      Short.class.hashCode() : parser.getClass().hashCode();
  }
}
