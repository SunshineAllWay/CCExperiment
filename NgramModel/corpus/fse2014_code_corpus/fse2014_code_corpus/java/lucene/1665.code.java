package org.apache.lucene.search;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.ToStringUtils;
import java.io.IOException;
import java.util.Set;
public class FilteredQuery
extends Query {
  Query query;
  Filter filter;
  public FilteredQuery (Query query, Filter filter) {
    this.query = query;
    this.filter = filter;
  }
  @Override
  public Weight createWeight(final Searcher searcher) throws IOException {
    final Weight weight = query.createWeight (searcher);
    final Similarity similarity = query.getSimilarity(searcher);
    return new Weight() {
      private float value;
      @Override
      public float getValue() { return value; }
      @Override
      public float sumOfSquaredWeights() throws IOException { 
        return weight.sumOfSquaredWeights() * getBoost() * getBoost(); 
      }
      @Override
      public void normalize (float v) { 
        weight.normalize(v);
        value = weight.getValue() * getBoost();
      }
      @Override
      public Explanation explain (IndexReader ir, int i) throws IOException {
        Explanation inner = weight.explain (ir, i);
        if (getBoost()!=1) {
          Explanation preBoost = inner;
          inner = new Explanation(inner.getValue()*getBoost(),"product of:");
          inner.addDetail(new Explanation(getBoost(),"boost"));
          inner.addDetail(preBoost);
        }
        Filter f = FilteredQuery.this.filter;
        DocIdSet docIdSet = f.getDocIdSet(ir);
        DocIdSetIterator docIdSetIterator = docIdSet == null ? DocIdSet.EMPTY_DOCIDSET.iterator() : docIdSet.iterator();
        if (docIdSetIterator == null) {
          docIdSetIterator = DocIdSet.EMPTY_DOCIDSET.iterator();
        }
        if (docIdSetIterator.advance(i) == i) {
          return inner;
        } else {
          Explanation result = new Explanation
            (0.0f, "failure to match filter: " + f.toString());
          result.addDetail(inner);
          return result;
        }
      }
      @Override
      public Query getQuery() { return FilteredQuery.this; }
      @Override
      public Scorer scorer(IndexReader indexReader, boolean scoreDocsInOrder, boolean topScorer)
          throws IOException {
        final Scorer scorer = weight.scorer(indexReader, true, false);
        if (scorer == null) {
          return null;
        }
        DocIdSet docIdSet = filter.getDocIdSet(indexReader);
        if (docIdSet == null) {
          return null;
        }
        final DocIdSetIterator docIdSetIterator = docIdSet.iterator();
        if (docIdSetIterator == null) {
          return null;
        }
        return new Scorer(similarity) {
          private int doc = -1;
          private int advanceToCommon(int scorerDoc, int disiDoc) throws IOException {
            while (scorerDoc != disiDoc) {
              if (scorerDoc < disiDoc) {
                scorerDoc = scorer.advance(disiDoc);
              } else {
                disiDoc = docIdSetIterator.advance(scorerDoc);
              }
            }
            return scorerDoc;
          }
          @Override
          public int nextDoc() throws IOException {
            int scorerDoc, disiDoc;
            return doc = (disiDoc = docIdSetIterator.nextDoc()) != NO_MORE_DOCS
                && (scorerDoc = scorer.nextDoc()) != NO_MORE_DOCS
                && advanceToCommon(scorerDoc, disiDoc) != NO_MORE_DOCS ? scorer.docID() : NO_MORE_DOCS;
          }
          @Override
          public int docID() { return doc; }
          @Override
          public int advance(int target) throws IOException {
            int disiDoc, scorerDoc;
            return doc = (disiDoc = docIdSetIterator.advance(target)) != NO_MORE_DOCS
                && (scorerDoc = scorer.advance(disiDoc)) != NO_MORE_DOCS 
                && advanceToCommon(scorerDoc, disiDoc) != NO_MORE_DOCS ? scorer.docID() : NO_MORE_DOCS;
          }
          @Override
          public float score() throws IOException { return getBoost() * scorer.score(); }
        };
      }
    };
  }
  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    Query rewritten = query.rewrite(reader);
    if (rewritten != query) {
      FilteredQuery clone = (FilteredQuery)this.clone();
      clone.query = rewritten;
      return clone;
    } else {
      return this;
    }
  }
  public Query getQuery() {
    return query;
  }
  public Filter getFilter() {
    return filter;
  }
  @Override
  public void extractTerms(Set<Term> terms) {
      getQuery().extractTerms(terms);
  }
  @Override
  public String toString (String s) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("filtered(");
    buffer.append(query.toString(s));
    buffer.append(")->");
    buffer.append(filter);
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
  @Override
  public boolean equals(Object o) {
    if (o instanceof FilteredQuery) {
      FilteredQuery fq = (FilteredQuery) o;
      return (query.equals(fq.query) && filter.equals(fq.filter) && getBoost()==fq.getBoost());
    }
    return false;
  }
  @Override
  public int hashCode() {
    return query.hashCode() ^ filter.hashCode() + Float.floatToRawIntBits(getBoost());
  }
}
