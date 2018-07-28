package org.apache.solr.handler.clustering;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocSet;
import org.apache.lucene.search.Query;
public abstract class DocumentClusteringEngine extends ClusteringEngine {
  public abstract NamedList cluster(SolrParams solrParams);
  public abstract NamedList cluster(DocSet docs, SolrParams solrParams);
}
