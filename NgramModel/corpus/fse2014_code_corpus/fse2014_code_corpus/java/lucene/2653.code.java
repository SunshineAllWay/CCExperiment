package org.apache.solr.client.solrj.request;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.XML;
public class UpdateRequest extends AbstractUpdateRequest {
  public enum ACTION {
    COMMIT,
    OPTIMIZE
  };
  private List<SolrInputDocument> documents = null;
  private Iterator<SolrInputDocument> docIterator = null;
  private List<String> deleteById = null;
  private List<String> deleteQuery = null;
  public UpdateRequest()
  {
    super( METHOD.POST, "/update" );
  }
  public UpdateRequest(String url) {
    super(METHOD.POST, url);
  }
  public void clear()
  {
    if( documents != null ) {
      documents.clear();
    }
    if( deleteById != null ) {
      deleteById.clear();
    }
    if( deleteQuery != null ) {
      deleteQuery.clear();
    }
  }
  public UpdateRequest add( final SolrInputDocument doc )
  {
    if( documents == null ) {
      documents = new ArrayList<SolrInputDocument>( 2 );
    }
    documents.add( doc );
    return this;
  }
  public UpdateRequest add( final Collection<SolrInputDocument> docs )
  {
    if( documents == null ) {
      documents = new ArrayList<SolrInputDocument>( docs.size()+1 );
    }
    documents.addAll( docs );
    return this;
  }
  public UpdateRequest deleteById( String id )
  {
    if( deleteById == null ) {
      deleteById = new ArrayList<String>();
    }
    deleteById.add( id );
    return this;
  }
  public UpdateRequest deleteById( List<String> ids )
  {
    if( deleteById == null ) {
      deleteById = new ArrayList<String>(ids);
    } else {
      deleteById.addAll(ids);
    }
    return this;
  }
  public UpdateRequest deleteByQuery( String q )
  {
    if( deleteQuery == null ) {
      deleteQuery = new ArrayList<String>();
    }
    deleteQuery.add( q );
    return this;
  }
  public UpdateRequest setAction(ACTION action, boolean waitFlush, boolean waitSearcher ) {
    return setAction(action, waitFlush, waitSearcher, 1);
  }
  public UpdateRequest setAction(ACTION action, boolean waitFlush, boolean waitSearcher, int maxSegments ) {
    if (params == null)
      params = new ModifiableSolrParams();
    if( action == ACTION.OPTIMIZE ) {
      params.set( UpdateParams.OPTIMIZE, "true" );
      params.set(UpdateParams.MAX_OPTIMIZE_SEGMENTS, maxSegments);
    }
    else if( action == ACTION.COMMIT ) {
      params.set( UpdateParams.COMMIT, "true" );
    }
    params.set( UpdateParams.WAIT_FLUSH, waitFlush+"" );
    params.set( UpdateParams.WAIT_SEARCHER, waitSearcher+"" );
    return this;
  }
  public UpdateRequest setAction(ACTION action, boolean waitFlush, boolean waitSearcher, int maxSegments , boolean expungeDeletes) {
    setAction(action, waitFlush, waitSearcher,maxSegments) ;
    params.set(UpdateParams.EXPUNGE_DELETES,""+expungeDeletes);
    return this;
  }
  public void setDocIterator(Iterator<SolrInputDocument> docIterator) {
    this.docIterator = docIterator;
  }
  @Override
  public Collection<ContentStream> getContentStreams() throws IOException {
    return ClientUtils.toContentStreams( getXML(), ClientUtils.TEXT_XML );
  }
  public String getXML() throws IOException {
    StringWriter writer = new StringWriter();
    writeXML( writer );
    writer.flush();
    String xml = writer.toString();
    return (xml.length() > 0) ? xml : null;
  }
  public void writeXML( Writer writer ) throws IOException {
    if( (documents != null && documents.size() > 0) || docIterator != null) {
      if( commitWithin > 0 ) {
        writer.write("<add commitWithin=\""+commitWithin+"\">");
      }
      else {
        writer.write("<add>");
      }
      if(documents != null) {
        for (SolrInputDocument doc : documents) {
          if (doc != null) {
            ClientUtils.writeXML(doc, writer);
          }
        }
      }
      if (docIterator != null) {
        while (docIterator.hasNext()) {
          SolrInputDocument doc = docIterator.next();
          if (doc != null) {
            ClientUtils.writeXML(doc, writer);
          }
        }
      }
      writer.write("</add>");
    }
    boolean deleteI = deleteById != null && deleteById.size() > 0;
    boolean deleteQ = deleteQuery != null && deleteQuery.size() > 0;
    if( deleteI || deleteQ ) {
      writer.append( "<delete>" );
      if( deleteI ) {
        for( String id : deleteById ) {
          writer.append( "<id>" );
          XML.escapeCharData( id, writer );
          writer.append( "</id>" );
        }
      }
      if( deleteQ ) {
        for( String q : deleteQuery ) {
          writer.append( "<query>" );
          XML.escapeCharData( q, writer );
          writer.append( "</query>" );
        }
      }
      writer.append( "</delete>" );
    }
  }
  public List<SolrInputDocument> getDocuments() {
    return documents;
  }
  public Iterator<SolrInputDocument> getDocIterator() {
    return docIterator;
  }
  public List<String> getDeleteById() {
    return deleteById;
  }
  public List<String> getDeleteQuery() {
    return deleteQuery;
  }
}
