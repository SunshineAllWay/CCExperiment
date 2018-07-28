package org.apache.solr.search;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
public class PrefixQParserPlugin extends QParserPlugin {
  public static String NAME = "prefix";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new QParser(qstr, localParams, params, req) {
      public Query parse() throws ParseException {
        return new PrefixQuery(new Term(localParams.get(QueryParsing.F), localParams.get(QueryParsing.V)));
      }
    };
  }
}
