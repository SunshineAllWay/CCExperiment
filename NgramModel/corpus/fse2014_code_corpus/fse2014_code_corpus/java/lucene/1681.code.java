package org.apache.lucene.search;
import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation.IDFExplanation;
import org.apache.lucene.util.ToStringUtils;
public class PhraseQuery extends Query {
  private String field;
  private ArrayList<Term> terms = new ArrayList<Term>(4);
  private ArrayList<Integer> positions = new ArrayList<Integer>(4);
  private int maxPosition = 0;
  private int slop = 0;
  public PhraseQuery() {}
  public void setSlop(int s) { slop = s; }
  public int getSlop() { return slop; }
  public void add(Term term) {
    int position = 0;
    if(positions.size() > 0)
        position = positions.get(positions.size()-1).intValue() + 1;
    add(term, position);
  }
  public void add(Term term, int position) {
      if (terms.size() == 0)
          field = term.field();
      else if (term.field() != field)
          throw new IllegalArgumentException("All phrase terms must be in the same field: " + term);
      terms.add(term);
      positions.add(Integer.valueOf(position));
      if (position > maxPosition) maxPosition = position;
  }
  public Term[] getTerms() {
    return terms.toArray(new Term[0]);
  }
  public int[] getPositions() {
      int[] result = new int[positions.size()];
      for(int i = 0; i < positions.size(); i++)
          result[i] = positions.get(i).intValue();
      return result;
  }
  private class PhraseWeight extends Weight {
    private final Similarity similarity;
    private float value;
    private float idf;
    private float queryNorm;
    private float queryWeight;
    private IDFExplanation idfExp;
    public PhraseWeight(Searcher searcher)
      throws IOException {
      this.similarity = getSimilarity(searcher);
      idfExp = similarity.idfExplain(terms, searcher);
      idf = idfExp.getIdf();
    }
    @Override
    public String toString() { return "weight(" + PhraseQuery.this + ")"; }
    @Override
    public Query getQuery() { return PhraseQuery.this; }
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
      if (terms.size() == 0)			  
        return null;
      TermPositions[] tps = new TermPositions[terms.size()];
      for (int i = 0; i < terms.size(); i++) {
        TermPositions p = reader.termPositions(terms.get(i));
        if (p == null)
          return null;
        tps[i] = p;
      }
      if (slop == 0)				  
        return new ExactPhraseScorer(this, tps, getPositions(), similarity,
                                     reader.norms(field));
      else
        return
          new SloppyPhraseScorer(this, tps, getPositions(), similarity, slop,
                                 reader.norms(field));
    }
    @Override
    public Explanation explain(IndexReader reader, int doc)
      throws IOException {
      Explanation result = new Explanation();
      result.setDescription("weight("+getQuery()+" in "+doc+"), product of:");
      StringBuilder docFreqs = new StringBuilder();
      StringBuilder query = new StringBuilder();
      query.append('\"');
      docFreqs.append(idfExp.explain());
      for (int i = 0; i < terms.size(); i++) {
        if (i != 0) {
          query.append(" ");
        }
        Term term = terms.get(i);
        query.append(term.text());
      }
      query.append('\"');
      Explanation idfExpl =
        new Explanation(idf, "idf(" + field + ":" + docFreqs + ")");
      Explanation queryExpl = new Explanation();
      queryExpl.setDescription("queryWeight(" + getQuery() + "), product of:");
      Explanation boostExpl = new Explanation(getBoost(), "boost");
      if (getBoost() != 1.0f)
        queryExpl.addDetail(boostExpl);
      queryExpl.addDetail(idfExpl);
      Explanation queryNormExpl = new Explanation(queryNorm,"queryNorm");
      queryExpl.addDetail(queryNormExpl);
      queryExpl.setValue(boostExpl.getValue() *
                         idfExpl.getValue() *
                         queryNormExpl.getValue());
      result.addDetail(queryExpl);
      Explanation fieldExpl = new Explanation();
      fieldExpl.setDescription("fieldWeight("+field+":"+query+" in "+doc+
                               "), product of:");
      PhraseScorer scorer = (PhraseScorer) scorer(reader, true, false);
      if (scorer == null) {
        return new Explanation(0.0f, "no matching docs");
      }
      Explanation tfExplanation = new Explanation();
      int d = scorer.advance(doc);
      float phraseFreq = (d == doc) ? scorer.currentFreq() : 0.0f;
      tfExplanation.setValue(similarity.tf(phraseFreq));
      tfExplanation.setDescription("tf(phraseFreq=" + phraseFreq + ")");
      fieldExpl.addDetail(tfExplanation);
      fieldExpl.addDetail(idfExpl);
      Explanation fieldNormExpl = new Explanation();
      byte[] fieldNorms = reader.norms(field);
      float fieldNorm =
        fieldNorms!=null ? similarity.decodeNormValue(fieldNorms[doc]) : 1.0f;
      fieldNormExpl.setValue(fieldNorm);
      fieldNormExpl.setDescription("fieldNorm(field="+field+", doc="+doc+")");
      fieldExpl.addDetail(fieldNormExpl);
      fieldExpl.setValue(tfExplanation.getValue() *
                         idfExpl.getValue() *
                         fieldNormExpl.getValue());
      result.addDetail(fieldExpl);
      result.setValue(queryExpl.getValue() * fieldExpl.getValue());
      if (queryExpl.getValue() == 1.0f)
        return fieldExpl;
      return result;
    }
  }
  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    if (terms.size() == 1) {			  
      Term term = terms.get(0);
      Query termQuery = new TermQuery(term);
      termQuery.setBoost(getBoost());
      return termQuery.createWeight(searcher);
    }
    return new PhraseWeight(searcher);
  }
  @Override
  public void extractTerms(Set<Term> queryTerms) {
    queryTerms.addAll(terms);
  }
  @Override
  public String toString(String f) {
    StringBuilder buffer = new StringBuilder();
    if (field != null && !field.equals(f)) {
      buffer.append(field);
      buffer.append(":");
    }
    buffer.append("\"");
    String[] pieces = new String[maxPosition + 1];
    for (int i = 0; i < terms.size(); i++) {
      int pos = positions.get(i).intValue();
      String s = pieces[pos];
      if (s == null) {
        s = (terms.get(i)).text();
      } else {
        s = s + "|" + (terms.get(i)).text();
      }
      pieces[pos] = s;
    }
    for (int i = 0; i < pieces.length; i++) {
      if (i > 0) {
        buffer.append(' ');
      }
      String s = pieces[i];
      if (s == null) {
        buffer.append('?');
      } else {
        buffer.append(s);
      }
    }
    buffer.append("\"");
    if (slop != 0) {
      buffer.append("~");
      buffer.append(slop);
    }
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PhraseQuery))
      return false;
    PhraseQuery other = (PhraseQuery)o;
    return (this.getBoost() == other.getBoost())
      && (this.slop == other.slop)
      &&  this.terms.equals(other.terms)
      && this.positions.equals(other.positions);
  }
  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost())
      ^ slop
      ^ terms.hashCode()
      ^ positions.hashCode();
  }
}
