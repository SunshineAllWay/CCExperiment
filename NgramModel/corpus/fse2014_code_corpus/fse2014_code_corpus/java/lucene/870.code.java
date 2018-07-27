package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
public class ClearStatsTask extends PerfTask {
  public ClearStatsTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws Exception {
    getRunData().getPoints().clearData();
    return 0;
  }
  @Override
  protected boolean shouldNotRecordStats() {
    return true;
  }
}
