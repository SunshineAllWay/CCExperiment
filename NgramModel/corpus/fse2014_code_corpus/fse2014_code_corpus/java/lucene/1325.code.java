package org.apache.lucene.queryParser.surround.query;
import java.util.List;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
public class NotQuery extends ComposedQuery { 
  public NotQuery(List<SrndQuery> queries, String opName) { super(queries, true , opName); }
  @Override
  public Query makeLuceneQueryFieldNoBoost(String fieldName, BasicQueryFactory qf) {
    List<Query> luceneSubQueries = makeLuceneSubQueriesField(fieldName, qf);
    BooleanQuery bq = new BooleanQuery();
    bq.add( luceneSubQueries.get(0), BooleanClause.Occur.MUST);
    SrndBooleanQuery.addQueriesToBoolean(bq,
            luceneSubQueries.subList(1, luceneSubQueries.size()),
            BooleanClause.Occur.MUST_NOT);
    return bq;
  }
}
