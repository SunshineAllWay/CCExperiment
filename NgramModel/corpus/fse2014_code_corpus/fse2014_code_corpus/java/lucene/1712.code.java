package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.ThreadInterruptedException;
public class TimeLimitingCollector extends Collector {
  public static final int DEFAULT_RESOLUTION = 20;
  public boolean DEFAULT_GREEDY = false; 
  private static long resolution = DEFAULT_RESOLUTION;
  private boolean greedy = DEFAULT_GREEDY ;
  private static final class TimerThread extends Thread  {
    private volatile long time = 0;
    private TimerThread() {
      super("TimeLimitedCollector timer thread");
      this.setDaemon( true );
    }
    @Override
    public void run() {
      while (true) {
        time += resolution;
        try {
          Thread.sleep( resolution );
        } catch (InterruptedException ie) {
          throw new ThreadInterruptedException(ie);
        }
      }
    }
    public long getMilliseconds() {
      return time;
    }
  }
  public static class TimeExceededException extends RuntimeException {
    private long timeAllowed;
    private long timeElapsed;
    private int lastDocCollected;
    private TimeExceededException(long timeAllowed, long timeElapsed, int lastDocCollected) {
      super("Elapsed time: " + timeElapsed + "Exceeded allowed search time: " + timeAllowed + " ms.");
      this.timeAllowed = timeAllowed;
      this.timeElapsed = timeElapsed;
      this.lastDocCollected = lastDocCollected;
    }
    public long getTimeAllowed() {
      return timeAllowed;
    }
    public long getTimeElapsed() {
      return timeElapsed;
    }
    public int getLastDocCollected() {
      return lastDocCollected;
    }
  }
  private final static TimerThread TIMER_THREAD = new TimerThread();
  static  {
    TIMER_THREAD.start();
  }
  private final long t0;
  private final long timeout;
  private final Collector collector;
  public TimeLimitingCollector(final Collector collector, final long timeAllowed ) {
    this.collector = collector;
    t0 = TIMER_THREAD.getMilliseconds();
    this.timeout = t0 + timeAllowed;
  }
  public static long getResolution() {
    return resolution;
  }
  public static void setResolution(long newResolution) {
    resolution = Math.max(newResolution,5); 
  }
  public boolean isGreedy() {
    return greedy;
  }
  public void setGreedy(boolean greedy) {
    this.greedy = greedy;
  }
  @Override
  public void collect(final int doc) throws IOException {
    long time = TIMER_THREAD.getMilliseconds();
    if (timeout < time) {
      if (greedy) {
        collector.collect(doc);
      }
      throw new TimeExceededException( timeout-t0, time-t0, doc );
    }
    collector.collect(doc);
  }
  @Override
  public void setNextReader(IndexReader reader, int base) throws IOException {
    collector.setNextReader(reader, base);
  }
  @Override
  public void setScorer(Scorer scorer) throws IOException {
    collector.setScorer(scorer);
  }
  @Override
  public boolean acceptsDocsOutOfOrder() {
    return collector.acceptsDocsOutOfOrder();
  }
}
