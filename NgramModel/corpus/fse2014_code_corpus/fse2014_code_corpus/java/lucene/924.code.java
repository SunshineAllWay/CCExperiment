package org.apache.lucene.benchmark.quality;
import java.util.Map;
public class QualityQuery implements Comparable<QualityQuery> {
  private String queryID;
  private Map<String,String> nameValPairs;
  public QualityQuery(String queryID, Map<String,String> nameValPairs) {
    this.queryID = queryID;
    this.nameValPairs = nameValPairs;
  }
  public String[] getNames() {
    return nameValPairs.keySet().toArray(new String[0]);
  }
  public String getValue(String name) {
    return nameValPairs.get(name);
  }
  public String getQueryID() {
    return queryID;
  }
  public int compareTo(QualityQuery other) {
    try {
      int n = Integer.parseInt(queryID);
      int nOther = Integer.parseInt(other.queryID);
      return n - nOther;
    } catch (NumberFormatException e) {
      return queryID.compareTo(other.queryID);
    }
  }
}
