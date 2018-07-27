package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.SortField;
import java.io.IOException;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
public abstract class ValueSource implements Serializable {
  @Deprecated
  public DocValues getValues(IndexReader reader) throws IOException {
    return getValues(null, reader);
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    return getValues(reader);
  }
  public abstract boolean equals(Object o);
  public abstract int hashCode();
  public abstract String description();
  public String toString() {
    return description();
  }
  public SortField getSortField(boolean reverse) throws IOException {
    return new SortField(description(), new ValueSourceComparatorSource(), reverse);
  }
  public void createWeight(Map context, Searcher searcher) throws IOException {
  }
  public static Map newContext() {
    return new IdentityHashMap();
  }
  class ValueSourceComparatorSource extends FieldComparatorSource {
    public ValueSourceComparatorSource() {
    }
    public FieldComparator newComparator(String fieldname, int numHits,
                                         int sortPos, boolean reversed) throws IOException {
      return new ValueSourceComparator(numHits);
    }
  }
  class ValueSourceComparator extends FieldComparator {
    private final double[] values;
    private DocValues docVals;
    private double bottom;
    ValueSourceComparator(int numHits) {
      values = new double[numHits];
    }
    public int compare(int slot1, int slot2) {
      final double v1 = values[slot1];
      final double v2 = values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    public int compareBottom(int doc) {
      final double v2 = docVals.doubleVal(doc);
      if (bottom > v2) {
        return 1;
      } else if (bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    public void copy(int slot, int doc) {
      values[slot] = docVals.doubleVal(doc);
    }
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      docVals = getValues(Collections.emptyMap(), reader);
    }
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    public Comparable value(int slot) {
      return Double.valueOf(values[slot]);
    }
  }
}
class ValueSourceScorer extends Scorer {
  protected IndexReader reader;
  private int doc = -1;
  protected final int maxDoc;
  protected final DocValues values;
  protected boolean checkDeletes;
  protected ValueSourceScorer(IndexReader reader, DocValues values) {
    super(null);
    this.reader = reader;
    this.maxDoc = reader.maxDoc();
    this.values = values;
    setCheckDeletes(true);
  }
  public IndexReader getReader() {
    return reader;
  }
  public void setCheckDeletes(boolean checkDeletes) {
    this.checkDeletes = checkDeletes && reader.hasDeletions();
  }
  public boolean matches(int doc) {
    return (!checkDeletes || !reader.isDeleted(doc)) && matchesValue(doc);
  }
  public boolean matchesValue(int doc) {
    return true;
  }
  @Override
  public int docID() {
    return doc;
  }
  @Override
  public int nextDoc() throws IOException {
    for (; ;) {
      doc++;
      if (doc >= maxDoc) return doc = NO_MORE_DOCS;
      if (matches(doc)) return doc;
    }
  }
  @Override
  public int advance(int target) throws IOException {
    doc = target - 1;
    return nextDoc();
  }
  public int doc() {
    return doc;
  }
  public boolean next() {
    for (; ;) {
      doc++;
      if (doc >= maxDoc) return false;
      if (matches(doc)) return true;
    }
  }
  public boolean skipTo(int target) {
    doc = target - 1;
    return next();
  }
  public float score() throws IOException {
    return values.floatVal(doc);
  }
  public Explanation explain(int doc) throws IOException {
    return values.explain(doc);
  }
}
