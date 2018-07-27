package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
public class PrintReaderTask extends PerfTask {
  private String userData = null;
  public PrintReaderTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    userData = params;
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
  @Override
  public int doLogic() throws Exception {
    Directory dir = getRunData().getDirectory();
    IndexReader r = null;
    if (userData == null) 
      r = IndexReader.open(dir, true);
    else
      r = IndexReader.open(OpenReaderTask.findIndexCommit(dir, userData),
                           null,
                           true);
    System.out.println("--> numDocs:"+r.numDocs()+" dels:"+r.numDeletedDocs());
    r.close();
    return 1;
  }
}
