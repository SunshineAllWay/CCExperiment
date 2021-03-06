package org.apache.lucene.index;
import java.io.IOException;
import java.util.Set;
public abstract class LogMergePolicy extends MergePolicy {
  public static final double LEVEL_LOG_SPAN = 0.75;
  public static final int DEFAULT_MERGE_FACTOR = 10;
  public static final int DEFAULT_MAX_MERGE_DOCS = Integer.MAX_VALUE;
  private int mergeFactor = DEFAULT_MERGE_FACTOR;
  long minMergeSize;
  long maxMergeSize;
  int maxMergeDocs = DEFAULT_MAX_MERGE_DOCS;
  protected boolean calibrateSizeByDeletes = false;
  private boolean useCompoundFile = true;
  private boolean useCompoundDocStore = true;
  public LogMergePolicy(IndexWriter writer) {
    super(writer);
  }
  protected boolean verbose() {
    return writer != null && writer.verbose();
  }
  private void message(String message) {
    if (verbose())
      writer.message("LMP: " + message);
  }
  public int getMergeFactor() {
    return mergeFactor;
  }
  public void setMergeFactor(int mergeFactor) {
    if (mergeFactor < 2)
      throw new IllegalArgumentException("mergeFactor cannot be less than 2");
    this.mergeFactor = mergeFactor;
  }
  @Override
  public boolean useCompoundFile(SegmentInfos infos, SegmentInfo info) {
    return useCompoundFile;
  }
  public void setUseCompoundFile(boolean useCompoundFile) {
    this.useCompoundFile = useCompoundFile;
  }
  public boolean getUseCompoundFile() {
    return useCompoundFile;
  }
  @Override
  public boolean useCompoundDocStore(SegmentInfos infos) {
    return useCompoundDocStore;
  }
  public void setUseCompoundDocStore(boolean useCompoundDocStore) {
    this.useCompoundDocStore = useCompoundDocStore;
  }
  public boolean getUseCompoundDocStore() {
    return useCompoundDocStore;
  }
  public void setCalibrateSizeByDeletes(boolean calibrateSizeByDeletes) {
    this.calibrateSizeByDeletes = calibrateSizeByDeletes;
  }
  public boolean getCalibrateSizeByDeletes() {
    return calibrateSizeByDeletes;
  }
  @Override
  public void close() {}
  abstract protected long size(SegmentInfo info) throws IOException;
  protected long sizeDocs(SegmentInfo info) throws IOException {
    if (calibrateSizeByDeletes) {
      int delCount = writer.numDeletedDocs(info);
      return (info.docCount - (long)delCount);
    } else {
      return info.docCount;
    }
  }
  protected long sizeBytes(SegmentInfo info) throws IOException {
    long byteSize = info.sizeInBytes();
    if (calibrateSizeByDeletes) {
      int delCount = writer.numDeletedDocs(info);
      float delRatio = (info.docCount <= 0 ? 0.0f : ((float)delCount / (float)info.docCount));
      return (info.docCount <= 0 ?  byteSize : (long)(byteSize * (1.0f - delRatio)));
    } else {
      return byteSize;
    }
  }
  private boolean isOptimized(SegmentInfos infos, int maxNumSegments, Set<SegmentInfo> segmentsToOptimize) throws IOException {
    final int numSegments = infos.size();
    int numToOptimize = 0;
    SegmentInfo optimizeInfo = null;
    for(int i=0;i<numSegments && numToOptimize <= maxNumSegments;i++) {
      final SegmentInfo info = infos.info(i);
      if (segmentsToOptimize.contains(info)) {
        numToOptimize++;
        optimizeInfo = info;
      }
    }
    return numToOptimize <= maxNumSegments &&
      (numToOptimize != 1 || isOptimized(optimizeInfo));
  }
  private boolean isOptimized(SegmentInfo info)
    throws IOException {
    boolean hasDeletions = writer.numDeletedDocs(info) > 0;
    return !hasDeletions &&
      !info.hasSeparateNorms() &&
      info.dir == writer.getDirectory() &&
      info.getUseCompoundFile() == useCompoundFile;
  }
  @Override
  public MergeSpecification findMergesForOptimize(SegmentInfos infos,
      int maxNumSegments, Set<SegmentInfo> segmentsToOptimize) throws IOException {
    MergeSpecification spec;
    assert maxNumSegments > 0;
    if (!isOptimized(infos, maxNumSegments, segmentsToOptimize)) {
      int last = infos.size();
      while(last > 0) {
        final SegmentInfo info = infos.info(--last);
        if (segmentsToOptimize.contains(info)) {
          last++;
          break;
        }
      }
      if (last > 0) {
        spec = new MergeSpecification();
        while (last - maxNumSegments + 1 >= mergeFactor) {
          spec.add(new OneMerge(infos.range(last-mergeFactor, last), useCompoundFile));
          last -= mergeFactor;
        }
        if (0 == spec.merges.size()) {
          if (maxNumSegments == 1) {
            if (last > 1 || !isOptimized(infos.info(0)))
              spec.add(new OneMerge(infos.range(0, last), useCompoundFile));
          } else if (last > maxNumSegments) {
            final int finalMergeSize = last - maxNumSegments + 1;
            long bestSize = 0;
            int bestStart = 0;
            for(int i=0;i<last-finalMergeSize+1;i++) {
              long sumSize = 0;
              for(int j=0;j<finalMergeSize;j++)
                sumSize += size(infos.info(j+i));
              if (i == 0 || (sumSize < 2*size(infos.info(i-1)) && sumSize < bestSize)) {
                bestStart = i;
                bestSize = sumSize;
              }
            }
            spec.add(new OneMerge(infos.range(bestStart, bestStart+finalMergeSize), useCompoundFile));
          }
        }
      } else
        spec = null;
    } else
      spec = null;
    return spec;
  }
  @Override
  public MergeSpecification findMergesToExpungeDeletes(SegmentInfos segmentInfos)
      throws CorruptIndexException, IOException {
    final int numSegments = segmentInfos.size();
    if (verbose())
      message("findMergesToExpungeDeletes: " + numSegments + " segments");
    MergeSpecification spec = new MergeSpecification();
    int firstSegmentWithDeletions = -1;
    for(int i=0;i<numSegments;i++) {
      final SegmentInfo info = segmentInfos.info(i);
      int delCount = writer.numDeletedDocs(info);
      if (delCount > 0) {
        if (verbose())
          message("  segment " + info.name + " has deletions");
        if (firstSegmentWithDeletions == -1)
          firstSegmentWithDeletions = i;
        else if (i - firstSegmentWithDeletions == mergeFactor) {
          if (verbose())
            message("  add merge " + firstSegmentWithDeletions + " to " + (i-1) + " inclusive");
          spec.add(new OneMerge(segmentInfos.range(firstSegmentWithDeletions, i), useCompoundFile));
          firstSegmentWithDeletions = i;
        }
      } else if (firstSegmentWithDeletions != -1) {
        if (verbose())
          message("  add merge " + firstSegmentWithDeletions + " to " + (i-1) + " inclusive");
        spec.add(new OneMerge(segmentInfos.range(firstSegmentWithDeletions, i), useCompoundFile));
        firstSegmentWithDeletions = -1;
      }
    }
    if (firstSegmentWithDeletions != -1) {
      if (verbose())
        message("  add merge " + firstSegmentWithDeletions + " to " + (numSegments-1) + " inclusive");
      spec.add(new OneMerge(segmentInfos.range(firstSegmentWithDeletions, numSegments), useCompoundFile));
    }
    return spec;
  }
  @Override
  public MergeSpecification findMerges(SegmentInfos infos) throws IOException {
    final int numSegments = infos.size();
    if (verbose())
      message("findMerges: " + numSegments + " segments");
    float[] levels = new float[numSegments];
    final float norm = (float) Math.log(mergeFactor);
    for(int i=0;i<numSegments;i++) {
      final SegmentInfo info = infos.info(i);
      long size = size(info);
      if (size < 1)
        size = 1;
      levels[i] = (float) Math.log(size)/norm;
    }
    final float levelFloor;
    if (minMergeSize <= 0)
      levelFloor = (float) 0.0;
    else
      levelFloor = (float) (Math.log(minMergeSize)/norm);
    MergeSpecification spec = null;
    int start = 0;
    while(start < numSegments) {
      float maxLevel = levels[start];
      for(int i=1+start;i<numSegments;i++) {
        final float level = levels[i];
        if (level > maxLevel)
          maxLevel = level;
      }
      float levelBottom;
      if (maxLevel < levelFloor)
        levelBottom = -1.0F;
      else {
        levelBottom = (float) (maxLevel - LEVEL_LOG_SPAN);
        if (levelBottom < levelFloor && maxLevel >= levelFloor)
          levelBottom = levelFloor;
      }
      int upto = numSegments-1;
      while(upto >= start) {
        if (levels[upto] >= levelBottom) {
          break;
        }
        upto--;
      }
      if (verbose())
        message("  level " + levelBottom + " to " + maxLevel + ": " + (1+upto-start) + " segments");
      int end = start + mergeFactor;
      while(end <= 1+upto) {
        boolean anyTooLarge = false;
        for(int i=start;i<end;i++) {
          final SegmentInfo info = infos.info(i);
          anyTooLarge |= (size(info) >= maxMergeSize || sizeDocs(info) >= maxMergeDocs);
        }
        if (!anyTooLarge) {
          if (spec == null)
            spec = new MergeSpecification();
          if (verbose())
            message("    " + start + " to " + end + ": add this merge");
          spec.add(new OneMerge(infos.range(start, end), useCompoundFile));
        } else if (verbose())
          message("    " + start + " to " + end + ": contains segment over maxMergeSize or maxMergeDocs; skipping");
        start = end;
        end = start + mergeFactor;
      }
      start = 1+upto;
    }
    return spec;
  }
  public void setMaxMergeDocs(int maxMergeDocs) {
    this.maxMergeDocs = maxMergeDocs;
  }
  public int getMaxMergeDocs() {
    return maxMergeDocs;
  }
}
