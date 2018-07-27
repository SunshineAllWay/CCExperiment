package org.apache.solr.update.processor;
import java.io.IOException;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.DocumentBuilder;
import org.apache.solr.update.MergeIndexesCommand;
import org.apache.solr.update.RollbackUpdateCommand;
import org.apache.solr.update.UpdateHandler;
public class RunUpdateProcessorFactory extends UpdateRequestProcessorFactory 
{
  @Override
  public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) 
  {
    return new RunUpdateProcessor(req, next);
  }
}
class RunUpdateProcessor extends UpdateRequestProcessor 
{
  private final SolrQueryRequest req;
  private final UpdateHandler updateHandler;
  public RunUpdateProcessor(SolrQueryRequest req, UpdateRequestProcessor next) {
    super( next );
    this.req = req;
    this.updateHandler = req.getCore().getUpdateHandler();
  }
  @Override
  public void processAdd(AddUpdateCommand cmd) throws IOException {
    cmd.doc = DocumentBuilder.toDocument(cmd.getSolrInputDocument(), req.getSchema());
    updateHandler.addDoc(cmd);
    super.processAdd(cmd);
  }
  @Override
  public void processDelete(DeleteUpdateCommand cmd) throws IOException {
    if( cmd.id != null ) {
      updateHandler.delete(cmd);
    }
    else {
      updateHandler.deleteByQuery(cmd);
    }
    super.processDelete(cmd);
  }
  @Override
  public void processMergeIndexes(MergeIndexesCommand cmd) throws IOException {
    updateHandler.mergeIndexes(cmd);
    super.processMergeIndexes(cmd);
  }
  @Override
  public void processCommit(CommitUpdateCommand cmd) throws IOException
  {
    updateHandler.commit(cmd);
    super.processCommit(cmd);
  }
  @Override
  public void processRollback(RollbackUpdateCommand cmd) throws IOException
  {
    updateHandler.rollback(cmd);
    super.processRollback(cmd);
  }
}
