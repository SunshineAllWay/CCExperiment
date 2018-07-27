package org.apache.lucene.search.spell;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.English;
import org.apache.lucene.util.LuceneTestCase;
public class TestSpellChecker extends LuceneTestCase {
  private SpellCheckerMock spellChecker;
  private Directory userindex, spellindex;
  private final Random random = newRandom();
  private List<IndexSearcher> searchers;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    userindex = new RAMDirectory();
    IndexWriter writer = new IndexWriter(userindex, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < 1000; i++) {
      Document doc = new Document();
      doc.add(new Field("field1", English.intToEnglish(i), Field.Store.YES, Field.Index.ANALYZED));
      doc.add(new Field("field2", English.intToEnglish(i + 1), Field.Store.YES, Field.Index.ANALYZED)); 
      writer.addDocument(doc);
    }
    writer.close();
    searchers = Collections.synchronizedList(new ArrayList<IndexSearcher>());
    spellindex = new RAMDirectory();
    spellChecker = new SpellCheckerMock(spellindex);
  }
  public void testBuild() throws CorruptIndexException, IOException {
    IndexReader r = IndexReader.open(userindex, true);
    spellChecker.clearIndex();
    addwords(r, "field1");
    int num_field1 = this.numdoc();
    addwords(r, "field2");
    int num_field2 = this.numdoc();
    assertEquals(num_field2, num_field1 + 1);
    assertLastSearcherOpen(4);
    checkCommonSuggestions(r);
    checkLevenshteinSuggestions(r);
    spellChecker.setStringDistance(new JaroWinklerDistance());
    spellChecker.setAccuracy(0.8f);
    checkCommonSuggestions(r);
    checkJaroWinklerSuggestions();
    spellChecker.setStringDistance(new NGramDistance(2));
    spellChecker.setAccuracy(0.5f);
    checkCommonSuggestions(r);
    checkNGramSuggestions();
  }
  private void checkCommonSuggestions(IndexReader r) throws IOException {
    String[] similar = spellChecker.suggestSimilar("fvie", 2);
    assertTrue(similar.length > 0);
    assertEquals(similar[0], "five");
    similar = spellChecker.suggestSimilar("five", 2);
    if (similar.length > 0) {
      assertFalse(similar[0].equals("five")); 
    }
    similar = spellChecker.suggestSimilar("fiv", 2);
    assertTrue(similar.length > 0);
    assertEquals(similar[0], "five");
    similar = spellChecker.suggestSimilar("fives", 2);
    assertTrue(similar.length > 0);
    assertEquals(similar[0], "five");
    assertTrue(similar.length > 0);
    similar = spellChecker.suggestSimilar("fie", 2);
    assertEquals(similar[0], "five");
    similar = spellChecker.suggestSimilar("tousand", 10, r, "field1", false);
    assertEquals(0, similar.length); 
    similar = spellChecker.suggestSimilar("tousand", 10, r, "field2", false);
    assertEquals(1, similar.length); 
  }
  private void checkLevenshteinSuggestions(IndexReader r) throws IOException {
    String[] similar = spellChecker.suggestSimilar("fvie", 2);
    assertEquals(1, similar.length);
    assertEquals(similar[0], "five");
    similar = spellChecker.suggestSimilar("five", 2);
    assertEquals(1, similar.length);
    assertEquals(similar[0], "nine");     
    similar = spellChecker.suggestSimilar("fiv", 2);
    assertEquals(1, similar.length);
    assertEquals(similar[0], "five");
    similar = spellChecker.suggestSimilar("ive", 2);
    assertEquals(2, similar.length);
    assertEquals(similar[0], "five");
    assertEquals(similar[1], "nine");
    similar = spellChecker.suggestSimilar("fives", 2);
    assertEquals(1, similar.length);
    assertEquals(similar[0], "five");
    similar = spellChecker.suggestSimilar("fie", 2);
    assertEquals(2, similar.length);
    assertEquals(similar[0], "five");
    assertEquals(similar[1], "nine");
    similar = spellChecker.suggestSimilar("fi", 2);
    assertEquals(1, similar.length);
    assertEquals(similar[0], "five");
    similar = spellChecker.suggestSimilar("tousand", 10, r, "field1", false);
    assertEquals(0, similar.length); 
    similar = spellChecker.suggestSimilar("tousand", 10, r, "field2", false);
    assertEquals(1, similar.length); 
    similar = spellChecker.suggestSimilar("onety", 2);
    assertEquals(2, similar.length);
    assertEquals(similar[0], "ninety");
    assertEquals(similar[1], "one");
    try {
      similar = spellChecker.suggestSimilar("tousand", 10, r, null, false);
    } catch (NullPointerException e) {
      assertTrue("threw an NPE, and it shouldn't have", false);
    }
  }
  private void checkJaroWinklerSuggestions() throws IOException {
    String[] similar = spellChecker.suggestSimilar("onety", 2);
    assertEquals(2, similar.length);
    assertEquals(similar[0], "one");
    assertEquals(similar[1], "ninety");
  }
  private void checkNGramSuggestions() throws IOException {
    String[] similar = spellChecker.suggestSimilar("onety", 2);
    assertEquals(2, similar.length);
    assertEquals(similar[0], "one");
    assertEquals(similar[1], "ninety");
  }
  private void addwords(IndexReader r, String field) throws IOException {
    long time = System.currentTimeMillis();
    spellChecker.indexDictionary(new LuceneDictionary(r, field));
    time = System.currentTimeMillis() - time;
  }
  private int numdoc() throws IOException {
    IndexReader rs = IndexReader.open(spellindex, true);
    int num = rs.numDocs();
    assertTrue(num != 0);
    rs.close();
    return num;
  }
  public void testClose() throws IOException {
    IndexReader r = IndexReader.open(userindex, true);
    spellChecker.clearIndex();
    String field = "field1";
    addwords(r, "field1");
    int num_field1 = this.numdoc();
    addwords(r, "field2");
    int num_field2 = this.numdoc();
    assertEquals(num_field2, num_field1 + 1);
    checkCommonSuggestions(r);
    assertLastSearcherOpen(4);
    spellChecker.close();
    assertSearchersClosed();
    try {
      spellChecker.close();
      fail("spellchecker was already closed");
    } catch (AlreadyClosedException e) {
    }
    try {
      checkCommonSuggestions(r);
      fail("spellchecker was already closed");
    } catch (AlreadyClosedException e) {
    }
    try {
      spellChecker.clearIndex();
      fail("spellchecker was already closed");
    } catch (AlreadyClosedException e) {
    }
    try {
      spellChecker.indexDictionary(new LuceneDictionary(r, field));
      fail("spellchecker was already closed");
    } catch (AlreadyClosedException e) {
    }
    try {
      spellChecker.setSpellIndex(spellindex);
      fail("spellchecker was already closed");
    } catch (AlreadyClosedException e) {
    }
    assertEquals(4, searchers.size());
    assertSearchersClosed();
  }
  public void testConcurrentAccess() throws IOException, InterruptedException {
    assertEquals(1, searchers.size());
    final IndexReader r = IndexReader.open(userindex, true);
    spellChecker.clearIndex();
    assertEquals(2, searchers.size());
    addwords(r, "field1");
    assertEquals(3, searchers.size());
    int num_field1 = this.numdoc();
    addwords(r, "field2");
    assertEquals(4, searchers.size());
    int num_field2 = this.numdoc();
    assertEquals(num_field2, num_field1 + 1);
    int numThreads = 5 + this.random.nextInt(5);
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    SpellCheckWorker[] workers = new SpellCheckWorker[numThreads];
    for (int i = 0; i < numThreads; i++) {
      SpellCheckWorker spellCheckWorker = new SpellCheckWorker(r);
      executor.execute(spellCheckWorker);
      workers[i] = spellCheckWorker;
    }
    int iterations = 5 + random.nextInt(5);
    for (int i = 0; i < iterations; i++) {
      Thread.sleep(100);
      spellChecker.setSpellIndex(this.spellindex);
    }
    spellChecker.close();
    executor.shutdown();
    executor.awaitTermination(60L, TimeUnit.SECONDS);
    for (int i = 0; i < workers.length; i++) {
      assertFalse(String.format("worker thread %d failed", i), workers[i].failed);
      assertTrue(String.format("worker thread %d is still running but should be terminated", i), workers[i].terminated);
    }
    assertEquals(iterations + 4, searchers.size());
    assertSearchersClosed();
  }
  private void assertLastSearcherOpen(int numSearchers) {
    assertEquals(numSearchers, searchers.size());
    IndexSearcher[] searcherArray = searchers.toArray(new IndexSearcher[0]);
    for (int i = 0; i < searcherArray.length; i++) {
      if (i == searcherArray.length - 1) {
        assertTrue("expected last searcher open but was closed",
            searcherArray[i].getIndexReader().getRefCount() > 0);
      } else {
        assertFalse("expected closed searcher but was open - Index: " + i,
            searcherArray[i].getIndexReader().getRefCount() > 0);
      }
    }
  }
  private void assertSearchersClosed() {
    for (IndexSearcher searcher : searchers) {
      assertEquals(0, searcher.getIndexReader().getRefCount());
    }
  }
  private class SpellCheckWorker implements Runnable {
    private final IndexReader reader;
    volatile boolean terminated = false;
    volatile boolean failed = false;
    SpellCheckWorker(IndexReader reader) {
      super();
      this.reader = reader;
    }
    public void run() {
      try {
        while (true) {
          try {
            checkCommonSuggestions(reader);
          } catch (AlreadyClosedException e) {
            return;
          } catch (Throwable e) {
            e.printStackTrace();
            failed = true;
            return;
          }
        }
      } finally {
        terminated = true;
      }
    }
  }
  class SpellCheckerMock extends SpellChecker {
    public SpellCheckerMock(Directory spellIndex) throws IOException {
      super(spellIndex);
    }
    public SpellCheckerMock(Directory spellIndex, StringDistance sd)
        throws IOException {
      super(spellIndex, sd);
    }
    @Override
    IndexSearcher createSearcher(Directory dir) throws IOException {
      IndexSearcher searcher = super.createSearcher(dir);
      TestSpellChecker.this.searchers.add(searcher);
      return searcher;
    }
  }
}
