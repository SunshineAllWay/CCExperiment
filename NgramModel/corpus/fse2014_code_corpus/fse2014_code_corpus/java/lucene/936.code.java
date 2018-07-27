package org.apache.lucene.benchmark.stats;
import java.util.Vector;
import org.apache.lucene.search.Query;
import org.apache.lucene.benchmark.Constants;
public class QueryData {
  public String id;
  public Query q;
  public boolean reopen;
  public boolean warmup;
  public boolean retrieve;
  public static QueryData[] getAll(Query[] queries) {
    Vector<QueryData> vqd = new Vector<QueryData>();
    for (int i = 0; i < queries.length; i++) {
      for (int r = 1; r >= 0; r--) {
        for (int w = 1; w >= 0; w--) {
          for (int t = 0; t < 2; t++) {
            QueryData qd = new QueryData();
            qd.id="qd-" + i + r + w + t;
            qd.reopen = Constants.BOOLEANS[r].booleanValue();
            qd.warmup = Constants.BOOLEANS[w].booleanValue();
            qd.retrieve = Constants.BOOLEANS[t].booleanValue();
            qd.q = queries[i];
            vqd.add(qd);
          }
        }
      }
    }
    return vqd.toArray(new QueryData[0]);
  }
  public static String getLabels() {
    return "# Query data: R-reopen, W-warmup, T-retrieve, N-no";
  }
  @Override
  public String toString() {
    return id + " " + (reopen ? "R" : "NR") + " " + (warmup ? "W" : "NW") +
      " " + (retrieve ? "T" : "NT") + " [" + q.toString() + "]";
  }
}
