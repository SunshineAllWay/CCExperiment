package org.apache.solr.update.processor;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
public class CustomUpdateRequestProcessorFactory extends UpdateRequestProcessorFactory 
{
  public NamedList args = null;
  @Override
  public void init( NamedList args )
  {
    this.args = args;
  }
  @Override
  public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
    return null;
  }
}
