package org.apache.solr.update;
public class CommitUpdateCommand extends UpdateCommand {
  public boolean optimize;
  public boolean waitFlush;
  public boolean waitSearcher=true;
  public boolean expungeDeletes = false;
  public int maxOptimizeSegments = 1;
  public CommitUpdateCommand(boolean optimize) {
    super("commit");
    this.optimize=optimize;
  }
  public String toString() {
    return "commit(optimize="+optimize
            +",waitFlush="+waitFlush
            +",waitSearcher="+waitSearcher
            +",expungeDeletes="+expungeDeletes
            +')';
  }
}
