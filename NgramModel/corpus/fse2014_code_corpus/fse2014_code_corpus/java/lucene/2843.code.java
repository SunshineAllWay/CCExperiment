package org.apache.solr.search;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.index.Term;
public class FooQParserPlugin extends QParserPlugin {
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new FooQParser(qstr, localParams, params, req);
  }
  public void init(NamedList args) {
  }
}
class FooQParser extends QParser {
  public FooQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }
  public Query parse() throws ParseException {
    return new TermQuery(new Term(localParams.get(QueryParsing.F), localParams.get(QueryParsing.V)));
  }
}
