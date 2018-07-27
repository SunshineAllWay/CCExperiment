package org.apache.solr.servlet;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
public class DirectSolrConnection 
{
  final SolrCore core;
  final SolrRequestParsers parser;
  @Deprecated
  public DirectSolrConnection()
  {
    this( SolrCore.getSolrCore() );
  }
  public DirectSolrConnection( SolrCore c )
  {
    core = c;
    parser = new SolrRequestParsers( c.getSolrConfig() );
  }
  public DirectSolrConnection( String instanceDir, String dataDir, String loggingPath )
  {
    if( loggingPath != null ) {
      File loggingConfig = new File( loggingPath );
      if( !loggingConfig.exists() && instanceDir != null ) {
        loggingConfig = new File( new File(instanceDir), loggingPath  );
      }
      if( loggingConfig.exists() ) {
        System.setProperty("java.util.logging.config.file", loggingConfig.getAbsolutePath() ); 
      }
      else {
        throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "can not find logging file: "+loggingConfig );
      }
    }
    if( instanceDir == null ) {
      instanceDir = SolrResourceLoader.locateInstanceDir();
    }
    try {
      CoreContainer cores = new CoreContainer(new SolrResourceLoader(instanceDir));
      SolrConfig solrConfig = new SolrConfig(instanceDir, SolrConfig.DEFAULT_CONF_FILE, null);
      CoreDescriptor dcore = new CoreDescriptor(cores, "", solrConfig.getResourceLoader().getInstanceDir());
      IndexSchema indexSchema = new IndexSchema(solrConfig, instanceDir+"/conf/schema.xml", null);
      core = new SolrCore( null, dataDir, solrConfig, indexSchema, dcore);
      cores.register("", core, false);
      parser = new SolrRequestParsers( solrConfig );
    } 
    catch (Exception ee) {
      throw new RuntimeException(ee);
    }
  }
  public String request( String pathAndParams, String body ) throws Exception
  {
    String path = null;
    SolrParams params = null;
    int idx = pathAndParams.indexOf( '?' );
    if( idx > 0 ) {
      path = pathAndParams.substring( 0, idx );
      params = SolrRequestParsers.parseQueryString( pathAndParams.substring(idx+1) );
    }
    else {
      path= pathAndParams;
      params = new MapSolrParams( new HashMap<String, String>() );
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
    }
    if( handler == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "unknown handler: "+path );
    }
    List<ContentStream> streams = new ArrayList<ContentStream>( 1 );
    if( body != null && body.length() > 0 ) {
      streams.add( new ContentStreamBase.StringStream( body ) );
    }
    SolrQueryRequest req = null;
    try {
      req = parser.buildRequestFrom( core, params, streams );
      SolrQueryResponse rsp = new SolrQueryResponse();
      core.execute( handler, req, rsp );
      if( rsp.getException() != null ) {
        throw rsp.getException();
      }
      QueryResponseWriter responseWriter = core.getQueryResponseWriter(req);
      StringWriter out = new StringWriter();
      responseWriter.write(out, req, rsp);
      return out.toString();
    } finally {
      if (req != null) {
        req.close();
      }
    }
  }
  public void close() {
    core.close();
  }
}
