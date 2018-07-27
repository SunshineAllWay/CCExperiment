package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.solr.common.SolrException;
import java.io.IOException;
import java.util.Map;
public class QueryValueSource extends ValueSource {
  final Query q;
  final float defVal;
  public QueryValueSource(Query q, float defVal) {
    this.q = q;
    this.defVal = defVal;
  }
  public Query getQuery() { return q; }
  public float getDefaultValue() { return defVal; }
  public String description() {
    return "query(" + q + ",def=" + defVal + ")";
  }
  @Override
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    return new QueryDocValues(reader, q, defVal, context==null ? null : (Weight)context.get(this));
  }
  public int hashCode() {
    return q.hashCode() * 29;
  }
  public boolean equals(Object o) {
    if (QueryValueSource.class != o.getClass()) return false;
    QueryValueSource other = (QueryValueSource)o;
    return this.q.equals(other.q) && this.defVal==other.defVal;
  }
  @Override
  public void createWeight(Map context, Searcher searcher) throws IOException {
    Weight w = q.weight(searcher);
    context.put(this, w);
  }
}
class QueryDocValues extends DocValues {
  final Query q;
  final IndexReader reader;
  final Weight weight;
  final float defVal;
  Scorer scorer;
  int scorerDoc; 
  int lastDocRequested=Integer.MAX_VALUE;
  public QueryDocValues(IndexReader reader, Query q, float defVal, Weight w) throws IOException {
    this.reader = reader;
    this.q = q;
    this.defVal = defVal;
    weight = w!=null ? w : q.weight(new IndexSearcher(reader));
  }
  public float floatVal(int doc) {
    try {
      if (doc < lastDocRequested) {
        scorer = weight.scorer(reader, true, false);
        if (scorer==null) return defVal;
        scorerDoc = -1;
      }
      lastDocRequested = doc;
      if (scorerDoc < doc) {
        scorerDoc = scorer.advance(doc);
      }
      if (scorerDoc > doc) {
        return defVal;
      }
      return scorer.score();
    } catch (IOException e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "caught exception in QueryDocVals("+q+") doc="+doc, e);
    }
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
    return "query(" + q + ",def=" + defVal + ")=" + floatVal(doc);
  }
}