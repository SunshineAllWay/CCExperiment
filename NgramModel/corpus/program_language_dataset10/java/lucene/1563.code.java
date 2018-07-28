package org.apache.lucene.index;
import org.apache.lucene.store.Directory;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
public abstract class MergePolicy implements java.io.Closeable {
  public static class OneMerge {
    SegmentInfo info;               
    boolean mergeDocStores;         
    boolean optimize;               
    boolean increfDone;             
    boolean registerDone;           
    long mergeGen;                  
    boolean isExternal;             
    int maxNumSegmentsOptimize;     
    SegmentReader[] readers;        
    SegmentReader[] readersClone;   
    final SegmentInfos segments;
    final boolean useCompoundFile;
    boolean aborted;
    Throwable error;
    boolean paused;
    public OneMerge(SegmentInfos segments, boolean useCompoundFile) {
      if (0 == segments.size())
        throw new RuntimeException("segments must include at least one segment");
      this.segments = segments;
      this.useCompoundFile = useCompoundFile;
    }
    synchronized void setException(Throwable error) {
      this.error = error;
    }
    synchronized Throwable getException() {
      return error;
    }
    synchronized void abort() {
      aborted = true;
      notifyAll();
    }
    synchronized boolean isAborted() {
      return aborted;
    }
    synchronized void checkAborted(Directory dir) throws MergeAbortedException {
      if (aborted) {
        throw new MergeAbortedException("merge is aborted: " + segString(dir));
      }
      while (paused) {
        try {
          wait(1000);
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
        if (aborted) {
          throw new MergeAbortedException("merge is aborted: " + segString(dir));
        }
      }
    }
    synchronized public void setPause(boolean paused) {
      this.paused = paused;
      if (!paused) {
        notifyAll();
      }
    }
    synchronized public boolean getPause() {
      return paused;
    }
    String segString(Directory dir) {
      StringBuilder b = new StringBuilder();
      final int numSegments = segments.size();
      for(int i=0;i<numSegments;i++) {
        if (i > 0) b.append(' ');
        b.append(segments.info(i).toString(dir, 0));
      }
      if (info != null)
        b.append(" into ").append(info.name);
      if (optimize)
        b.append(" [optimize]");
      if (mergeDocStores) {
        b.append(" [mergeDocStores]");
      }
      return b.toString();
    }
  }
  public static class MergeSpecification {
    public List<OneMerge> merges = new ArrayList<OneMerge>();
    public void add(OneMerge merge) {
      merges.add(merge);
    }
    public String segString(Directory dir) {
      StringBuilder b = new StringBuilder();
      b.append("MergeSpec:\n");
      final int count = merges.size();
      for(int i=0;i<count;i++)
        b.append("  ").append(1 + i).append(": ").append(merges.get(i).segString(dir));
      return b.toString();
    }
  }
  public static class MergeException extends RuntimeException {
    private Directory dir;
    public MergeException(String message, Directory dir) {
      super(message);
      this.dir = dir;
    }
    public MergeException(Throwable exc, Directory dir) {
      super(exc);
      this.dir = dir;
    }
    public Directory getDirectory() {
      return dir;
    }
  }
  public static class MergeAbortedException extends IOException {
    public MergeAbortedException() {
      super("merge is aborted");
    }
    public MergeAbortedException(String message) {
      super(message);
    }
  }
  final protected IndexWriter writer;
  public MergePolicy(IndexWriter writer) {
    this.writer = writer;
  }
  public abstract MergeSpecification findMerges(SegmentInfos segmentInfos)
      throws CorruptIndexException, IOException;
  public abstract MergeSpecification findMergesForOptimize(
      SegmentInfos segmentInfos, int maxSegmentCount, Set<SegmentInfo> segmentsToOptimize)
      throws CorruptIndexException, IOException;
  public abstract MergeSpecification findMergesToExpungeDeletes(
      SegmentInfos segmentInfos) throws CorruptIndexException, IOException;
  public abstract void close();
  public abstract boolean useCompoundFile(SegmentInfos segments, SegmentInfo newSegment);
  public abstract boolean useCompoundDocStore(SegmentInfos segments);
}
