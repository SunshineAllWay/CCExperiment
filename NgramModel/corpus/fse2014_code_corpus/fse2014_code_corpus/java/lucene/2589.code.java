package org.apache.solr.update.processor;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
import org.apache.solr.util.plugin.SolrCoreAware;
public abstract class UpdateRequestProcessorFactory implements NamedListInitializedPlugin
{    
  public void init( NamedList args )
  {
  }
  abstract public UpdateRequestProcessor getInstance( 
      SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next );
}
