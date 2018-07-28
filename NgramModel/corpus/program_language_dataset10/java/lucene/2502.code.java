package org.apache.solr.search;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
public class RawQParserPlugin extends QParserPlugin {
  public static String NAME = "raw";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new QParser(qstr, localParams, params, req) {
      public Query parse() throws ParseException {
        return new TermQuery(new Term(localParams.get(QueryParsing.F), localParams.get(QueryParsing.V)));
      }
    };
  }
}
