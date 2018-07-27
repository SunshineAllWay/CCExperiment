package org.apache.lucene.benchmark.byTask.tasks;
import java.text.NumberFormat;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.stats.Points;
import org.apache.lucene.benchmark.byTask.stats.TaskStats;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.benchmark.byTask.utils.Format;
public abstract class PerfTask implements Cloneable {
  static final int DEFAULT_LOG_STEP = 1000;
  private PerfRunData runData;
  private String name;
  private int depth = 0;
  protected int logStep;
  private int logStepCount = 0;
  private int maxDepthLogStart = 0;
  private boolean disableCounting = false;
  protected String params = null;
  private boolean runInBackground;
  private int deltaPri;
  protected static final String NEW_LINE = System.getProperty("line.separator");
  private PerfTask() {
    name = Format.simpleName(getClass());
    if (name.endsWith("Task")) {
      name = name.substring(0, name.length() - 4);
    }
  }
  public void setRunInBackground(int deltaPri) {
    runInBackground = true;
    this.deltaPri = deltaPri;
  }
  public boolean getRunInBackground() {
    return runInBackground;
  }
  public int getBackgroundDeltaPriority() {
    return deltaPri;
  }
  protected volatile boolean stopNow;
  public void stopNow() {
    stopNow = true;
  }
  public PerfTask(PerfRunData runData) {
    this();
    this.runData = runData;
    Config config = runData.getConfig();
    this.maxDepthLogStart = config.get("task.max.depth.log",0);
    String logStepAtt = "log.step";
    String taskName = getClass().getName();
    int idx = taskName.lastIndexOf('.');
    int idx2 = taskName.indexOf('$', idx);
    if (idx2 != -1) idx = idx2;
    String taskLogStepAtt = "log.step." + taskName.substring(idx + 1, taskName.length() - 4 );
    if (config.get(taskLogStepAtt, null) != null) {
      logStepAtt = taskLogStepAtt;
    }
    logStep = config.get(logStepAtt, DEFAULT_LOG_STEP);
    if (logStep <= 0) {
      logStep = Integer.MAX_VALUE;
    }
  }
  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public void close() throws Exception {
  }
  public final int runAndMaybeStats(boolean reportStats) throws Exception {
    if (!reportStats || shouldNotRecordStats()) {
      setup();
      int count = doLogic();
      count = disableCounting ? 0 : count;
      tearDown();
      return count;
    }
    if (reportStats && depth <= maxDepthLogStart && !shouldNeverLogAtStart()) {
      System.out.println("------------> starting task: " + getName());
    }
    setup();
    Points pnts = runData.getPoints();
    TaskStats ts = pnts.markTaskStart(this, runData.getConfig().getRoundNumber());
    int count = doLogic();
    count = disableCounting ? 0 : count;
    pnts.markTaskEnd(ts, count);
    tearDown();
    return count;
  }
  public abstract int doLogic() throws Exception;
  public String getName() {
    if (params==null) {
      return name;
    } 
    return new StringBuffer(name).append('(').append(params).append(')').toString();
  }
  protected void setName(String name) {
    this.name = name;
  }
  public PerfRunData getRunData() {
    return runData;
  }
  public int getDepth() {
    return depth;
  }
  public void setDepth(int depth) {
    this.depth = depth;
  }
  String getPadding () {
    char c[] = new char[4*getDepth()];
    for (int i = 0; i < c.length; i++) c[i] = ' ';
    return new String(c);
  }
  @Override
  public String toString() {
    String padd = getPadding();
    StringBuffer sb = new StringBuffer(padd);
    if (disableCounting) {
      sb.append('-');
    }
    sb.append(getName());
    if (getRunInBackground()) {
      sb.append(" &");
      int x = getBackgroundDeltaPriority();
      if (x != 0) {
        sb.append(x);
      }
    }
    return sb.toString();
  }
  int getMaxDepthLogStart() {
    return maxDepthLogStart;
  }
  protected String getLogMessage(int recsCount) {
    return "processed " + recsCount + " records";
  }
  protected boolean shouldNeverLogAtStart () {
    return false;
  }
  protected boolean shouldNotRecordStats () {
    return false;
  }
  public void setup () throws Exception {
  }
  public void tearDown() throws Exception {
    if (++logStepCount % logStep == 0) {
      double time = (System.currentTimeMillis() - runData.getStartTimeMillis()) / 1000.0;
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(2);
      System.out.println(nf.format(time) + " sec --> "
          + Thread.currentThread().getName() + " " + getLogMessage(logStepCount));
    }
  }
  public boolean supportsParams () {
    return false;
  }
  public void setParams(String params) {
    if (!supportsParams()) {
      throw new UnsupportedOperationException(getName()+" does not support command line parameters.");
    }
    this.params = params;
  }
  public String getParams() {
    return params;
  }
  public boolean isDisableCounting() {
    return disableCounting;
  }
  public void setDisableCounting(boolean disableCounting) {
    this.disableCounting = disableCounting;
  }
}
