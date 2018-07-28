package org.apache.solr.client.solrj.request;
import java.io.IOException;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.ContentStream;
public class SolrPing extends SolrRequest
{
  private ModifiableSolrParams params;
  public SolrPing()
  {
    super( METHOD.GET, "/admin/ping" );
    params = new ModifiableSolrParams();
  }
  @Override
  public Collection<ContentStream> getContentStreams() {
    return null;
  }
  @Override
  public ModifiableSolrParams getParams() {
    return params;
  }
  @Override
  public SolrPingResponse process( SolrServer server ) throws SolrServerException, IOException 
  {
    long startTime = System.currentTimeMillis();
    SolrPingResponse res = new SolrPingResponse();
    res.setResponse( server.request( this ) );
    res.setElapsedTime( System.currentTimeMillis()-startTime );
    return res;
  }
}
