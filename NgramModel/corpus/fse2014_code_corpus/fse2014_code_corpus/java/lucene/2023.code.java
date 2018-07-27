package org.apache.lucene.util;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.CacheEntry;
import org.apache.lucene.util.FieldCacheSanityChecker.Insanity;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;
import java.lang.reflect.Method;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
public class LuceneTestCaseJ4 {
  public static final boolean VERBOSE = Boolean.getBoolean("tests.verbose");
  public static final Version TEST_VERSION_CURRENT = Version.LUCENE_31;
  public static final File TEMP_DIR;
  static {
    String s = System.getProperty("tempDir", System.getProperty("java.io.tmpdir"));
    if (s == null)
      throw new RuntimeException("To run tests, you need to define system property 'tempDir' or 'java.io.tmpdir'.");
    TEMP_DIR = new File(s);
  }
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
  private static final Object PLACEHOLDER = new Object();
  private static final Map<Class<? extends LuceneTestCaseJ4>,Object> checkedClasses =
    Collections.synchronizedMap(new WeakHashMap<Class<? extends LuceneTestCaseJ4>,Object>());
  @Rule
  public final TestWatchman intercept = new TestWatchman() {
    @Override
    public void failed(Throwable e, FrameworkMethod method) {
      reportAdditionalFailureInfo();
      super.failed(e, method);
    }
    @Override
    public void starting(FrameworkMethod method) {
      LuceneTestCaseJ4.this.name = method.getName();
      final Class<? extends LuceneTestCaseJ4> clazz = LuceneTestCaseJ4.this.getClass();
      if (!checkedClasses.containsKey(clazz)) {
        checkedClasses.put(clazz, PLACEHOLDER);
        for (Method m : clazz.getMethods()) {
          if (m.getName().startsWith("test") && m.getAnnotation(Test.class) == null) {
            fail("In class '" + clazz.getName() + "' the method '" + m.getName() + "' is not annotated with @Test.");
          }
        }
      }
      super.starting(method);
    }
  };
  @Before
  public void setUp() throws Exception {
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
    seed = null;
  }
  protected void purgeFieldCache(final FieldCache fc) {
    fc.purgeAllCaches();
  }
  protected String getTestLabel() {
    return getClass().getName() + "." + getName();
  }
  @After
  public void tearDown() throws Exception {
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
  }
  protected void assertSaneFieldCaches(final String msg) {
    final CacheEntry[] entries = FieldCache.DEFAULT.getCacheEntries();
    Insanity[] insanity = null;
    try {
      try {
        insanity = FieldCacheSanityChecker.checkSanity(entries);
      } catch (RuntimeException e) {
        dumpArray(msg + ": FieldCache", entries, System.err);
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
  public static void dumpIterator(String label, Iterator<?> iter,
                                  PrintStream stream) {
    stream.println("*** BEGIN " + label + " ***");
    if (null == iter) {
      stream.println(" ... NULL ...");
    } else {
      while (iter.hasNext()) {
        stream.println(iter.next().toString());
      }
    }
    stream.println("*** END " + label + " ***");
  }
  public static void dumpArray(String label, Object[] objs,
                               PrintStream stream) {
    Iterator<?> iter = (null == objs) ? null : Arrays.asList(objs).iterator();
    dumpIterator(label, iter, stream);
  }
  public Random newRandom() {
    if (seed != null) {
      throw new IllegalStateException("please call LuceneTestCaseJ4.newRandom only once per test");
    }
    return newRandom(seedRnd.nextLong());
  }
  public Random newRandom(long seed) {
    if (this.seed != null) {
      throw new IllegalStateException("please call LuceneTestCaseJ4.newRandom only once per test");
    }
    this.seed = Long.valueOf(seed);
    return new Random(seed);
  }
  public String getName() {
    return this.name;
  }
  protected File getDataFile(String name) throws IOException {
    try {
      return new File(this.getClass().getResource(name).toURI());
    } catch (Exception e) {
      throw new IOException("Cannot find resource: " + name);
    }
  }
  public void reportAdditionalFailureInfo() {
    if (seed != null) {
      System.out.println("NOTE: random seed of testcase '" + getName() + "' was: " + seed);
    }
  }
  protected Long seed = null;
  private static final Random seedRnd = new Random();
  private String name = "<unknown>";
}
