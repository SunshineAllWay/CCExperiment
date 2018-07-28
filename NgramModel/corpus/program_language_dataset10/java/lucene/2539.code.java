package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;
import java.io.IOException;
import java.util.Map;
public class ReciprocalFloatFunction extends ValueSource {
  protected final ValueSource source;
  protected final float m;
  protected final float a;
  protected final float b;
  public ReciprocalFloatFunction(ValueSource source, float m, float a, float b) {
    this.source=source;
    this.m=m;
    this.a=a;
    this.b=b;
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final DocValues vals = source.getValues(context, reader);
    return new DocValues() {
      public float floatVal(int doc) {
        return a/(m*vals.floatVal(doc) + b);
      }
      public int intVal(int doc) {
        return (int)floatVal(doc);
      }
      public long longVal(int doc) {
        return (long)floatVal(doc);
      }
      public double doubleVal(int doc) {
        return (double)floatVal(doc);
      }
      public String strVal(int doc) {
        return Float.toString(floatVal(doc));
      }
      public String toString(int doc) {
        return Float.toString(a) + "/("
                + m + "*float(" + vals.toString(doc) + ')'
                + '+' + b + ')';
      }
    };
  }
  @Override
  public void createWeight(Map context, Searcher searcher) throws IOException {
    source.createWeight(context, searcher);
  }
  public String description() {
    return Float.toString(a) + "/("
           + m + "*float(" + source.description() + ")"
           + "+" + b + ')';
  }
  public int hashCode() {
    int h = Float.floatToIntBits(a) + Float.floatToIntBits(m);
    h ^= (h << 13) | (h >>> 20);
    return h + (Float.floatToIntBits(b)) + source.hashCode();
  }
  public boolean equals(Object o) {
    if (ReciprocalFloatFunction.class != o.getClass()) return false;
    ReciprocalFloatFunction other = (ReciprocalFloatFunction)o;
    return this.m == other.m
            && this.a == other.a
            && this.b == other.b
            && this.source.equals(other.source);
  }
}
