package org.apache.solr.client.solrj.request;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
public class QueryRequest extends SolrRequest
{
  private SolrParams query;
  public QueryRequest()
  {
    super( METHOD.GET, null );
  }
  public QueryRequest( SolrParams q )
  {
    super( METHOD.GET, null );
    query = q;
  }
  public QueryRequest( SolrParams q, METHOD method )
  {
    super( method, null );
    query = q;
  }
  @Override
  public String getPath() {
    String qt = query.get( CommonParams.QT );
    if( qt == null ) {
      qt = super.getPath();
    }
    if( qt != null && qt.startsWith( "/" ) ) {
      return qt;
    }
    return "/select";
  }
  @Override
  public Collection<ContentStream> getContentStreams() {
    return null;
  }
  @Override
  public SolrParams getParams() {
    return query;
  }
  @Override
  public QueryResponse process( SolrServer server ) throws SolrServerException 
  {
    try {
      long startTime = System.currentTimeMillis();
      QueryResponse res = new QueryResponse( server.request( this ), server );
      res.setElapsedTime( System.currentTimeMillis()-startTime );
      return res;
    } catch (SolrServerException e){
      throw e;
    } catch (Exception e) {
      throw new SolrServerException("Error executing query", e);
    }
  }
}
