package org.apache.lucene.index;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.ThreadInterruptedException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
public class ConcurrentMergeScheduler extends MergeScheduler {
  private int mergeThreadPriority = -1;
  protected List<MergeThread> mergeThreads = new ArrayList<MergeThread>();
  private int maxThreadCount = Math.max(1, Math.min(3, Runtime.getRuntime().availableProcessors()/2));
  private int maxMergeCount = maxThreadCount+2;
  protected Directory dir;
  private boolean closed;
  protected IndexWriter writer;
  protected int mergeThreadCount;
  public ConcurrentMergeScheduler() {
    if (allInstances != null) {
      addMyself();
    }
  }
  public void setMaxThreadCount(int count) {
    if (count < 1) {
      throw new IllegalArgumentException("count should be at least 1");
    }
    if (count > maxMergeCount) {
      throw new IllegalArgumentException("count should be <= maxMergeCount (= " + maxMergeCount + ")");
    }
    maxThreadCount = count;
  }
  public int getMaxThreadCount() {
    return maxThreadCount;
  }
  public void setMaxMergeCount(int count) {
    if (count < 1) {
      throw new IllegalArgumentException("count should be at least 1");
    }
    if (count < maxThreadCount) {
      throw new IllegalArgumentException("count should be >= maxThreadCount (= " + maxThreadCount + ")");
    }
    maxMergeCount = count;
  }
  public int getMaxMergeCount() {
    return maxMergeCount;
  }
  public synchronized int getMergeThreadPriority() {
    initMergeThreadPriority();
    return mergeThreadPriority;
  }
  public synchronized void setMergeThreadPriority(int pri) {
    if (pri > Thread.MAX_PRIORITY || pri < Thread.MIN_PRIORITY)
      throw new IllegalArgumentException("priority must be in range " + Thread.MIN_PRIORITY + " .. " + Thread.MAX_PRIORITY + " inclusive");
    mergeThreadPriority = pri;
    updateMergeThreads();
  }
  protected static class CompareByMergeDocCount implements Comparator<MergeThread> {
    public int compare(MergeThread t1, MergeThread t2) {
      final MergePolicy.OneMerge m1 = t1.getCurrentMerge();
      final MergePolicy.OneMerge m2 = t2.getCurrentMerge();
      final int c1 = m1 == null ? Integer.MAX_VALUE : m1.segments.totalDocCount();
      final int c2 = m2 == null ? Integer.MAX_VALUE : m2.segments.totalDocCount();
      return c2 - c1;
    }
  }
  protected synchronized void updateMergeThreads() {
    Collections.sort(mergeThreads, new CompareByMergeDocCount());
    final int count = mergeThreads.size();
    int pri = mergeThreadPriority;
    for(int i=0;i<count;i++) {
      final MergeThread mergeThread = mergeThreads.get(i);
      final MergePolicy.OneMerge merge = mergeThread.getCurrentMerge();
      if (merge == null) {
        continue;
      }
      final boolean doPause;
      if (i < count-maxThreadCount) {
        doPause = true;
      } else {
        doPause = false;
      }
      if (verbose()) {
        if (doPause != merge.getPause()) {
          if (doPause) {
            message("pause thread " + mergeThread.getName());
          } else {
            message("unpause thread " + mergeThread.getName());
          }
        }
      }
      if (doPause != merge.getPause()) {
        merge.setPause(doPause);
      }
      if (!doPause) {
        if (verbose()) {
          message("set priority of merge thread " + mergeThread.getName() + " to " + pri);
        }
        mergeThread.setThreadPriority(pri);
        pri = Math.min(Thread.MAX_PRIORITY, 1+pri);
      }
    }
  }
  private boolean verbose() {
    return writer != null && writer.verbose();
  }
  private void message(String message) {
    if (verbose())
      writer.message("CMS: " + message);
  }
  private synchronized void initMergeThreadPriority() {
    if (mergeThreadPriority == -1) {
      mergeThreadPriority = 1+Thread.currentThread().getPriority();
      if (mergeThreadPriority > Thread.MAX_PRIORITY)
        mergeThreadPriority = Thread.MAX_PRIORITY;
    }
  }
  @Override
  public void close() {
    closed = true;
  }
  public synchronized void sync() {
    while(mergeThreadCount() > 0) {
      if (verbose())
        message("now wait for threads; currently " + mergeThreads.size() + " still running");
      final int count = mergeThreads.size();
      if (verbose()) {
        for(int i=0;i<count;i++)
          message("    " + i + ": " + mergeThreads.get(i));
      }
      try {
        wait();
      } catch (InterruptedException ie) {
        throw new ThreadInterruptedException(ie);
      }
    }
  }
  private synchronized int mergeThreadCount() {
    int count = 0;
    final int numThreads = mergeThreads.size();
    for(int i=0;i<numThreads;i++)
      if (mergeThreads.get(i).isAlive())
        count++;
    return count;
  }
  @Override
  public void merge(IndexWriter writer)
    throws CorruptIndexException, IOException {
    assert !Thread.holdsLock(writer);
    this.writer = writer;
    initMergeThreadPriority();
    dir = writer.getDirectory();
    if (verbose()) {
      message("now merge");
      message("  index: " + writer.segString());
    }
    while(true) {
      MergePolicy.OneMerge merge = writer.getNextMerge();
      if (merge == null) {
        if (verbose())
          message("  no more merges pending; now return");
        return;
      }
      writer.mergeInit(merge);
      boolean success = false;
      try {
        synchronized(this) {
          final MergeThread merger;
          long startStallTime = 0;
          while (mergeThreadCount() >= maxMergeCount) {
            startStallTime = System.currentTimeMillis();
            if (verbose()) {
              message("    too many merges; stalling...");
            }
            try {
              wait();
            } catch (InterruptedException ie) {
              throw new ThreadInterruptedException(ie);
            }
          }
          if (verbose()) {
            if (startStallTime != 0) {
              message("  stalled for " + (System.currentTimeMillis()-startStallTime) + " msec");
            }
            message("  consider merge " + merge.segString(dir));
          }
          assert mergeThreadCount() < maxMergeCount;
          merger = getMergeThread(writer, merge);
          mergeThreads.add(merger);
          updateMergeThreads();
          if (verbose())
            message("    launch new thread [" + merger.getName() + "]");
          merger.start();
          success = true;
        }
      } finally {
        if (!success) {
          writer.mergeFinish(merge);
        }
      }
    }
  }
  protected void doMerge(MergePolicy.OneMerge merge)
    throws IOException {
    writer.merge(merge);
  }
  protected synchronized MergeThread getMergeThread(IndexWriter writer, MergePolicy.OneMerge merge) throws IOException {
    final MergeThread thread = new MergeThread(writer, merge);
    thread.setThreadPriority(mergeThreadPriority);
    thread.setDaemon(true);
    thread.setName("Lucene Merge Thread #" + mergeThreadCount++);
    return thread;
  }
  protected class MergeThread extends Thread {
    IndexWriter tWriter;
    MergePolicy.OneMerge startMerge;
    MergePolicy.OneMerge runningMerge;
    private volatile boolean done;
    public MergeThread(IndexWriter writer, MergePolicy.OneMerge startMerge) throws IOException {
      this.tWriter = writer;
      this.startMerge = startMerge;
    }
    public synchronized void setRunningMerge(MergePolicy.OneMerge merge) {
      runningMerge = merge;
    }
    public synchronized MergePolicy.OneMerge getRunningMerge() {
      return runningMerge;
    }
    public synchronized MergePolicy.OneMerge getCurrentMerge() {
      if (done) {
        return null;
      } else if (runningMerge != null) {
        return runningMerge;
      } else {
        return startMerge;
      }
    }
    public void setThreadPriority(int pri) {
      try {
        setPriority(pri);
      } catch (NullPointerException npe) {
      } catch (SecurityException se) {
      }
    }
    @Override
    public void run() {
      MergePolicy.OneMerge merge = this.startMerge;
      try {
        if (verbose())
          message("  merge thread: start");
        while(true) {
          setRunningMerge(merge);
          doMerge(merge);
          merge = tWriter.getNextMerge();
          if (merge != null) {
            tWriter.mergeInit(merge);
            updateMergeThreads();
            if (verbose())
              message("  merge thread: do another merge " + merge.segString(dir));
          } else {
            done = true;
            updateMergeThreads();
            break;
          }
        }
        if (verbose())
          message("  merge thread: done");
      } catch (Throwable exc) {
        if (!(exc instanceof MergePolicy.MergeAbortedException)) {
          if (!suppressExceptions) {
            anyExceptions = true;
            handleMergeException(exc);
          }
        }
      } finally {
        synchronized(ConcurrentMergeScheduler.this) {
          ConcurrentMergeScheduler.this.notifyAll();
          boolean removed = mergeThreads.remove(this);
          assert removed;
          updateMergeThreads();
        }
      }
    }
    @Override
    public String toString() {
      MergePolicy.OneMerge merge = getRunningMerge();
      if (merge == null)
        merge = startMerge;
      return "merge thread: " + merge.segString(dir);
    }
  }
  protected void handleMergeException(Throwable exc) {
    try {
      Thread.sleep(250);
    } catch (InterruptedException ie) {
      throw new ThreadInterruptedException(ie);
    }
    throw new MergePolicy.MergeException(exc, dir);
  }
  static boolean anyExceptions = false;
  public static boolean anyUnhandledExceptions() {
    if (allInstances == null) {
      throw new RuntimeException("setTestMode() was not called; often this is because your test case's setUp method fails to call super.setUp in LuceneTestCase");
    }
    synchronized(allInstances) {
      final int count = allInstances.size();
      for(int i=0;i<count;i++)
        allInstances.get(i).sync();
      boolean v = anyExceptions;
      anyExceptions = false;
      return v;
    }
  }
  public static void clearUnhandledExceptions() {
    synchronized(allInstances) {
      anyExceptions = false;
    }
  }
  private void addMyself() {
    synchronized(allInstances) {
      final int size = allInstances.size();
      int upto = 0;
      for(int i=0;i<size;i++) {
        final ConcurrentMergeScheduler other = allInstances.get(i);
        if (!(other.closed && 0 == other.mergeThreadCount()))
          allInstances.set(upto++, other);
      }
      allInstances.subList(upto, allInstances.size()).clear();
      allInstances.add(this);
    }
  }
  private boolean suppressExceptions;
  void setSuppressExceptions() {
    suppressExceptions = true;
  }
  void clearSuppressExceptions() {
    suppressExceptions = false;
  }
  private static List<ConcurrentMergeScheduler> allInstances;
  public static void setTestMode() {
    allInstances = new ArrayList<ConcurrentMergeScheduler>();
  }
}
