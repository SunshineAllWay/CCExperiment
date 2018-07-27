package org.apache.lucene.queryParser.surround.query;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.lucene.search.Query;
public abstract class ComposedQuery extends SrndQuery { 
  public ComposedQuery(List qs, boolean operatorInfix, String opName) {
    recompose(qs);
    this.operatorInfix = operatorInfix;
    this.opName = opName;
  }
  protected void recompose(List queries) {
    if (queries.size() < 2) throw new AssertionError("Too few subqueries"); 
    this.queries = queries;
  }
  private String opName;
  public String getOperatorName() {return opName;}
  private List queries;
  public Iterator getSubQueriesIterator() {return queries.listIterator();}
  public int getNrSubQueries() {return queries.size();}
  public SrndQuery getSubQuery(int qn) {return (SrndQuery) queries.get(qn);}
  private boolean operatorInfix; 
  public boolean isOperatorInfix() { return operatorInfix; } 
  public List<Query> makeLuceneSubQueriesField(String fn, BasicQueryFactory qf) {
    List<Query> luceneSubQueries = new ArrayList<Query>();
    Iterator sqi = getSubQueriesIterator();
    while (sqi.hasNext()) {
      luceneSubQueries.add( ((SrndQuery) sqi.next()).makeLuceneQueryField(fn, qf));
    }
    return luceneSubQueries;
  }
  @Override
  public String toString() {
    StringBuilder r = new StringBuilder();
    if (isOperatorInfix()) {
      infixToString(r);
    } else {
      prefixToString(r);
    }
    weightToString(r);
    return r.toString();
  }
  protected String getPrefixSeparator() { return ", ";}
  protected String getBracketOpen() { return "(";}
  protected String getBracketClose() { return ")";}
  protected void infixToString(StringBuilder r) {
    Iterator sqi = getSubQueriesIterator();
    r.append(getBracketOpen());
    if (sqi.hasNext()) {
      r.append(sqi.next().toString());
      while (sqi.hasNext()) {
        r.append(" ");
        r.append(getOperatorName()); 
        r.append(" ");
        r.append(sqi.next().toString());
      }
    }
    r.append(getBracketClose());
  }
  protected void prefixToString(StringBuilder r) {
    Iterator sqi = getSubQueriesIterator();
    r.append(getOperatorName()); 
    r.append(getBracketOpen());
    if (sqi.hasNext()) {
      r.append(sqi.next().toString());
      while (sqi.hasNext()) {
        r.append(getPrefixSeparator());
        r.append(sqi.next().toString());
      }
    }
    r.append(getBracketClose());
  }
  @Override
  public boolean isFieldsSubQueryAcceptable() {
    Iterator sqi = getSubQueriesIterator();
    while (sqi.hasNext()) {
      if (((SrndQuery) sqi.next()).isFieldsSubQueryAcceptable()) {
        return true;
      }
    }
    return false;
  }
}
