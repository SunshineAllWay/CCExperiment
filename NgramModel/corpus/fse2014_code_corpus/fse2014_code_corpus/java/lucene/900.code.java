package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
public class ResetInputsTask extends PerfTask {
  public ResetInputsTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws Exception {
    getRunData().resetInputs();
    return 0;
  }
  @Override
  protected boolean shouldNotRecordStats() {
    return true;
  }
}
