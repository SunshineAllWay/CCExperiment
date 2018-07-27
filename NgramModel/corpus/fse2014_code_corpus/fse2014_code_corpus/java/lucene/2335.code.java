package org.apache.solr.handler;
import java.io.IOException;
import java.util.HashMap;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.RollbackUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
public class RequestHandlerUtils
{
  public static void addExperimentalFormatWarning( SolrQueryResponse rsp )
  {
    rsp.add( "WARNING", "This response format is experimental.  It is likely to change in the future." ); 
  }
  @Deprecated
  public static boolean handleCommit( SolrQueryRequest req, SolrQueryResponse rsp, boolean force ) throws IOException
  {
    SolrParams params = req.getParams();
    if( params == null ) {
      params = new MapSolrParams( new HashMap<String, String>() ); 
    }
    boolean optimize = params.getBool( UpdateParams.OPTIMIZE, false );
    boolean commit   = params.getBool( UpdateParams.COMMIT,   false );
    if( optimize || commit || force ) {
      CommitUpdateCommand cmd = new CommitUpdateCommand( optimize );
      cmd.waitFlush    = params.getBool( UpdateParams.WAIT_FLUSH,    cmd.waitFlush    );
      cmd.waitSearcher = params.getBool( UpdateParams.WAIT_SEARCHER, cmd.waitSearcher );
      cmd.expungeDeletes = params.getBool( UpdateParams.EXPUNGE_DELETES, cmd.expungeDeletes);
      cmd.maxOptimizeSegments = params.getInt(UpdateParams.MAX_OPTIMIZE_SEGMENTS, cmd.maxOptimizeSegments);
      req.getCore().getUpdateHandler().commit( cmd );
      return true;
    }
    return false;
  }
  public static boolean handleCommit( UpdateRequestProcessor processor, SolrParams params, boolean force ) throws IOException
  {
    if( params == null ) {
      params = new MapSolrParams( new HashMap<String, String>() ); 
    }
    boolean optimize = params.getBool( UpdateParams.OPTIMIZE, false );
    boolean commit   = params.getBool( UpdateParams.COMMIT,   false );
    if( optimize || commit || force ) {
      CommitUpdateCommand cmd = new CommitUpdateCommand( optimize );
      cmd.waitFlush    = params.getBool( UpdateParams.WAIT_FLUSH,    cmd.waitFlush    );
      cmd.waitSearcher = params.getBool( UpdateParams.WAIT_SEARCHER, cmd.waitSearcher );
      cmd.expungeDeletes = params.getBool( UpdateParams.EXPUNGE_DELETES, cmd.expungeDeletes);      
      cmd.maxOptimizeSegments = params.getInt(UpdateParams.MAX_OPTIMIZE_SEGMENTS, cmd.maxOptimizeSegments);
      processor.processCommit( cmd );
      return true;
    }
    return false;
  }
  public static boolean handleRollback( UpdateRequestProcessor processor, SolrParams params, boolean force ) throws IOException
  {
    if( params == null ) {
      params = new MapSolrParams( new HashMap<String, String>() ); 
    }
    boolean rollback = params.getBool( UpdateParams.ROLLBACK, false );
    if( rollback || force ) {
      RollbackUpdateCommand cmd = new RollbackUpdateCommand();
      processor.processRollback( cmd );
      return true;
    }
    return false;
  }
}
