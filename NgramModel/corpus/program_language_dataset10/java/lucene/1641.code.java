package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
public abstract class Collector {
  public abstract void setScorer(Scorer scorer) throws IOException;
  public abstract void collect(int doc) throws IOException;
  public abstract void setNextReader(IndexReader reader, int docBase) throws IOException;
  public abstract boolean acceptsDocsOutOfOrder();
}
