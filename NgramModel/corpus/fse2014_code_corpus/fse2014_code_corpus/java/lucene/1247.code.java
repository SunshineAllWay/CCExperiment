package org.apache.lucene.search.regex;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.ToStringUtils;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
public class SpanRegexQuery extends SpanQuery implements RegexQueryCapable {
  private RegexCapabilities regexImpl = new JavaUtilRegexCapabilities();
  private Term term;
  public SpanRegexQuery(Term term) {
    this.term = term;
  }
  public Term getTerm() { return term; }
  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    RegexQuery orig = new RegexQuery(term);
    orig.setRegexImplementation(regexImpl);
    orig.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
    BooleanQuery bq = (BooleanQuery) orig.rewrite(reader);
    BooleanClause[] clauses = bq.getClauses();
    SpanQuery[] sqs = new SpanQuery[clauses.length];
    for (int i = 0; i < clauses.length; i++) {
      BooleanClause clause = clauses[i];
      TermQuery tq = (TermQuery) clause.getQuery();
      sqs[i] = new SpanTermQuery(tq.getTerm());
      sqs[i].setBoost(tq.getBoost());
    }
    SpanOrQuery query = new SpanOrQuery(sqs);
    query.setBoost(orig.getBoost());
    return query;
  }
  @Override
  public Spans getSpans(IndexReader reader) throws IOException {
    throw new UnsupportedOperationException("Query should have been rewritten");
  }
  @Override
  public String getField() {
    return term.field();
  }
  public Collection<Term> getTerms() {
    Collection<Term> terms = new ArrayList<Term>();
    terms.add(term);
    return terms;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final SpanRegexQuery that = (SpanRegexQuery) o;
    if (!regexImpl.equals(that.regexImpl)) return false;
    if (!term.equals(that.term)) return false;
    return true;
  }
  @Override
  public int hashCode() {
    int result;
    result = regexImpl.hashCode();
    result = 29 * result + term.hashCode();
    return result;
  }
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("spanRegexQuery(");
    buffer.append(term);
    buffer.append(")");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
  public void setRegexImplementation(RegexCapabilities impl) {
    this.regexImpl = impl;
  }
  public RegexCapabilities getRegexImplementation() {
    return regexImpl;
  }
}
