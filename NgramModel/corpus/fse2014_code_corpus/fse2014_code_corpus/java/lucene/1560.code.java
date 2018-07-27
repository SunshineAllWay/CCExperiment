package org.apache.lucene.index;
import java.io.IOException;
public class LogDocMergePolicy extends LogMergePolicy {
  public static final int DEFAULT_MIN_MERGE_DOCS = 1000;
  public LogDocMergePolicy(IndexWriter writer) {
    super(writer);
    minMergeSize = DEFAULT_MIN_MERGE_DOCS;
    maxMergeSize = Long.MAX_VALUE;
  }
  @Override
  protected long size(SegmentInfo info) throws IOException {
    return sizeDocs(info);
  }
  public void setMinMergeDocs(int minMergeDocs) {
    minMergeSize = minMergeDocs;
  }
  public int getMinMergeDocs() {
    return (int) minMergeSize;
  }
}
