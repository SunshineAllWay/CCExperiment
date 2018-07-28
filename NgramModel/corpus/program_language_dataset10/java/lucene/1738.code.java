package org.apache.lucene.search.payloads;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.spans.NearSpansOrdered;
import org.apache.lucene.search.spans.NearSpansUnordered;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanScorer;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.ToStringUtils;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
public class PayloadNearQuery extends SpanNearQuery {
  protected String fieldName;
  protected PayloadFunction function;
  public PayloadNearQuery(SpanQuery[] clauses, int slop, boolean inOrder) {
    this(clauses, slop, inOrder, new AveragePayloadFunction());
  }
  public PayloadNearQuery(SpanQuery[] clauses, int slop, boolean inOrder,
      PayloadFunction function) {
    super(clauses, slop, inOrder);
    fieldName = clauses[0].getField(); 
    this.function = function;
  }
  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    return new PayloadNearSpanWeight(this, searcher);
  }
  @Override
  public Object clone() {
    int sz = clauses.size();
    SpanQuery[] newClauses = new SpanQuery[sz];
    for (int i = 0; i < sz; i++) {
      newClauses[i] = (SpanQuery) clauses.get(i).clone();
    }
    PayloadNearQuery boostingNearQuery = new PayloadNearQuery(newClauses, slop,
        inOrder);
    boostingNearQuery.setBoost(getBoost());
    return boostingNearQuery;
  }
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("payloadNear([");
    Iterator<SpanQuery> i = clauses.iterator();
    while (i.hasNext()) {
      SpanQuery clause = i.next();
      buffer.append(clause.toString(field));
      if (i.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append("], ");
    buffer.append(slop);
    buffer.append(", ");
    buffer.append(inOrder);
    buffer.append(")");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
    result = prime * result + ((function == null) ? 0 : function.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    PayloadNearQuery other = (PayloadNearQuery) obj;
    if (fieldName == null) {
      if (other.fieldName != null)
        return false;
    } else if (!fieldName.equals(other.fieldName))
      return false;
    if (function == null) {
      if (other.function != null)
        return false;
    } else if (!function.equals(other.function))
      return false;
    return true;
  }
  public class PayloadNearSpanWeight extends SpanWeight {
    public PayloadNearSpanWeight(SpanQuery query, Searcher searcher)
        throws IOException {
      super(query, searcher);
    }
    @Override
    public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder,
        boolean topScorer) throws IOException {
      return new PayloadNearSpanScorer(query.getSpans(reader), this,
          similarity, reader.norms(query.getField()));
    }
  }
  public class PayloadNearSpanScorer extends SpanScorer {
    Spans spans;
    protected float payloadScore;
    private int payloadsSeen;
    Similarity similarity = getSimilarity();
    protected PayloadNearSpanScorer(Spans spans, Weight weight,
        Similarity similarity, byte[] norms) throws IOException {
      super(spans, weight, similarity, norms);
      this.spans = spans;
    }
    public void getPayloads(Spans[] subSpans) throws IOException {
      for (int i = 0; i < subSpans.length; i++) {
        if (subSpans[i] instanceof NearSpansOrdered) {
          if (((NearSpansOrdered) subSpans[i]).isPayloadAvailable()) {
            processPayloads(((NearSpansOrdered) subSpans[i]).getPayload(),
                subSpans[i].start(), subSpans[i].end());
          }
          getPayloads(((NearSpansOrdered) subSpans[i]).getSubSpans());
        } else if (subSpans[i] instanceof NearSpansUnordered) {
          if (((NearSpansUnordered) subSpans[i]).isPayloadAvailable()) {
            processPayloads(((NearSpansUnordered) subSpans[i]).getPayload(),
                subSpans[i].start(), subSpans[i].end());
          }
          getPayloads(((NearSpansUnordered) subSpans[i]).getSubSpans());
        }
      }
    }
    protected void processPayloads(Collection<byte[]> payLoads, int start, int end) {
      for (final byte[] thePayload : payLoads) {
        payloadScore = function.currentScore(doc, fieldName, start, end,
            payloadsSeen, payloadScore, similarity.scorePayload(doc, fieldName,
                spans.start(), spans.end(), thePayload, 0, thePayload.length));
        ++payloadsSeen;
      }
    }
    @Override
    protected boolean setFreqCurrentDoc() throws IOException {
      if (!more) {
        return false;
      }
      Spans[] spansArr = new Spans[1];
      spansArr[0] = spans;
      payloadScore = 0;
      payloadsSeen = 0;
      getPayloads(spansArr);
      return super.setFreqCurrentDoc();
    }
    @Override
    public float score() throws IOException {
      return super.score()
          * function.docScore(doc, fieldName, payloadsSeen, payloadScore);
    }
    @Override
    protected Explanation explain(int doc) throws IOException {
      Explanation result = new Explanation();
      Explanation nonPayloadExpl = super.explain(doc);
      result.addDetail(nonPayloadExpl);
      Explanation payloadBoost = new Explanation();
      result.addDetail(payloadBoost);
      float avgPayloadScore = (payloadsSeen > 0 ? (payloadScore / payloadsSeen)
          : 1);
      payloadBoost.setValue(avgPayloadScore);
      payloadBoost.setDescription("scorePayload(...)");
      result.setValue(nonPayloadExpl.getValue() * avgPayloadScore);
      result.setDescription("bnq, product of:");
      return result;
    }
  }
}
