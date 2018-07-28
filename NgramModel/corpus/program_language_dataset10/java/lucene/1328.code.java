package org.apache.lucene.queryParser.surround.query;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
public class SpanNearClauseFactory {
  public SpanNearClauseFactory(IndexReader reader, String fieldName, BasicQueryFactory qf) {
    this.reader = reader;
    this.fieldName = fieldName;
    this.weightBySpanQuery = new HashMap<SpanQuery, Float>(); 
    this.qf = qf;
  }
  private IndexReader reader;
  private String fieldName;
  private HashMap<SpanQuery, Float> weightBySpanQuery;
  private BasicQueryFactory qf;
  public IndexReader getIndexReader() {return reader;}
  public String getFieldName() {return fieldName;}
  public BasicQueryFactory getBasicQueryFactory() {return qf;}
  public TermEnum getTermEnum(String termText) throws IOException {
    return getIndexReader().terms(new Term(getFieldName(), termText));
  }
  public int size() {return weightBySpanQuery.size();}
  public void clear() {weightBySpanQuery.clear();}
  protected void addSpanQueryWeighted(SpanQuery sq, float weight) {
    Float w = weightBySpanQuery.get(sq);
    if (w != null)
      w = Float.valueOf(w.floatValue() + weight);
    else
      w = Float.valueOf(weight);
    weightBySpanQuery.put(sq, w); 
  }
  public void addTermWeighted(Term t, float weight) throws IOException {   
    SpanTermQuery stq = qf.newSpanTermQuery(t);
    addSpanQueryWeighted(stq, weight);
  }
  public void addSpanNearQuery(Query q) {
    if (q == SrndQuery.theEmptyLcnQuery)
      return;
    if (! (q instanceof SpanNearQuery))
      throw new AssertionError("Expected SpanNearQuery: " + q.toString(getFieldName()));
    addSpanQueryWeighted((SpanNearQuery)q, q.getBoost());
  }
  public SpanQuery makeSpanNearClause() {
    SpanQuery [] spanQueries = new SpanQuery[size()];
    Iterator<SpanQuery> sqi = weightBySpanQuery.keySet().iterator();
    int i = 0;
    while (sqi.hasNext()) {
      SpanQuery sq = sqi.next();
      sq.setBoost(weightBySpanQuery.get(sq).floatValue());
      spanQueries[i++] = sq;
    }
    if (spanQueries.length == 1)
      return spanQueries[0];
    else
      return new SpanOrQuery(spanQueries);
  }
}
