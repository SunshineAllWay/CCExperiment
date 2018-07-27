package org.apache.lucene.benchmark.byTask.tasks;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexWriter;
public class CloseIndexTask extends PerfTask {
  public CloseIndexTask(PerfRunData runData) {
    super(runData);
  }
  boolean doWait = true;
  @Override
  public int doLogic() throws IOException {
    IndexWriter iw = getRunData().getIndexWriter();
    if (iw != null) {
      PrintStream infoStream = iw.getInfoStream();
      if (infoStream != null && infoStream != System.out
          && infoStream != System.err) {
        infoStream.close();
      }
      iw.close(doWait);
      getRunData().setIndexWriter(null);
    }
    return 1;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    doWait = Boolean.valueOf(params).booleanValue();
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
