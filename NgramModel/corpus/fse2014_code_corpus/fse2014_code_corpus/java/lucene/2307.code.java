package org.apache.solr.core;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocIterator;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import java.util.List;
class QuerySenderListener extends AbstractSolrEventListener {
  public QuerySenderListener(SolrCore core) {
    super(core);
  }
  @Override
  public void newSearcher(SolrIndexSearcher newSearcher, SolrIndexSearcher currentSearcher) {
    final SolrIndexSearcher searcher = newSearcher;
    log.info("QuerySenderListener sending requests to " + newSearcher);
    for (NamedList nlst : (List<NamedList>)args.get("queries")) {
      try {
        NamedList params = addEventParms(currentSearcher, nlst);
        LocalSolrQueryRequest req = new LocalSolrQueryRequest(core,params) {
          @Override public SolrIndexSearcher getSearcher() { return searcher; }
          @Override public void close() { }
        };
        SolrQueryResponse rsp = new SolrQueryResponse();
        core.execute(core.getRequestHandler(req.getParams().get(CommonParams.QT)), req, rsp);
        NamedList values = rsp.getValues();
        for (int i=0; i<values.size(); i++) {
          Object o = values.getVal(i);
          if (o instanceof DocList) {
            DocList docs = (DocList)o;
            for (DocIterator iter = docs.iterator(); iter.hasNext();) {
              newSearcher.doc(iter.nextDoc());
            }
          }
        }
        req.close();
      } catch (Exception e) {
      }
    }
    log.info("QuerySenderListener done.");
  }
}
