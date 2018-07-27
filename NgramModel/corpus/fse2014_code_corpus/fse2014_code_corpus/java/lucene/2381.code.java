package org.apache.solr.highlight;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
public interface SolrFragmenter extends SolrInfoMBean, NamedListInitializedPlugin {
  public void init(NamedList args);
  public Fragmenter getFragmenter(String fieldName, SolrParams params);
}
