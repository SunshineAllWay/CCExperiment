package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexWriter;
public class OptimizeTask extends PerfTask {
  public OptimizeTask(PerfRunData runData) {
    super(runData);
  }
  int maxNumSegments = 1;
  @Override
  public int doLogic() throws Exception {
    IndexWriter iw = getRunData().getIndexWriter();
    iw.optimize(maxNumSegments);
    return 1;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    maxNumSegments = Double.valueOf(params).intValue();
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
