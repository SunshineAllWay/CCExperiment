package org.apache.solr.client.solrj;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.SolrPing;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
public abstract class SolrServer implements Serializable
{
  private DocumentObjectBinder binder;
  public UpdateResponse add(Collection<SolrInputDocument> docs ) throws SolrServerException, IOException {
    UpdateRequest req = new UpdateRequest();
    req.add(docs);
    return req.process(this);
  }
  public UpdateResponse addBeans(Collection<?> beans ) throws SolrServerException, IOException {
    DocumentObjectBinder binder = this.getBinder();
    ArrayList<SolrInputDocument> docs =  new ArrayList<SolrInputDocument>(beans.size());
    for (Object bean : beans) {
      docs.add(binder.toSolrInputDocument(bean));
    }
    return add(docs);
  }
  public UpdateResponse add(SolrInputDocument doc ) throws SolrServerException, IOException {
    UpdateRequest req = new UpdateRequest();
    req.add(doc);
    return req.process(this);
  }
  public UpdateResponse addBean(Object obj) throws IOException, SolrServerException {
    return add(getBinder().toSolrInputDocument(obj));
  }
  public UpdateResponse commit( ) throws SolrServerException, IOException {
    return commit(true, true);
  }
  public UpdateResponse optimize( ) throws SolrServerException, IOException {
    return optimize(true, true, 1);
  }
  public UpdateResponse commit( boolean waitFlush, boolean waitSearcher ) throws SolrServerException, IOException {
    return new UpdateRequest().setAction( UpdateRequest.ACTION.COMMIT, waitFlush, waitSearcher ).process( this );
  }
  public UpdateResponse optimize( boolean waitFlush, boolean waitSearcher ) throws SolrServerException, IOException {
    return optimize(waitFlush, waitSearcher, 1);
  }
  public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher, int maxSegments ) throws SolrServerException, IOException {
    return new UpdateRequest().setAction( UpdateRequest.ACTION.OPTIMIZE, waitFlush, waitSearcher, maxSegments ).process( this );
  }
  public UpdateResponse rollback() throws SolrServerException, IOException {
    return new UpdateRequest().rollback().process( this );
  }
  public UpdateResponse deleteById(String id) throws SolrServerException, IOException {
    return new UpdateRequest().deleteById( id ).process( this );
  }
  public UpdateResponse deleteById(List<String> ids) throws SolrServerException, IOException {
    return new UpdateRequest().deleteById( ids ).process( this );
  }
  public UpdateResponse deleteByQuery(String query) throws SolrServerException, IOException {
    return new UpdateRequest().deleteByQuery( query ).process( this );
  }
  public SolrPingResponse ping() throws SolrServerException, IOException {
    return new SolrPing().process( this );
  }
  public QueryResponse query(SolrParams params) throws SolrServerException {
    return new QueryRequest( params ).process( this );
  }
  public QueryResponse query(SolrParams params, METHOD method) throws SolrServerException {
    return new QueryRequest( params, method ).process( this );
  }
  public abstract NamedList<Object> request( final SolrRequest request ) throws SolrServerException, IOException;
  public DocumentObjectBinder getBinder() {
    if(binder == null){
      binder = new DocumentObjectBinder();
    }
    return binder;
  }
}
