package org.apache.lucene.benchmark.byTask.tasks;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.stats.Report;
import org.apache.lucene.benchmark.byTask.stats.TaskStats;
public class RepSumByNameRoundTask extends ReportTask {
  public RepSumByNameRoundTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws Exception {
    Report rp = reportSumByNameRound(getRunData().getPoints().taskStats());
    System.out.println();
    System.out.println("------------> Report Sum By (any) Name and Round ("+
        rp.getSize()+" about "+rp.getReported()+" out of "+rp.getOutOf()+")");
    System.out.println(rp.getText());
    System.out.println();
    return 0;
  }
  protected Report reportSumByNameRound(List<TaskStats> taskStats) {
    LinkedHashMap<String,TaskStats> p2 = new LinkedHashMap<String,TaskStats>();
    int reported = 0;
    for (final TaskStats stat1 : taskStats) {
      if (stat1.getElapsed()>=0) { 
        reported++;
        String name = stat1.getTask().getName();
        String rname = stat1.getRound()+"."+name; 
        TaskStats stat2 = p2.get(rname);
        if (stat2 == null) {
          try {
            stat2 = (TaskStats) stat1.clone();
          } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
          }
          p2.put(rname,stat2);
        } else {
          stat2.add(stat1);
        }
      }
    }
    return genPartialReport(reported, p2, taskStats.size());
  }
}
