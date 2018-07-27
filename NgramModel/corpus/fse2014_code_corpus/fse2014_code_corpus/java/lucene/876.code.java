package org.apache.lucene.benchmark.byTask.tasks;
import java.util.Random;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
public class DeleteByPercentTask extends PerfTask {
  double percent;
  int numDeleted = 0;
  final Random random;
  public DeleteByPercentTask(PerfRunData runData) {
    super(runData);
    random = new Random(runData.getConfig().get("delete.percent.rand.seed", 1717));
  }
  @Override
  public void setup() throws Exception {
    super.setup();
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    percent = Double.parseDouble(params)/100;
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
  @Override
  public int doLogic() throws Exception {
    IndexReader r = getRunData().getIndexReader();
    int maxDoc = r.maxDoc();
    int numDeleted = 0;
    int numToDelete = ((int) (maxDoc * percent)) - r.numDeletedDocs();
    if (numToDelete < 0) {
      r.undeleteAll();
      numToDelete = (int) (maxDoc * percent);
    }
    while (numDeleted < numToDelete) {
      double delRate = ((double) (numToDelete-numDeleted))/r.numDocs();
      TermDocs termDocs = r.termDocs(null);
      while (termDocs.next() && numDeleted < numToDelete) {
        if (random.nextDouble() <= delRate) {
          r.deleteDocument(termDocs.doc());
          numDeleted++;
        }
      }
      termDocs.close();
    }
    System.out.println("--> processed (delete) " + numDeleted + " docs");
    r.decRef();
    return numDeleted;
  }
}
