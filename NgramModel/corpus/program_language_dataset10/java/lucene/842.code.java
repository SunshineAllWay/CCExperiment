package org.apache.lucene.benchmark.byTask.feeds;
import org.apache.lucene.search.Query;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.benchmark.byTask.utils.Format;
public abstract class AbstractQueryMaker implements QueryMaker {
  protected int qnum = 0;
  protected Query[] queries;
  protected Config config;
  public void resetInputs() {
    qnum = 0;
  }
  protected abstract Query[] prepareQueries() throws Exception;
  public void setConfig(Config config) throws Exception {
    this.config = config;
    queries = prepareQueries();
  }
  public String printQueries() {
    String newline = System.getProperty("line.separator");
    StringBuffer sb = new StringBuffer();
    if (queries != null) {
      for (int i = 0; i < queries.length; i++) {
        sb.append(i+". "+ Format.simpleName(queries[i].getClass())+" - "+queries[i].toString());
        sb.append(newline);
      }
    }
    return sb.toString();
  }
  public Query makeQuery() throws Exception {
    return queries[nextQnum()];
  }
  protected synchronized int nextQnum() {
    int res = qnum;
    qnum = (qnum+1) % queries.length;
    return res;
  }
  public Query makeQuery(int size) throws Exception {
    throw new Exception(this+".makeQuery(int size) is not supported!");
  }
}
