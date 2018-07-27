package org.apache.lucene.benchmark.byTask.tasks;
import java.util.Properties;
import org.apache.lucene.benchmark.BenchmarkTestCase;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.utils.Config;
public class PerfTaskTest extends BenchmarkTestCase {
  private static final class MyPerfTask extends PerfTask {
    public MyPerfTask(PerfRunData runData) {
      super(runData);
    }
    @Override
    public int doLogic() throws Exception {
      return 0;
    }
    public int getLogStep() { return logStep; }
  }
  private PerfRunData createPerfRunData(boolean setLogStep, int logStepVal,
      boolean setTaskLogStep, int taskLogStepVal) throws Exception {
    Properties props = new Properties();
    if (setLogStep) {
      props.setProperty("log.step", Integer.toString(logStepVal));
    }
    if (setTaskLogStep) {
      props.setProperty("log.step.MyPerf", Integer.toString(taskLogStepVal));
    }
    props.setProperty("directory", "RAMDirectory"); 
    Config config = new Config(props);
    return new PerfRunData(config);
  }
  private void doLogStepTest(boolean setLogStep, int logStepVal,
      boolean setTaskLogStep, int taskLogStepVal, int expLogStepValue) throws Exception {
    PerfRunData runData = createPerfRunData(setLogStep, logStepVal, setTaskLogStep, taskLogStepVal);
    MyPerfTask mpt = new MyPerfTask(runData);
    assertEquals(expLogStepValue, mpt.getLogStep());
  }
  public void testLogStep() throws Exception {
    doLogStepTest(false, -1, false, -1, PerfTask.DEFAULT_LOG_STEP);
    doLogStepTest(true, -1, false, -1, Integer.MAX_VALUE);
    doLogStepTest(true, 100, false, -1, 100);
    doLogStepTest(false, -1, true, -1, Integer.MAX_VALUE);
    doLogStepTest(false, -1, true, 100, 100);
  }
}
