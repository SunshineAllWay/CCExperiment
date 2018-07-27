package org.apache.lucene.index;
import org.apache.lucene.util.*;
import org.apache.lucene.store.*;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.*;
import java.util.Random;
import java.io.File;
public class TestStressIndexing extends LuceneTestCase {
  private Random RANDOM;
  private static abstract class TimedThread extends Thread {
    volatile boolean failed;
    int count;
    private static int RUN_TIME_SEC = 1;
    private TimedThread[] allThreads;
    abstract public void doWork() throws Throwable;
    TimedThread(TimedThread[] threads) {
      this.allThreads = threads;
    }
    @Override
    public void run() {
      final long stopTime = System.currentTimeMillis() + 1000*RUN_TIME_SEC;
      count = 0;
      try {
        do {
          if (anyErrors()) break;
          doWork();
          count++;
        } while(System.currentTimeMillis() < stopTime);
      } catch (Throwable e) {
        System.out.println(Thread.currentThread() + ": exc");
        e.printStackTrace(System.out);
        failed = true;
      }
    }
    private boolean anyErrors() {
      for(int i=0;i<allThreads.length;i++)
        if (allThreads[i] != null && allThreads[i].failed)
          return true;
      return false;
    }
  }
  private class IndexerThread extends TimedThread {
    IndexWriter writer;
    int nextID;
    public IndexerThread(IndexWriter writer, TimedThread[] threads) {
      super(threads);
      this.writer = writer;
    }
    @Override
    public void doWork() throws Exception {
      for(int j=0; j<10; j++) {
        Document d = new Document();
        int n = RANDOM.nextInt();
        d.add(new Field("id", Integer.toString(nextID++), Field.Store.YES, Field.Index.NOT_ANALYZED));
        d.add(new Field("contents", English.intToEnglish(n), Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(d);
      }
      int deleteID = nextID-1;
      for(int j=0; j<5; j++) {
        writer.deleteDocuments(new Term("id", ""+deleteID));
        deleteID -= 2;
      }
    }
  }
  private static class SearcherThread extends TimedThread {
    private Directory directory;
    public SearcherThread(Directory directory, TimedThread[] threads) {
      super(threads);
      this.directory = directory;
    }
    @Override
    public void doWork() throws Throwable {
      for (int i=0; i<100; i++)
        (new IndexSearcher(directory, true)).close();
      count += 100;
    }
  }
  public void runStressTest(Directory directory, MergeScheduler mergeScheduler) throws Exception {
    IndexWriter modifier = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.CREATE).setMaxBufferedDocs(10).setMergeScheduler(
            mergeScheduler));
    TimedThread[] threads = new TimedThread[4];
    int numThread = 0;
    IndexerThread indexerThread = new IndexerThread(modifier, threads);
    threads[numThread++] = indexerThread;
    indexerThread.start();
    IndexerThread indexerThread2 = new IndexerThread(modifier, threads);
    threads[numThread++] = indexerThread2;
    indexerThread2.start();
    SearcherThread searcherThread1 = new SearcherThread(directory, threads);
    threads[numThread++] = searcherThread1;
    searcherThread1.start();
    SearcherThread searcherThread2 = new SearcherThread(directory, threads);
    threads[numThread++] = searcherThread2;
    searcherThread2.start();
    for(int i=0;i<numThread;i++)
      threads[i].join();
    modifier.close();
    for(int i=0;i<numThread;i++)
      assertTrue(! threads[i].failed);
  }
  public void testStressIndexAndSearching() throws Exception {
    RANDOM = newRandom();
    Directory directory = new MockRAMDirectory();
    runStressTest(directory, new ConcurrentMergeScheduler());
    directory.close();
    File dirPath = _TestUtil.getTempDir("lucene.test.stress");
    directory = FSDirectory.open(dirPath);
    runStressTest(directory, new ConcurrentMergeScheduler());
    directory.close();
    _TestUtil.rmDir(dirPath);
  }
}
