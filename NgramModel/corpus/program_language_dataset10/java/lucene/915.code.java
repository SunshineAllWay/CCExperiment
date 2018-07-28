package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.QueryMaker;
public class WarmTask extends ReadTask {
  public WarmTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public boolean withRetrieve() {
    return false;
  }
  @Override
  public boolean withSearch() {
    return false;
  }
  @Override
  public boolean withTraverse() {
    return false;
  }
  @Override
  public boolean withWarm() {
    return true;
  }
  @Override
  public QueryMaker getQueryMaker() {
    return null; 
  }
}
