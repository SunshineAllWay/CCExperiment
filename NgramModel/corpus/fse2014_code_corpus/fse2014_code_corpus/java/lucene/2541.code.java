package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;
import java.io.IOException;
import java.util.Map;
public class ScaleFloatFunction extends ValueSource {
  protected final ValueSource source;
  protected final float min;
  protected final float max;
  public ScaleFloatFunction(ValueSource source, float min, float max) {
    this.source = source;
    this.min = min;
    this.max = max;
  }
  public String description() {
    return "scale(" + source.description() + "," + min + "," + max + ")";
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final DocValues vals =  source.getValues(context, reader);
    int maxDoc = reader.maxDoc();
    float minVal=0.0f;
    float maxVal=0.0f;
    if (maxDoc>0) {
      minVal = maxVal = vals.floatVal(0);      
    }
    for (int i=0; i<maxDoc; i++) {
      float val = vals.floatVal(i);
      if ((Float.floatToRawIntBits(val) & (0xff<<23)) == 0xff<<23) {
        continue;
      }
      if (val < minVal) {
        minVal = val;
      } else if (val > maxVal) {
        maxVal = val;
      }
    }
    final float scale = (maxVal-minVal==0) ? 0 : (max-min)/(maxVal-minVal);
    final float minSource = minVal;
    final float maxSource = maxVal;
    return new DocValues() {
      public float floatVal(int doc) {
	return (vals.floatVal(doc) - minSource) * scale + min;
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
	return "scale(" + vals.toString(doc) + ",toMin=" + min + ",toMax=" + max
                + ",fromMin=" + minSource
                + ",fromMax=" + maxSource
                + ")";
      }
    };
  }
  @Override
  public void createWeight(Map context, Searcher searcher) throws IOException {
    source.createWeight(context, searcher);
  }
  public int hashCode() {
    int h = Float.floatToIntBits(min);
    h = h*29;
    h += Float.floatToIntBits(max);
    h = h*29;
    h += source.hashCode();
    return h;
  }
  public boolean equals(Object o) {
    if (ScaleFloatFunction.class != o.getClass()) return false;
    ScaleFloatFunction other = (ScaleFloatFunction)o;
    return this.min == other.min
         && this.max == other.max
         && this.source.equals(other.source);
  }
}
