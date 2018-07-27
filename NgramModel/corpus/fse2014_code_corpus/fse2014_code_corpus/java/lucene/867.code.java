package org.apache.lucene.benchmark.byTask.stats;
import org.apache.lucene.benchmark.byTask.tasks.PerfTask;
public class TaskStats implements Cloneable {
  private PerfTask task; 
  private int round;
  private long start;
  private long elapsed = -1;
  private long maxTotMem;
  private long maxUsedMem;
  private int taskRunNum;
  private int numParallelTasks;
  private int count;
  private int numRuns = 1;
  TaskStats (PerfTask task, int taskRunNum, int round) {
    this.task = task;
    this.taskRunNum = taskRunNum;
    this.round = round;
    maxTotMem = Runtime.getRuntime().totalMemory();
    maxUsedMem = maxTotMem - Runtime.getRuntime().freeMemory();
    start = System.currentTimeMillis();
  }
  void markEnd (int numParallelTasks, int count) {
    elapsed = System.currentTimeMillis() - start;
    long totMem = Runtime.getRuntime().totalMemory();
    if (totMem > maxTotMem) {
      maxTotMem = totMem;
    }
    long usedMem = totMem - Runtime.getRuntime().freeMemory();
    if (usedMem > maxUsedMem) {
      maxUsedMem = usedMem;
    }
    this.numParallelTasks = numParallelTasks;
    this.count = count;
  }
  private int[] countsByTime;
  private long countsByTimeStepMSec;
  public void setCountsByTime(int[] counts, long msecStep) {
    countsByTime = counts;
    countsByTimeStepMSec = msecStep;
  }
  public int[] getCountsByTime() {
    return countsByTime;
  }
  public long getCountsByTimeStepMSec() {
    return countsByTimeStepMSec;
  }
  public int getTaskRunNum() {
    return taskRunNum;
  }
  @Override
  public String toString() {
    StringBuffer res = new StringBuffer(task.getName());
    res.append(" ");
    res.append(count);
    res.append(" ");
    res.append(elapsed);
    return res.toString();
  }
  public int getCount() {
    return count;
  }
  public long getElapsed() {
    return elapsed;
  }
  public long getMaxTotMem() {
    return maxTotMem;
  }
  public long getMaxUsedMem() {
    return maxUsedMem;
  }
  public int getNumParallelTasks() {
    return numParallelTasks;
  }
  public PerfTask getTask() {
    return task;
  }
  public int getNumRuns() {
    return numRuns;
  }
  public void add(TaskStats stat2) {
    numRuns += stat2.getNumRuns();
    elapsed += stat2.getElapsed();
    maxTotMem += stat2.getMaxTotMem();
    maxUsedMem += stat2.getMaxUsedMem();
    count += stat2.getCount();
    if (round != stat2.round) {
      round = -1; 
    }
    if (countsByTime != null && stat2.countsByTime != null) {
      if (countsByTimeStepMSec != stat2.countsByTimeStepMSec) {
        throw new IllegalStateException("different by-time msec step");
      }
      if (countsByTime.length != stat2.countsByTime.length) {
        throw new IllegalStateException("different by-time msec count");
      }
      for(int i=0;i<stat2.countsByTime.length;i++) {
        countsByTime[i] += stat2.countsByTime[i];
      }
    }
  }
  @Override
  public Object clone() throws CloneNotSupportedException {
    TaskStats c = (TaskStats) super.clone();
    if (c.countsByTime != null) {
      c.countsByTime = c.countsByTime.clone();
    }
    return c;
  }
  public int getRound() {
    return round;
  }
}
