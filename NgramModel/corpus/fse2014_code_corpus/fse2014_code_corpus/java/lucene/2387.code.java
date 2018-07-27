package org.apache.solr.request;
import org.apache.solr.common.params.SolrParams;
@Deprecated
public class DefaultSolrParams extends org.apache.solr.common.params.DefaultSolrParams {
  public DefaultSolrParams(SolrParams main, SolrParams extra) {
    super(main, extra);
  }
}
