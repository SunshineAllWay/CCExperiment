package org.apache.lucene.benchmark.byTask.tasks;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexWriter;
public class RollbackIndexTask extends PerfTask {
  public RollbackIndexTask(PerfRunData runData) {
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
      iw.rollback();
      getRunData().setIndexWriter(null);
    }
    return 1;
  }
}
