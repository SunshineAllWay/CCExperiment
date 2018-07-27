package org.apache.solr.search.function;
import org.apache.lucene.search.*;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.util.NumberUtils;
import java.io.IOException;
public abstract class DocValues {
  public byte byteVal(int doc) { throw new UnsupportedOperationException(); }
  public short shortVal(int doc) { throw new UnsupportedOperationException(); }
  public float floatVal(int doc) { throw new UnsupportedOperationException(); }
  public int intVal(int doc) { throw new UnsupportedOperationException(); }
  public long longVal(int doc) { throw new UnsupportedOperationException(); }
  public double doubleVal(int doc) { throw new UnsupportedOperationException(); }
  public String strVal(int doc) { throw new UnsupportedOperationException(); }
  public abstract String toString(int doc);
  public void byteVal(int doc, byte [] vals) { throw new UnsupportedOperationException(); }
  public void shortVal(int doc, short [] vals) { throw new UnsupportedOperationException(); }
  public void floatVal(int doc, float [] vals) { throw new UnsupportedOperationException(); }
  public void intVal(int doc, int [] vals) { throw new UnsupportedOperationException(); }
  public void longVal(int doc, long [] vals) { throw new UnsupportedOperationException(); }
  public void doubleVal(int doc, double [] vals) { throw new UnsupportedOperationException(); }
  public void strVal(int doc, String [] vals) { throw new UnsupportedOperationException(); }
  public Explanation explain(int doc) {
    return new Explanation(floatVal(doc), toString(doc));
  }
  public ValueSourceScorer getScorer(IndexReader reader) {
    return new ValueSourceScorer(reader, this);
  }
  public ValueSourceScorer getRangeScorer(IndexReader reader, String lowerVal, String upperVal, boolean includeLower, boolean includeUpper) {
    float lower;
    float upper;
    if (lowerVal == null) {
      lower = Float.NEGATIVE_INFINITY;
    } else {
      lower = Float.parseFloat(lowerVal);
    }
    if (upperVal == null) {
      upper = Float.POSITIVE_INFINITY;
    } else {
      upper = Float.parseFloat(upperVal);
    }
    final float l = lower;
    final float u = upper;
    if (includeLower && includeUpper) {
      return new ValueSourceScorer(reader, this) {
        @Override
        public boolean matchesValue(int doc) {
          float docVal = floatVal(doc);
          return docVal >= l && docVal <= u;
        }
      };
    }
    else if (includeLower && !includeUpper) {
       return new ValueSourceScorer(reader, this) {
        @Override
        public boolean matchesValue(int doc) {
          float docVal = floatVal(doc);
          return docVal >= l && docVal < u;
        }
      };
    }
    else if (!includeLower && includeUpper) {
       return new ValueSourceScorer(reader, this) {
        @Override
        public boolean matchesValue(int doc) {
          float docVal = floatVal(doc);
          return docVal > l && docVal <= u;
        }
      };
    }
    else {
       return new ValueSourceScorer(reader, this) {
        @Override
        public boolean matchesValue(int doc) {
          float docVal = floatVal(doc);
          return docVal > l && docVal < u;
        }
      };
    }
  }
}
