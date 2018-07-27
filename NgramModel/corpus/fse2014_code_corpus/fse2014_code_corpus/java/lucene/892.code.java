package org.apache.lucene.benchmark.byTask.tasks;
import java.io.IOException;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexReader;
public class ReopenReaderTask extends PerfTask {
  public ReopenReaderTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws IOException {
    IndexReader r = getRunData().getIndexReader();
    IndexReader nr = r.reopen();
    if (nr != r) {
      getRunData().setIndexReader(nr);
      nr.decRef();
    }
    r.decRef();
    return 1;
  }
}
