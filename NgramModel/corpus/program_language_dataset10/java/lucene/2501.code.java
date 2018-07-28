package org.apache.solr.search;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.MatchAllDocsQuery;
import java.util.List;
public class QueryUtils {
  static boolean isNegative(Query q) {
    if (!(q instanceof BooleanQuery)) return false;
    BooleanQuery bq = (BooleanQuery)q;
    List<BooleanClause> clauses = bq.clauses();
    if (clauses.size()==0) return false;
    for (BooleanClause clause : clauses) {
      if (!clause.isProhibited()) return false;
    }
    return true;
  }
  static Query getAbs(Query q) {
    if (!(q instanceof BooleanQuery)) return q;
    BooleanQuery bq = (BooleanQuery)q;
    List<BooleanClause> clauses = bq.clauses();
    if (clauses.size()==0) return q;
    for (BooleanClause clause : clauses) {
      if (!clause.isProhibited()) return q;
    }
    if (clauses.size()==1) {
      Query negClause = clauses.get(0).getQuery();
      return negClause;
    } else {
      BooleanQuery newBq = new BooleanQuery(bq.isCoordDisabled());
      newBq.setBoost(bq.getBoost());
      for (BooleanClause clause : clauses) {
        newBq.add(clause.getQuery(), BooleanClause.Occur.SHOULD);
      }
      return newBq;
    }
  }
  static Query makeQueryable(Query q) {
    return isNegative(q) ? fixNegativeQuery(q) : q;
  }
  static Query fixNegativeQuery(Query q) {
    BooleanQuery newBq = (BooleanQuery)q.clone();
    newBq.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
    return newBq;    
  }
}
