package org.apache.solr.handler.clustering;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.DocList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.lucene.search.Query;
public abstract class SearchClusteringEngine extends ClusteringEngine {
  public abstract Object cluster(Query query, DocList docList, SolrQueryRequest sreq);
}
