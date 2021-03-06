package org.apache.lucene.search;
import java.io.IOException;
import java.io.Serializable;
import org.apache.lucene.index.IndexReader;
public abstract class Weight implements Serializable {
  public abstract Explanation explain(IndexReader reader, int doc) throws IOException;
  public abstract Query getQuery();
  public abstract float getValue();
  public abstract void normalize(float norm);
  public abstract Scorer scorer(IndexReader reader, boolean scoreDocsInOrder,
      boolean topScorer) throws IOException;
  public abstract float sumOfSquaredWeights() throws IOException;
  public boolean scoresDocsOutOfOrder() { return false; }
}
