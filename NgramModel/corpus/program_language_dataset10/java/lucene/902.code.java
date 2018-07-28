package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
public class ResetSystemSoftTask extends ResetInputsTask {
  public ResetSystemSoftTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws Exception {
    getRunData().reinit(false);
    return 0;
  }
}
