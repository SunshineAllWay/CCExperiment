package org.apache.solr.search;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.function.BoostedQuery;
import org.apache.solr.search.function.FunctionQuery;
import org.apache.solr.search.function.QueryValueSource;
import org.apache.solr.search.function.ValueSource;
public class NestedQParserPlugin extends QParserPlugin {
  public static String NAME = "query";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new QParser(qstr, localParams, params, req) {
      QParser baseParser;
      ValueSource vs;
      String b;
      public Query parse() throws ParseException {
        baseParser = subQuery(localParams.get(QueryParsing.V), null);
        return baseParser.getQuery();
      }
      public String[] getDefaultHighlightFields() {
        return baseParser.getDefaultHighlightFields();
      }
      public Query getHighlightQuery() throws ParseException {
        return baseParser.getHighlightQuery();
      }
      public void addDebugInfo(NamedList<Object> debugInfo) {
        baseParser.addDebugInfo(debugInfo);
      }
    };
  }
}