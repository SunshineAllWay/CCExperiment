package org.apache.solr.update.processor;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.MergeIndexesCommand;
import org.apache.solr.update.RollbackUpdateCommand;
public abstract class UpdateRequestProcessor {
  protected static Logger log = LoggerFactory.getLogger(UpdateRequestProcessor.class);
  protected final UpdateRequestProcessor next;
  public UpdateRequestProcessor( UpdateRequestProcessor next) {
    this.next = next;
  }
  public void processAdd(AddUpdateCommand cmd) throws IOException {
    if (next != null) next.processAdd(cmd);
  }
  public void processDelete(DeleteUpdateCommand cmd) throws IOException {
    if (next != null) next.processDelete(cmd);
  }
  public void processMergeIndexes(MergeIndexesCommand cmd) throws IOException {
    if (next != null) next.processMergeIndexes(cmd);
  }
  public void processCommit(CommitUpdateCommand cmd) throws IOException
  {
    if (next != null) next.processCommit(cmd);
  }
  public void processRollback(RollbackUpdateCommand cmd) throws IOException
  {
    if (next != null) next.processRollback(cmd);
  }
  public void finish() throws IOException {
    if (next != null) next.finish();    
  }
}
