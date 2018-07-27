package org.apache.lucene.util;
import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import junit.framework.TestCase;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.CacheEntry;
import org.apache.lucene.util.FieldCacheSanityChecker.Insanity;
public abstract class LuceneTestCase extends TestCase {
  public static final boolean VERBOSE = LuceneTestCaseJ4.VERBOSE;
  public static final Version TEST_VERSION_CURRENT = LuceneTestCaseJ4.TEST_VERSION_CURRENT;
  public static final File TEMP_DIR = LuceneTestCaseJ4.TEMP_DIR;
  private int savedBoolMaxClauseCount;
  private volatile Thread.UncaughtExceptionHandler savedUncaughtExceptionHandler = null;
  private static class UncaughtExceptionEntry {
    public final Thread thread;
    public final Throwable exception;
    public UncaughtExceptionEntry(Thread thread, Throwable exception) {
      this.thread = thread;
      this.exception = exception;
    }
  }
  private List<UncaughtExceptionEntry> uncaughtExceptions = Collections.synchronizedList(new ArrayList<UncaughtExceptionEntry>());
  public LuceneTestCase() {
    super();
  }
  public LuceneTestCase(String name) {
    super(name);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    savedUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        uncaughtExceptions.add(new UncaughtExceptionEntry(t, e));
        if (savedUncaughtExceptionHandler != null)
          savedUncaughtExceptionHandler.uncaughtException(t, e);
      }
    });
    ConcurrentMergeScheduler.setTestMode();
    savedBoolMaxClauseCount = BooleanQuery.getMaxClauseCount();
  }
  protected void purgeFieldCache(final FieldCache fc) {
    fc.purgeAllCaches();
  }
  protected String getTestLabel() {
    return getClass().getName() + "." + getName();
  }
  @Override
  protected void tearDown() throws Exception {
    BooleanQuery.setMaxClauseCount(savedBoolMaxClauseCount);
    try {
      assertSaneFieldCaches(getTestLabel());
      if (ConcurrentMergeScheduler.anyUnhandledExceptions()) {
        ConcurrentMergeScheduler.clearUnhandledExceptions();
        fail("ConcurrentMergeScheduler hit unhandled exceptions");
      }
    } finally {
      purgeFieldCache(FieldCache.DEFAULT);
    }
    Thread.setDefaultUncaughtExceptionHandler(savedUncaughtExceptionHandler);
    if (!uncaughtExceptions.isEmpty()) {
      System.err.println("The following exceptions were thrown by threads:");
      for (UncaughtExceptionEntry entry : uncaughtExceptions) {
        System.err.println("*** Thread: " + entry.thread.getName() + " ***");
        entry.exception.printStackTrace(System.err);
      }
      fail("Some threads throwed uncaught exceptions!");
    }
    super.tearDown();
  }
  protected void assertSaneFieldCaches(final String msg) {
    final CacheEntry[] entries = FieldCache.DEFAULT.getCacheEntries();
    Insanity[] insanity = null;
    try {
      try {
        insanity = FieldCacheSanityChecker.checkSanity(entries);
      } catch (RuntimeException e) {
        dumpArray(msg+ ": FieldCache", entries, System.err);
        throw e;
      }
      assertEquals(msg + ": Insane FieldCache usage(s) found", 
                   0, insanity.length);
      insanity = null;
    } finally {
      if (null != insanity) {
        dumpArray(msg + ": Insane FieldCache usage(s)", insanity, System.err);
      }
    }
  }
  public static <T> void dumpIterator(String label, Iterator<T> iter, 
                                  PrintStream stream) {
    stream.println("*** BEGIN "+label+" ***");
    if (null == iter) {
      stream.println(" ... NULL ...");
    } else {
      while (iter.hasNext()) {
        stream.println(iter.next().toString());
      }
    }
    stream.println("*** END "+label+" ***");
  }
  public static void dumpArray(String label, Object[] objs, 
                               PrintStream stream) {
    Iterator<Object> iter = (null == objs) ? null : Arrays.asList(objs).iterator();
    dumpIterator(label, iter, stream);
  }
  public Random newRandom() {
    if (seed != null) {
      throw new IllegalStateException("please call LuceneTestCase.newRandom only once per test");
    }
    return newRandom(seedRnd.nextLong());
  }
  public Random newRandom(long seed) {
    if (this.seed != null) {
      throw new IllegalStateException("please call LuceneTestCase.newRandom only once per test");
    }
    this.seed = Long.valueOf(seed);
    return new Random(seed);
  }
  protected File getDataFile(String name) throws IOException {
    try {
      return new File(this.getClass().getResource(name).toURI());
    } catch (Exception e) {
      throw new IOException("Cannot find resource: " + name);
    }
  }
  @Override
  public void runBare() throws Throwable {
    try {
      seed = null;
      super.runBare();
    } catch (Throwable e) {
      if (seed != null) {
        System.out.println("NOTE: random seed of testcase '" + getName() + "' was: " + seed);
      }
      throw e;
    }
  }
  protected Long seed = null;
  private static final Random seedRnd = new Random();
}
