package org.apache.lucene.benchmark.byTask.tasks;
import java.io.IOException;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexReader;
public class CloseReaderTask extends PerfTask {
  public CloseReaderTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws IOException {
    IndexReader reader = getRunData().getIndexReader();
    getRunData().setIndexReader(null);
    if (reader.getRefCount() != 1) {
      System.out.println("WARNING: CloseReader: reference count is currently " + reader.getRefCount());
    }
    reader.decRef();
    return 1;
  }
}
