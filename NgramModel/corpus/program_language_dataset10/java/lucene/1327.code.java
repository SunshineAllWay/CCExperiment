package org.apache.lucene.queryParser.surround.query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
public abstract class SimpleTerm
  extends SrndQuery
  implements DistanceSubQuery, Comparable<SimpleTerm>
{
  public SimpleTerm(boolean q) {quoted = q;}
  private boolean quoted;
  boolean isQuoted() {return quoted;}
  public String getQuote() {return "\"";}
  public String getFieldOperator() {return "/";}
  public abstract String toStringUnquoted();
  public int compareTo(SimpleTerm ost) {
    return this.toStringUnquoted().compareTo( ost.toStringUnquoted());
  }
  protected void suffixToString(StringBuilder r) {} 
  @Override
  public String toString() {
    StringBuilder r = new StringBuilder();
    if (isQuoted()) {
      r.append(getQuote());
    }
    r.append(toStringUnquoted());
    if (isQuoted()) {
      r.append(getQuote());
    }
    suffixToString(r);
    weightToString(r);
    return r.toString();
  }
  public abstract void visitMatchingTerms(
                            IndexReader reader,
                            String fieldName,
                            MatchingTermVisitor mtv) throws IOException;
  public interface MatchingTermVisitor {
    void visitMatchingTerm(Term t)throws IOException;
  }
  public String distanceSubQueryNotAllowed() {return null;}
  @Override
  public Query makeLuceneQueryFieldNoBoost(final String fieldName, final BasicQueryFactory qf) {
    return new Query() {
      @Override
      public String toString(String fn) {
        return getClass().toString() + " " + fieldName + " (" + fn + "?)";
      }
      @Override
      public Query rewrite(IndexReader reader) throws IOException {
        final List<Query> luceneSubQueries = new ArrayList<Query>();
        visitMatchingTerms( reader, fieldName,
            new MatchingTermVisitor() {
              public void visitMatchingTerm(Term term) throws IOException {
                luceneSubQueries.add(qf.newTermQuery(term));
              }
            });
        return  (luceneSubQueries.size() == 0) ? SrndQuery.theEmptyLcnQuery
              : (luceneSubQueries.size() == 1) ? luceneSubQueries.get(0)
              : SrndBooleanQuery.makeBooleanQuery(
                  luceneSubQueries, BooleanClause.Occur.SHOULD);  
      }
    };
  }
  public void addSpanQueries(final SpanNearClauseFactory sncf) throws IOException {
    visitMatchingTerms(
          sncf.getIndexReader(),
          sncf.getFieldName(),
          new MatchingTermVisitor() {
            public void visitMatchingTerm(Term term) throws IOException {
              sncf.addTermWeighted(term, getWeight());
            }
          });
  }
}