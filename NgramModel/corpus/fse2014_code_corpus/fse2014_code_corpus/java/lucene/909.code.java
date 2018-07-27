package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.QueryMaker;
public class SearchTravTask extends ReadTask {
  protected int traversalSize = Integer.MAX_VALUE;
  public SearchTravTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public boolean withRetrieve() {
    return false;
  }
  @Override
  public boolean withSearch() {
    return true;
  }
  @Override
  public boolean withTraverse() {
    return true;
  }
  @Override
  public boolean withWarm() {
    return false;
  }
  @Override
  public QueryMaker getQueryMaker() {
    return getRunData().getQueryMaker(this);
  }
  @Override
  public int traversalSize() {
    return traversalSize;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    traversalSize = (int)Float.parseFloat(params);
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
