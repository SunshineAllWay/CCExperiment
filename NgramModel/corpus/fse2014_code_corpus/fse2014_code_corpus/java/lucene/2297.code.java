package org.apache.solr.core;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.params.EventParams;
import org.apache.solr.search.SolrIndexSearcher;
class AbstractSolrEventListener implements SolrEventListener {
  protected final SolrCore core;
  public AbstractSolrEventListener(SolrCore core) {
    this.core = core;
  }
  protected NamedList args;
  public void init(NamedList args) {
    this.args = args;
  }
  public void postCommit() {
    throw new UnsupportedOperationException();
  }
  public void newSearcher(SolrIndexSearcher newSearcher, SolrIndexSearcher currentSearcher) {
    throw new UnsupportedOperationException();
  }
  public String toString() {
    return getClass().getName() + args;
  }
  protected NamedList addEventParms(SolrIndexSearcher currentSearcher, NamedList nlst) {
    NamedList result = new NamedList();
    result.addAll(nlst);
    if (currentSearcher != null) {
      result.add(EventParams.EVENT, EventParams.NEW_SEARCHER);
    } else {
      result.add(EventParams.EVENT, EventParams.FIRST_SEARCHER);
    }
    return result;
  }
}
