package org.apache.lucene.index;
import java.io.IOException;
public class LogByteSizeMergePolicy extends LogMergePolicy {
  public static final double DEFAULT_MIN_MERGE_MB = 1.6;
  public static final double DEFAULT_MAX_MERGE_MB = Long.MAX_VALUE;
  public LogByteSizeMergePolicy(IndexWriter writer) {
    super(writer);
    minMergeSize = (long) (DEFAULT_MIN_MERGE_MB*1024*1024);
    maxMergeSize = (long) (DEFAULT_MAX_MERGE_MB*1024*1024);
  }
  @Override
  protected long size(SegmentInfo info) throws IOException {
    return sizeBytes(info);
  }
  public void setMaxMergeMB(double mb) {
    maxMergeSize = (long) (mb*1024*1024);
  }
  public double getMaxMergeMB() {
    return ((double) maxMergeSize)/1024/1024;
  }
  public void setMinMergeMB(double mb) {
    minMergeSize = (long) (mb*1024*1024);
  }
  public double getMinMergeMB() {
    return ((double) minMergeSize)/1024/1024;
  }
}
