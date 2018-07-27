package org.apache.lucene.index;
import java.io.IOException;
public class SerialMergeScheduler extends MergeScheduler {
  @Override
  synchronized public void merge(IndexWriter writer)
    throws CorruptIndexException, IOException {
    while(true) {
      MergePolicy.OneMerge merge = writer.getNextMerge();
      if (merge == null)
        break;
      writer.merge(merge);
    }
  }
  @Override
  public void close() {}
}
