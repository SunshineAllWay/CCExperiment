package org.apache.lucene.queryParser.surround.query;
import java.util.List;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause;
public class AndQuery extends ComposedQuery { 
  public AndQuery(List<SrndQuery> queries, boolean inf, String opName) { 
    super(queries, inf, opName);
  }
  @Override
  public Query makeLuceneQueryFieldNoBoost(String fieldName, BasicQueryFactory qf) {
    return SrndBooleanQuery.makeBooleanQuery( 
      makeLuceneSubQueriesField(fieldName, qf), BooleanClause.Occur.MUST);
  }
}
