package org.apache.lucene.benchmark.byTask.feeds;
import org.apache.lucene.search.Query;
import org.apache.lucene.benchmark.byTask.utils.Config;
public interface QueryMaker {
  public Query makeQuery (int size) throws Exception;
  public Query makeQuery () throws Exception;
  public void setConfig (Config config) throws Exception;
  public void resetInputs();
  public String printQueries();
}
