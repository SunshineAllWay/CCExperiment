package org.apache.lucene.search;
import java.io.IOException;
import java.util.Set;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation.IDFExplanation;
import org.apache.lucene.util.ToStringUtils;
public class TermQuery extends Query {
  private Term term;
  private class TermWeight extends Weight {
    private final Similarity similarity;
    private float value;
    private float idf;
    private float queryNorm;
    private float queryWeight;
    private IDFExplanation idfExp;
    public TermWeight(Searcher searcher)
      throws IOException {
      this.similarity = getSimilarity(searcher);
      idfExp = similarity.idfExplain(term, searcher);
      idf = idfExp.getIdf();
    }
    @Override
    public String toString() { return "weight(" + TermQuery.this + ")"; }
    @Override
    public Query getQuery() { return TermQuery.this; }
    @Override
    public float getValue() { return value; }
    @Override
    public float sumOfSquaredWeights() {
      queryWeight = idf * getBoost();             
      return queryWeight * queryWeight;           
    }
    @Override
    public void normalize(float queryNorm) {
      this.queryNorm = queryNorm;
      queryWeight *= queryNorm;                   
      value = queryWeight * idf;                  
    }
    @Override
    public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
      TermDocs termDocs = reader.termDocs(term);
      if (termDocs == null)
        return null;
      return new TermScorer(this, termDocs, similarity, reader.norms(term.field()));
    }
    @Override
    public Explanation explain(IndexReader reader, int doc)
      throws IOException {
      ComplexExplanation result = new ComplexExplanation();
      result.setDescription("weight("+getQuery()+" in "+doc+"), product of:");
      Explanation expl = new Explanation(idf, idfExp.explain());
      Explanation queryExpl = new Explanation();
      queryExpl.setDescription("queryWeight(" + getQuery() + "), product of:");
      Explanation boostExpl = new Explanation(getBoost(), "boost");
      if (getBoost() != 1.0f)
        queryExpl.addDetail(boostExpl);
      queryExpl.addDetail(expl);
      Explanation queryNormExpl = new Explanation(queryNorm,"queryNorm");
      queryExpl.addDetail(queryNormExpl);
      queryExpl.setValue(boostExpl.getValue() *
                         expl.getValue() *
                         queryNormExpl.getValue());
      result.addDetail(queryExpl);
      String field = term.field();
      ComplexExplanation fieldExpl = new ComplexExplanation();
      fieldExpl.setDescription("fieldWeight("+term+" in "+doc+
                               "), product of:");
      Explanation tfExplanation = new Explanation();
      int tf = 0;
      TermDocs termDocs = reader.termDocs(term);
      if (termDocs != null) {
        try {
          if (termDocs.skipTo(doc) && termDocs.doc() == doc) {
            tf = termDocs.freq();
          }
        } finally {
          termDocs.close();
        }
        tfExplanation.setValue(similarity.tf(tf));
        tfExplanation.setDescription("tf(termFreq("+term+")="+tf+")");
      } else {
        tfExplanation.setValue(0.0f);
        tfExplanation.setDescription("no matching term");
      }
      fieldExpl.addDetail(tfExplanation);
      fieldExpl.addDetail(expl);
      Explanation fieldNormExpl = new Explanation();
      byte[] fieldNorms = reader.norms(field);
      float fieldNorm =
        fieldNorms!=null ? similarity.decodeNormValue(fieldNorms[doc]) : 1.0f;
      fieldNormExpl.setValue(fieldNorm);
      fieldNormExpl.setDescription("fieldNorm(field="+field+", doc="+doc+")");
      fieldExpl.addDetail(fieldNormExpl);
      fieldExpl.setMatch(Boolean.valueOf(tfExplanation.isMatch()));
      fieldExpl.setValue(tfExplanation.getValue() *
                         expl.getValue() *
                         fieldNormExpl.getValue());
      result.addDetail(fieldExpl);
      result.setMatch(fieldExpl.getMatch());
      result.setValue(queryExpl.getValue() * fieldExpl.getValue());
      if (queryExpl.getValue() == 1.0f)
        return fieldExpl;
      return result;
    }
  }
  public TermQuery(Term t) {
    term = t;
  }
  public Term getTerm() { return term; }
  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    return new TermWeight(searcher);
  }
  @Override
  public void extractTerms(Set<Term> terms) {
    terms.add(getTerm());
  }
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (!term.field().equals(field)) {
      buffer.append(term.field());
      buffer.append(":");
    }
    buffer.append(term.text());
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TermQuery))
      return false;
    TermQuery other = (TermQuery)o;
    return (this.getBoost() == other.getBoost())
      && this.term.equals(other.term);
  }
  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost()) ^ term.hashCode();
  }
}
