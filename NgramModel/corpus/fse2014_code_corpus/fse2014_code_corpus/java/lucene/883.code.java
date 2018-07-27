package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
public class NewRoundTask extends PerfTask {
  public NewRoundTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws Exception {
    getRunData().getConfig().newRound();
    return 0;
  }
  @Override
  protected boolean shouldNotRecordStats() {
    return true;
  }
}
