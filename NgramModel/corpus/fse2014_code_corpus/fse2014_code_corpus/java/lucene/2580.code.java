package org.apache.solr.update.processor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.MergeIndexesCommand;
import org.apache.solr.update.RollbackUpdateCommand;
public class LogUpdateProcessorFactory extends UpdateRequestProcessorFactory {
  int maxNumToLog = 8;
  @Override
  public void init( final NamedList args ) {
    if( args != null ) {
      SolrParams params = SolrParams.toSolrParams( args );
      maxNumToLog = params.getInt( "maxNumToLog", maxNumToLog );
    }
  }
  @Override
  public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
    boolean doLog = LogUpdateProcessor.log.isInfoEnabled();
    if( doLog ) {
      return new LogUpdateProcessor(req, rsp, this, next);
    }
    return null;
  }
}
class LogUpdateProcessor extends UpdateRequestProcessor {
  private final SolrQueryRequest req;
  private final SolrQueryResponse rsp;
  private final NamedList<Object> toLog;
  int numAdds;
  int numDeletes;
  private List<String> adds;
  private List<String> deletes;
  private final int maxNumToLog;
  public LogUpdateProcessor(SolrQueryRequest req, SolrQueryResponse rsp, LogUpdateProcessorFactory factory, UpdateRequestProcessor next) {
    super( next );
    this.req = req;
    this.rsp = rsp;
    maxNumToLog = factory.maxNumToLog;  
    this.toLog = new SimpleOrderedMap<Object>();
  }
  @Override
  public void processAdd(AddUpdateCommand cmd) throws IOException {
    if (next != null) next.processAdd(cmd);
    if (adds == null) {
      adds = new ArrayList<String>();
      toLog.add("add",adds);
    }
    if (adds.size() < maxNumToLog) {
      adds.add(cmd.getPrintableId(req.getSchema()));
    }
    numAdds++;
  }
  @Override
  public void processDelete( DeleteUpdateCommand cmd ) throws IOException {
    if (next != null) next.processDelete(cmd);
    if (cmd.id != null) {
      if (deletes == null) {
        deletes = new ArrayList<String>();
        toLog.add("delete",deletes);
      }
      if (deletes.size() < maxNumToLog) {
        deletes.add(cmd.id);
      }
    } else {
      if (toLog.size() < maxNumToLog) {
        toLog.add("deleteByQuery", cmd.query);
      }
    }
    numDeletes++;
  }
  @Override
  public void processMergeIndexes(MergeIndexesCommand cmd) throws IOException {
    if (next != null) next.processMergeIndexes(cmd);
    toLog.add("mergeIndexes", cmd.toString());
  }
  @Override
  public void processCommit( CommitUpdateCommand cmd ) throws IOException {
    if (next != null) next.processCommit(cmd);
    toLog.add(cmd.optimize ? "optimize" : "commit", "");
  }
  @Override
  public void processRollback( RollbackUpdateCommand cmd ) throws IOException {
    if (next != null) next.processRollback(cmd);
    toLog.add("rollback", "");
  }
  @Override
  public void finish() throws IOException {
    if (next != null) next.finish();
    if (adds != null && numAdds > maxNumToLog) {
      adds.add("... (" + numAdds + " adds)");
    }
    if (deletes != null && numDeletes > maxNumToLog) {
      deletes.add("... (" + numDeletes + " deletes)");
    }
    long elapsed = rsp.getEndTime() - req.getStartTime();
    log.info( ""+toLog + " 0 " + (elapsed) );
  }
}
