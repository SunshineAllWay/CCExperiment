package org.apache.solr.client.solrj.embedded;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.BinaryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.servlet.SolrRequestParsers;
public class EmbeddedSolrServer extends SolrServer
{
  protected final CoreContainer coreContainer;
  protected final String coreName;
  private final SolrRequestParsers _parser;
  @Deprecated
  public EmbeddedSolrServer( SolrCore core )
  {
    if ( core == null ) {
      throw new NullPointerException("SolrCore instance required");
    }
    CoreDescriptor dcore = core.getCoreDescriptor();
    if (dcore == null)
      throw new NullPointerException("CoreDescriptor required");
    CoreContainer cores = dcore.getCoreContainer();
    if (cores == null)
      throw new NullPointerException("CoreContainer required");
    coreName = dcore.getName();
    coreContainer = cores;
    _parser = new SolrRequestParsers( null );
  }
  public EmbeddedSolrServer(  CoreContainer coreContainer, String coreName )
  {
    if ( coreContainer == null ) {
      throw new NullPointerException("CoreContainer instance required");
    }
    this.coreContainer = coreContainer;
    this.coreName = coreName == null? "" : coreName;
    _parser = new SolrRequestParsers( null );
  }
  @Override
  public NamedList<Object> request(SolrRequest request) throws SolrServerException, IOException 
  {
    String path = request.getPath();
    if( path == null || !path.startsWith( "/" ) ) {
      path = "/select";
    }
    SolrCore core =  coreContainer.getCore( coreName );
    if( core == null ) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, 
                               "No such core: " + coreName );
    }
    SolrParams params = request.getParams();
    if( params == null ) {
      params = new ModifiableSolrParams();
    }
    SolrRequestHandler handler = core.getRequestHandler( path );
    if( handler == null ) {
      if( "/select".equals( path ) || "/select/".equalsIgnoreCase( path) ) {
        String qt = params.get( CommonParams.QT );
        handler = core.getRequestHandler( qt );
        if( handler == null ) {
          throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "unknown handler: "+qt);
        }
      }
      if( handler == null &&
          coreContainer != null &&
          path.equals( coreContainer.getAdminPath() ) ) {
        handler = coreContainer.getMultiCoreHandler();
      }
    }
    if( handler == null ) {
      core.close();
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "unknown handler: "+path );
    }
    try {
      SolrQueryRequest req = _parser.buildRequestFrom( core, params, request.getContentStreams() );
      req.getContext().put( "path", path );
      SolrQueryResponse rsp = new SolrQueryResponse();
      core.execute( handler, req, rsp );
      if( rsp.getException() != null ) {
        throw new SolrServerException( rsp.getException() );
      }
      NamedList<Object> normalized = getParsedResponse(req, rsp);
      req.close();
      return normalized;
    }
    catch( IOException iox ) {
      throw iox;
    }
    catch( Exception ex ) {
      throw new SolrServerException( ex );
    }
    finally {
      core.close();
    }
  }
  public NamedList<Object> getParsedResponse( SolrQueryRequest req, SolrQueryResponse rsp )
  {
    return BinaryResponseWriter.getParsedResponse(req, rsp);
  }
}
