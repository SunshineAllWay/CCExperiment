package org.apache.lucene.benchmark.quality;
import java.io.PrintWriter;
public interface Judge {
  public boolean isRelevant(String docName, QualityQuery query);
  public boolean validateData (QualityQuery qq[], PrintWriter logger);
  public int maxRecall (QualityQuery query);
}
