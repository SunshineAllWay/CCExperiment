package org.apache.lucene.search;
import java.io.IOException;
import java.util.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultipleTermPositions;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.util.ToStringUtils;
public class MultiPhraseQuery extends Query {
  private String field;
  private ArrayList<Term[]> termArrays = new ArrayList<Term[]>();
  private ArrayList<Integer> positions = new ArrayList<Integer>();
  private int slop = 0;
  public void setSlop(int s) { slop = s; }
  public int getSlop() { return slop; }
  public void add(Term term) { add(new Term[]{term}); }
  public void add(Term[] terms) {
    int position = 0;
    if (positions.size() > 0)
      position = positions.get(positions.size()-1).intValue() + 1;
    add(terms, position);
  }
  public void add(Term[] terms, int position) {
    if (termArrays.size() == 0)
      field = terms[0].field();
    for (int i = 0; i < terms.length; i++) {
      if (terms[i].field() != field) {
        throw new IllegalArgumentException(
            "All phrase terms must be in the same field (" + field + "): "
                + terms[i]);
      }
    }
    termArrays.add(terms);
    positions.add(Integer.valueOf(position));
  }
  public List<Term[]> getTermArrays() {
	  return Collections.unmodifiableList(termArrays);
  }
  public int[] getPositions() {
    int[] result = new int[positions.size()];
    for (int i = 0; i < positions.size(); i++)
      result[i] = positions.get(i).intValue();
    return result;
  }
  @Override
  public void extractTerms(Set<Term> terms) {
    for (final Term[] arr : termArrays) {
      for (final Term term: arr) {
        terms.add(term);
      }
    }
  }
  private class MultiPhraseWeight extends Weight {
    private Similarity similarity;
    private float value;
    private float idf;
    private float queryNorm;
    private float queryWeight;
    public MultiPhraseWeight(Searcher searcher)
      throws IOException {
      this.similarity = getSimilarity(searcher);
      final int maxDoc = searcher.maxDoc();
      for(final Term[] terms: termArrays) {
        for (Term term: terms) {
          idf += this.similarity.idf(searcher.docFreq(term), maxDoc);
        }
      }
    }
    @Override
    public Query getQuery() { return MultiPhraseQuery.this; }
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
      if (termArrays.size() == 0)                  
        return null;
      TermPositions[] tps = new TermPositions[termArrays.size()];
      for (int i=0; i<tps.length; i++) {
        Term[] terms = termArrays.get(i);
        TermPositions p;
        if (terms.length > 1)
          p = new MultipleTermPositions(reader, terms);
        else
          p = reader.termPositions(terms[0]);
        if (p == null)
          return null;
        tps[i] = p;
      }
      if (slop == 0)
        return new ExactPhraseScorer(this, tps, getPositions(), similarity,
                                     reader.norms(field));
      else
        return new SloppyPhraseScorer(this, tps, getPositions(), similarity,
                                      slop, reader.norms(field));
    }
    @Override
    public Explanation explain(IndexReader reader, int doc)
      throws IOException {
      ComplexExplanation result = new ComplexExplanation();
      result.setDescription("weight("+getQuery()+" in "+doc+"), product of:");
      Explanation idfExpl = new Explanation(idf, "idf("+getQuery()+")");
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
      ComplexExplanation fieldExpl = new ComplexExplanation();
      fieldExpl.setDescription("fieldWeight("+getQuery()+" in "+doc+
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
      fieldExpl.setMatch(Boolean.valueOf(tfExplanation.isMatch()));
      fieldExpl.setValue(tfExplanation.getValue() *
                         idfExpl.getValue() *
                         fieldNormExpl.getValue());
      result.addDetail(fieldExpl);
      result.setMatch(fieldExpl.getMatch());
      result.setValue(queryExpl.getValue() * fieldExpl.getValue());
      if (queryExpl.getValue() == 1.0f)
        return fieldExpl;
      return result;
    }
  }
  @Override
  public Query rewrite(IndexReader reader) {
    if (termArrays.size() == 1) {                 
      Term[] terms = termArrays.get(0);
      BooleanQuery boq = new BooleanQuery(true);
      for (int i=0; i<terms.length; i++) {
        boq.add(new TermQuery(terms[i]), BooleanClause.Occur.SHOULD);
      }
      boq.setBoost(getBoost());
      return boq;
    } else {
      return this;
    }
  }
  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    return new MultiPhraseWeight(searcher);
  }
  @Override
  public final String toString(String f) {
    StringBuilder buffer = new StringBuilder();
    if (!field.equals(f)) {
      buffer.append(field);
      buffer.append(":");
    }
    buffer.append("\"");
    Iterator<Term[]> i = termArrays.iterator();
    while (i.hasNext()) {
      Term[] terms = i.next();
      if (terms.length > 1) {
        buffer.append("(");
        for (int j = 0; j < terms.length; j++) {
          buffer.append(terms[j].text());
          if (j < terms.length-1)
            buffer.append(" ");
        }
        buffer.append(")");
      } else {
        buffer.append(terms[0].text());
      }
      if (i.hasNext())
        buffer.append(" ");
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
    if (!(o instanceof MultiPhraseQuery)) return false;
    MultiPhraseQuery other = (MultiPhraseQuery)o;
    return this.getBoost() == other.getBoost()
      && this.slop == other.slop
      && termArraysEquals(this.termArrays, other.termArrays)
      && this.positions.equals(other.positions);
  }
  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost())
      ^ slop
      ^ termArraysHashCode()
      ^ positions.hashCode()
      ^ 0x4AC65113;
  }
  private int termArraysHashCode() {
    int hashCode = 1;
    for (final Term[] termArray: termArrays) {
      hashCode = 31 * hashCode
          + (termArray == null ? 0 : Arrays.hashCode(termArray));
    }
    return hashCode;
  }
  private boolean termArraysEquals(List<Term[]> termArrays1, List<Term[]> termArrays2) {
    if (termArrays1.size() != termArrays2.size()) {
      return false;
    }
    ListIterator<Term[]> iterator1 = termArrays1.listIterator();
    ListIterator<Term[]> iterator2 = termArrays2.listIterator();
    while (iterator1.hasNext()) {
      Term[] termArray1 = iterator1.next();
      Term[] termArray2 = iterator2.next();
      if (!(termArray1 == null ? termArray2 == null : Arrays.equals(termArray1,
          termArray2))) {
        return false;
      }
    }
    return true;
  }
}
