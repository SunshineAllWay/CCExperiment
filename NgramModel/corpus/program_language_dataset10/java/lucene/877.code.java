package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexReader;
public class DeleteDocTask extends PerfTask {
  public static final int DEFAULT_DOC_DELETE_STEP = 8;
  public DeleteDocTask(PerfRunData runData) {
    super(runData);
  }
  private int deleteStep = -1;
  private static int lastDeleted = -1;
  private int docid = -1;
  private boolean byStep = true;
  @Override
  public int doLogic() throws Exception {
    IndexReader r = getRunData().getIndexReader();
    r.deleteDocument(docid);
    lastDeleted = docid;
    r.decRef();
    return 1; 
  }
  @Override
  public void setup() throws Exception {
    super.setup();
    if (deleteStep<0) {
      deleteStep = getRunData().getConfig().get("doc.delete.step",DEFAULT_DOC_DELETE_STEP);
    }
    docid = (byStep ? lastDeleted + deleteStep : docid);
  }
  @Override
  protected String getLogMessage(int recsCount) {
    return "deleted " + recsCount + " docs, last deleted: " + lastDeleted;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    docid = (int) Float.parseFloat(params);
    byStep = (docid < 0);
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
