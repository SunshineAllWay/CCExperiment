package org.apache.lucene.benchmark.byTask.tasks;
import java.util.List;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.stats.Report;
import org.apache.lucene.benchmark.byTask.stats.TaskStats;
public class RepAllTask extends ReportTask {
  public RepAllTask(PerfRunData runData) {
    super(runData);
   }
  @Override
  public int doLogic() throws Exception {
    Report rp = reportAll(getRunData().getPoints().taskStats());
    System.out.println();
    System.out.println("------------> Report All ("+rp.getSize()+" out of "+rp.getOutOf()+")");
    System.out.println(rp.getText());
    System.out.println();
    return 0;
  }
  protected Report reportAll(List<TaskStats> taskStats) {
    String longestOp = longestOp(taskStats);
    boolean first = true;
    StringBuffer sb = new StringBuffer();
    sb.append(tableTitle(longestOp));
    sb.append(newline);
    int reported = 0;
    for (final TaskStats stat : taskStats) {
      if (stat.getElapsed()>=0) { 
        if (!first) {
          sb.append(newline);
        }
        first = false;
        String line = taskReportLine(longestOp, stat);
        reported++;
        if (taskStats.size()>2 && reported%2==0) {
          line = line.replaceAll("   "," - ");
        }
        sb.append(line);
      }
    }
    String reptxt = (reported==0 ? "No Matching Entries Were Found!" : sb.toString());
    return new Report(reptxt,reported,reported,taskStats.size());
  }
}
