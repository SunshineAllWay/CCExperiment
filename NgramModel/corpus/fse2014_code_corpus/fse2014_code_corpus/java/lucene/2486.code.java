package org.apache.solr.search;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
public class FunctionQParserPlugin extends QParserPlugin {
  public static String NAME = "func";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new FunctionQParser(qstr, localParams, params, req);
  }
}
