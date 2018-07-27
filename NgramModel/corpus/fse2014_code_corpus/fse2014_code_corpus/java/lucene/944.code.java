package org.apache.lucene.benchmark.byTask;
import java.io.StringReader;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.feeds.ReutersQueryMaker;
import org.apache.lucene.benchmark.byTask.tasks.CountingSearchTestTask;
import org.apache.lucene.benchmark.byTask.tasks.CountingHighlighterTestTask;
import org.apache.lucene.benchmark.byTask.stats.TaskStats;
import org.apache.lucene.collation.CollationKeyAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.search.FieldCache.StringIndex;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.util.LuceneTestCase;
public class TestPerfTasksLogic extends LuceneTestCase {
  static final String NEW_LINE = System.getProperty("line.separator");
  static final String propLines [] = {
    "directory=RAMDirectory",
    "print.props=false",
  };
  public TestPerfTasksLogic(String name) {
    super(name);
  }
  public void testIndexAndSearchTasks() throws Exception {
    String algLines[] = {
        "ResetSystemErase",
        "CreateIndex",
        "{ AddDoc } : 1000",
        "Optimize",
        "CloseIndex",
        "OpenReader",
        "{ CountingSearchTest } : 200",
        "CloseReader",
        "[ CountingSearchTest > : 70",
        "[ CountingSearchTest > : 9",
    };
    CountingSearchTestTask.numSearches = 0;
    Benchmark benchmark = execBenchmark(algLines);
    assertEquals("TestSearchTask was supposed to be called!",279,CountingSearchTestTask.numSearches);
    assertTrue("Index does not exist?...!", IndexReader.indexExists(benchmark.getRunData().getDirectory()));
    IndexWriter iw = new IndexWriter(benchmark.getRunData().getDirectory(),
        new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
            .setOpenMode(OpenMode.APPEND));
    iw.close();
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    assertEquals("1000 docs were added to the index, this is what we expect to find!",1000,ir.numDocs());
    ir.close();
  }
  public void testTimedSearchTask() throws Exception {
    String algLines[] = {
        "log.step=100000",
        "ResetSystemErase",
        "CreateIndex",
        "{ AddDoc } : 100",
        "Optimize",
        "CloseIndex",
        "OpenReader",
        "{ CountingSearchTest } : .5s",
        "CloseReader",
    };
    CountingSearchTestTask.numSearches = 0;
    execBenchmark(algLines);
    assertTrue(CountingSearchTestTask.numSearches > 0);
    long elapsed = CountingSearchTestTask.prevLastMillis - CountingSearchTestTask.startMillis;
    assertTrue("elapsed time was " + elapsed + " msec", elapsed <= 1500);
  }
  public void testBGSearchTaskThreads() throws Exception {
    String algLines[] = {
        "log.time.step.msec = 100",
        "log.step=100000",
        "ResetSystemErase",
        "CreateIndex",
        "{ AddDoc } : 1000",
        "Optimize",
        "CloseIndex",
        "OpenReader",
        "{",
        "  [ \"XSearch\" { CountingSearchTest > : * ] : 2 &-1",
        "  Wait(0.5)",
        "}",
        "CloseReader",
        "RepSumByPref X"
    };
    CountingSearchTestTask.numSearches = 0;
    execBenchmark(algLines);
    assertTrue(CountingSearchTestTask.numSearches > 0);
  }
  public void testHighlighting() throws Exception {
    String algLines[] = {
        "doc.stored=true",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "query.maker=" + ReutersQueryMaker.class.getName(),
        "ResetSystemErase",
        "CreateIndex",
        "{ AddDoc } : 100",
        "Optimize",
        "CloseIndex",
        "OpenReader(true)",
        "{ CountingHighlighterTest(size[1],highlight[1],mergeContiguous[true],maxFrags[1],fields[body]) } : 200",
        "CloseReader",
    };
    CountingHighlighterTestTask.numHighlightedResults = 0;
    CountingHighlighterTestTask.numDocsRetrieved = 0;
    Benchmark benchmark = execBenchmark(algLines);
    assertEquals("TestSearchTask was supposed to be called!",92,CountingHighlighterTestTask.numDocsRetrieved);
    assertTrue("TestSearchTask was supposed to be called!", CountingHighlighterTestTask.numDocsRetrieved >= CountingHighlighterTestTask.numHighlightedResults && CountingHighlighterTestTask.numHighlightedResults > 0);
    assertTrue("Index does not exist?...!", IndexReader.indexExists(benchmark.getRunData().getDirectory()));
    IndexWriter iw = new IndexWriter(benchmark.getRunData().getDirectory(), new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    iw.close();
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    assertEquals("100 docs were added to the index, this is what we expect to find!",100,ir.numDocs());
    ir.close();
  }
  public void testHighlightingTV() throws Exception {
    String algLines[] = {
        "doc.stored=true",
        "doc.term.vector.offsets=true",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "query.maker=" + ReutersQueryMaker.class.getName(),
        "ResetSystemErase",
        "CreateIndex",
        "{ AddDoc } : 1000",
        "Optimize",
        "CloseIndex",
        "OpenReader(false)",
        "{ CountingHighlighterTest(size[1],highlight[1],mergeContiguous[true],maxFrags[1],fields[body]) } : 200",
        "CloseReader",
    };
    CountingHighlighterTestTask.numHighlightedResults = 0;
    CountingHighlighterTestTask.numDocsRetrieved = 0;
    Benchmark benchmark = execBenchmark(algLines);
    assertEquals("TestSearchTask was supposed to be called!",92,CountingHighlighterTestTask.numDocsRetrieved);
    assertTrue("TestSearchTask was supposed to be called!", CountingHighlighterTestTask.numDocsRetrieved >= CountingHighlighterTestTask.numHighlightedResults && CountingHighlighterTestTask.numHighlightedResults > 0);
    assertTrue("Index does not exist?...!", IndexReader.indexExists(benchmark.getRunData().getDirectory()));
    IndexWriter iw = new IndexWriter(benchmark.getRunData().getDirectory(), new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    iw.close();
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    assertEquals("1000 docs were added to the index, this is what we expect to find!",1000,ir.numDocs());
    ir.close();
  }
  public void testHighlightingNoTvNoStore() throws Exception {
    String algLines[] = {
        "doc.stored=false",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "query.maker=" + ReutersQueryMaker.class.getName(),
        "ResetSystemErase",
        "CreateIndex",
        "{ AddDoc } : 1000",
        "Optimize",
        "CloseIndex",
        "OpenReader",
        "{ CountingHighlighterTest(size[1],highlight[1],mergeContiguous[true],maxFrags[1],fields[body]) } : 200",
        "CloseReader",
    };
    CountingHighlighterTestTask.numHighlightedResults = 0;
    CountingHighlighterTestTask.numDocsRetrieved = 0;
    try {
      Benchmark benchmark = execBenchmark(algLines);
      assertTrue("CountingHighlighterTest should have thrown an exception", false);
      assertNotNull(benchmark); 
    } catch (Exception e) {
      assertTrue(true);
    }
  }
  public void testExhaustContentSource() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.SingleDocSource",
        "content.source.log.step=1",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "doc.stored=false",
        "doc.tokenized=false",
        "# ----- alg ",
        "CreateIndex",
        "{ AddDoc } : * ",
        "Optimize",
        "CloseIndex",
        "OpenReader",
        "{ CountingSearchTest } : 100",
        "CloseReader",
        "[ CountingSearchTest > : 30",
        "[ CountingSearchTest > : 9",
    };
    CountingSearchTestTask.numSearches = 0;
    Benchmark benchmark = execBenchmark(algLines);
    assertEquals("TestSearchTask was supposed to be called!",139,CountingSearchTestTask.numSearches);
    assertTrue("Index does not exist?...!", IndexReader.indexExists(benchmark.getRunData().getDirectory()));
    IndexWriter iw = new IndexWriter(benchmark.getRunData().getDirectory(), new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    iw.close();
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    assertEquals("1 docs were added to the index, this is what we expect to find!",1,ir.numDocs());
    ir.close();
  }
  public void testDocMakerThreadSafety() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.SortableSingleDocSource",
        "doc.term.vector=false",
        "log.step.AddDoc=10000",
        "content.source.forever=true",
        "directory=RAMDirectory",
        "doc.reuse.fields=false",
        "doc.stored=false",
        "doc.tokenized=false",
        "doc.index.props=true",
        "# ----- alg ",
        "CreateIndex",
        "[ { AddDoc > : 250 ] : 4",
        "CloseIndex",
    };
    CountingSearchTestTask.numSearches = 0;
    Benchmark benchmark = execBenchmark(algLines);
    IndexReader r = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    StringIndex idx = FieldCache.DEFAULT.getStringIndex(r, "country");
    final int maxDoc = r.maxDoc();
    assertEquals(1000, maxDoc);
    for(int i=0;i<1000;i++) {
      assertNotNull("doc " + i + " has null country", idx.lookup[idx.order[i]]);
    }
    r.close();
  }
  public void testParallelDocMaker() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=FSDirectory",
        "doc.stored=false",
        "doc.tokenized=false",
        "# ----- alg ",
        "CreateIndex",
        "[ { AddDoc } : * ] : 4 ",
        "CloseIndex",
    };
    Benchmark benchmark = execBenchmark(algLines);
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    int ndocsExpected = 20; 
    assertEquals("wrong number of docs in the index!", ndocsExpected, ir.numDocs());
    ir.close();
  }
  public void testLineDocFile() throws Exception {
    File lineFile = new File(TEMP_DIR, "test.reuters.lines.txt");
    final int NUM_TRY_DOCS = 50;
    String algLines1[] = {
      "# ----- properties ",
      "content.source=org.apache.lucene.benchmark.byTask.feeds.SingleDocSource",
      "content.source.forever=true",
      "line.file.out=" + lineFile.getAbsolutePath().replace('\\', '/'),
      "# ----- alg ",
      "{WriteLineDoc()}:" + NUM_TRY_DOCS,
    };
    Benchmark benchmark = execBenchmark(algLines1);
    BufferedReader r = new BufferedReader(new FileReader(lineFile));
    int numLines = 0;
    while(r.readLine() != null)
      numLines++;
    r.close();
    assertEquals("did not see the right number of docs; should be " + NUM_TRY_DOCS + " but was " + numLines, NUM_TRY_DOCS, numLines);
    String algLines2[] = {
      "# ----- properties ",
      "analyzer=org.apache.lucene.analysis.SimpleAnalyzer",
      "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
      "docs.file=" + lineFile.getAbsolutePath().replace('\\', '/'),
      "content.source.forever=false",
      "doc.reuse.fields=false",
      "ram.flush.mb=4",
      "# ----- alg ",
      "ResetSystemErase",
      "CreateIndex",
      "{AddDoc}: *",
      "CloseIndex",
    };
    benchmark = execBenchmark(algLines2);
    IndexWriter iw = new IndexWriter(benchmark.getRunData().getDirectory(),
        new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
            .setOpenMode(OpenMode.APPEND));
    iw.close();
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    assertEquals(numLines + " lines were created but " + ir.numDocs() + " docs are in the index", numLines, ir.numDocs());
    ir.close();
    lineFile.delete();
  }
  public void testReadTokens() throws Exception {
    final int NUM_DOCS = 20;
    String algLines1[] = {
      "# ----- properties ",
      "analyzer=org.apache.lucene.analysis.WhitespaceAnalyzer",
      "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
      "docs.file=" + getReuters20LinesFile(),
      "# ----- alg ",
      "{ReadTokens}: " + NUM_DOCS,
      "ResetSystemErase",
      "CreateIndex",
      "{AddDoc}: " + NUM_DOCS,
      "CloseIndex",
    };
    Benchmark benchmark = execBenchmark(algLines1);
    List<TaskStats> stats = benchmark.getRunData().getPoints().taskStats();
    int totalTokenCount1 = 0;
    for (final TaskStats stat : stats) {
      if (stat.getTask().getName().equals("ReadTokens")) {
        totalTokenCount1 += stat.getCount();
      }
    }
    IndexReader reader = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    assertEquals(NUM_DOCS, reader.numDocs());
    TermEnum terms = reader.terms();
    TermDocs termDocs = reader.termDocs();
    int totalTokenCount2 = 0;
    while(terms.next()) {
      Term term = terms.term();
      if (term != null && term.field() != DocMaker.ID_FIELD) { 
        termDocs.seek(terms.term());
        while (termDocs.next())
          totalTokenCount2 += termDocs.freq();
      }
    }
    reader.close();
    assertEquals(totalTokenCount1, totalTokenCount2);
  }
  public void testParallelExhausted() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "doc.stored=false",
        "doc.tokenized=false",
        "task.max.depth.log=1",
        "# ----- alg ",
        "CreateIndex",
        "{ [ AddDoc]: 4} : * ",
        "ResetInputs ",
        "{ [ AddDoc]: 4} : * ",
        "CloseIndex",
    };
    Benchmark benchmark = execBenchmark(algLines);
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    int ndocsExpected = 2 * 20; 
    assertEquals("wrong number of docs in the index!", ndocsExpected, ir.numDocs());
    ir.close();
  }
  public static Benchmark execBenchmark(String[] algLines) throws Exception {
    String algText = algLinesToText(algLines);
    logTstLogic(algText);
    Benchmark benchmark = new Benchmark(new StringReader(algText));
    benchmark.execute();
    return benchmark;
  }
  private static String algLinesToText(String[] algLines) {
    String indent = "  ";
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < propLines.length; i++) {
      sb.append(indent).append(propLines[i]).append(NEW_LINE);
    }
    for (int i = 0; i < algLines.length; i++) {
      sb.append(indent).append(algLines[i]).append(NEW_LINE);
    }
    return sb.toString();
  }
  private static void logTstLogic (String txt) {
    if (!VERBOSE) 
      return;
    System.out.println("Test logic of:");
    System.out.println(txt);
  }
  public void testExhaustedLooped() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "doc.stored=false",
        "doc.tokenized=false",
        "task.max.depth.log=1",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  CreateIndex",
        "  { \"AddDocs\"  AddDoc > : * ",
        "  CloseIndex",
        "} : 2",
    };
    Benchmark benchmark = execBenchmark(algLines);
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    int ndocsExpected = 20;  
    assertEquals("wrong number of docs in the index!", ndocsExpected, ir.numDocs());
    ir.close();
  }
  public void testCloseIndexFalse() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "ram.flush.mb=-1",
        "max.buffered=2",
        "content.source.log.step=3",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "doc.stored=false",
        "doc.tokenized=false",
        "debug.level=1",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  CreateIndex",
        "  { \"AddDocs\"  AddDoc > : * ",
        "  CloseIndex(false)",
        "} : 2",
    };
    Benchmark benchmark = execBenchmark(algLines);
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    int ndocsExpected = 20; 
    assertEquals("wrong number of docs in the index!", ndocsExpected, ir.numDocs());
    ir.close();
  }
  public static class MyMergeScheduler extends SerialMergeScheduler {
    boolean called;
    public MyMergeScheduler() {
      super();
      called = true;
    }
  }
  public void testMergeScheduler() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "merge.scheduler=" + MyMergeScheduler.class.getName(),
        "doc.stored=false",
        "doc.tokenized=false",
        "debug.level=1",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  CreateIndex",
        "  { \"AddDocs\"  AddDoc > : * ",
        "} : 2",
    };
    Benchmark benchmark = execBenchmark(algLines);
    assertTrue("did not use the specified MergeScheduler",
        ((MyMergeScheduler) benchmark.getRunData().getIndexWriter().getConfig()
            .getMergeScheduler()).called);
    benchmark.getRunData().getIndexWriter().close();
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    int ndocsExpected = 20; 
    assertEquals("wrong number of docs in the index!", ndocsExpected, ir.numDocs());
    ir.close();
  }
  public static class MyMergePolicy extends LogDocMergePolicy {
    boolean called;
    public MyMergePolicy(IndexWriter writer) {
      super(writer);
      called = true;
    }
  }
  public void testMergePolicy() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "ram.flush.mb=-1",
        "max.buffered=2",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "merge.policy=" + MyMergePolicy.class.getName(),
        "doc.stored=false",
        "doc.tokenized=false",
        "debug.level=1",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  CreateIndex",
        "  { \"AddDocs\"  AddDoc > : * ",
        "} : 2",
    };
    Benchmark benchmark = execBenchmark(algLines);
    assertTrue("did not use the specified MergeScheduler", ((MyMergePolicy) benchmark.getRunData().getIndexWriter().getMergePolicy()).called);
    benchmark.getRunData().getIndexWriter().close();
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    int ndocsExpected = 20; 
    assertEquals("wrong number of docs in the index!", ndocsExpected, ir.numDocs());
    ir.close();
  }
  public void testIndexWriterSettings() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "ram.flush.mb=-1",
        "max.buffered=2",
        "compound=cmpnd:true:false",
        "doc.term.vector=vector:false:true",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "doc.stored=false",
        "merge.factor=3",
        "doc.tokenized=false",
        "debug.level=1",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  CreateIndex",
        "  { \"AddDocs\"  AddDoc > : * ",
        "  NewRound",
        "} : 2",
    };
    Benchmark benchmark = execBenchmark(algLines);
    final IndexWriter writer = benchmark.getRunData().getIndexWriter();
    assertEquals(2, writer.getConfig().getMaxBufferedDocs());
    assertEquals(IndexWriterConfig.DISABLE_AUTO_FLUSH, (int) writer.getConfig().getRAMBufferSizeMB());
    assertEquals(3, ((LogMergePolicy) writer.getMergePolicy()).getMergeFactor());
    assertFalse(((LogMergePolicy) writer.getMergePolicy()).getUseCompoundFile());
    writer.close();
    Directory dir = benchmark.getRunData().getDirectory();
    IndexReader reader = IndexReader.open(dir, true);
    TermFreqVector [] tfv = reader.getTermFreqVectors(0);
    assertNotNull(tfv);
    assertTrue(tfv.length > 0);
    reader.close();
  }
  public void testOptimizeMaxNumSegments() throws Exception {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "ram.flush.mb=-1",
        "max.buffered=3",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "merge.policy=org.apache.lucene.index.LogDocMergePolicy",
        "doc.stored=false",
        "doc.tokenized=false",
        "debug.level=1",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  CreateIndex",
        "  { \"AddDocs\"  AddDoc > : * ",
        "  Optimize(3)",
        "  CloseIndex()",
        "} : 2",
    };
    Benchmark benchmark = execBenchmark(algLines);
    IndexReader ir = IndexReader.open(benchmark.getRunData().getDirectory(), true);
    int ndocsExpected = 20; 
    assertEquals("wrong number of docs in the index!", ndocsExpected, ir.numDocs());
    ir.close();
    final String[] files = benchmark.getRunData().getDirectory().listAll();
    int cfsCount = 0;
    for(int i=0;i<files.length;i++)
      if (files[i].endsWith(".cfs"))
        cfsCount++;
    assertEquals(3, cfsCount);
  }
  public void testDisableCounting() throws Exception {
    doTestDisableCounting(true);
    doTestDisableCounting(false);
  }
  private void doTestDisableCounting(boolean disable) throws Exception {
    String algLines[] = disableCountingLines(disable);
    Benchmark benchmark = execBenchmark(algLines);
    int n = disable ? 0 : 1;
    int nChecked = 0;
    for (final TaskStats stats : benchmark.getRunData().getPoints().taskStats()) {
      String taskName = stats.getTask().getName();
      if (taskName.equals("Rounds")) {
        assertEquals("Wrong total count!",20+2*n,stats.getCount());
        nChecked++;
      } else if (taskName.equals("CreateIndex")) {
        assertEquals("Wrong count for CreateIndex!",n,stats.getCount());
        nChecked++;
      } else if (taskName.equals("CloseIndex")) {
        assertEquals("Wrong count for CloseIndex!",n,stats.getCount());
        nChecked++;
      }
    }
    assertEquals("Missing some tasks to check!",3,nChecked);
  }
  private static String[] disableCountingLines (boolean disable) {
    String dis = disable ? "-" : "";
    return new String[] {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=30",
        "doc.term.vector=false",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "doc.stored=false",
        "doc.tokenized=false",
        "task.max.depth.log=1",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  "+dis+"CreateIndex",            
        "  { \"AddDocs\"  AddDoc > : * ",
        "  "+dis+"  CloseIndex",             
        "}",
        "RepSumByName",
    };
  }
  public void testLocale() throws Exception {
    Benchmark benchmark = execBenchmark(getLocaleConfig(""));
    assertNull(benchmark.getRunData().getLocale());
    benchmark = execBenchmark(getLocaleConfig("ROOT"));
    assertEquals(new Locale(""), benchmark.getRunData().getLocale());
    benchmark = execBenchmark(getLocaleConfig("de"));
    assertEquals(new Locale("de"), benchmark.getRunData().getLocale());
    benchmark = execBenchmark(getLocaleConfig("en,US"));
    assertEquals(new Locale("en", "US"), benchmark.getRunData().getLocale());
    benchmark = execBenchmark(getLocaleConfig("no,NO,NY"));
    assertEquals(new Locale("no", "NO", "NY"), benchmark.getRunData().getLocale());
  }
  private static String[] getLocaleConfig(String localeParam) {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  NewLocale(" + localeParam + ")",
        "  CreateIndex",
        "  { \"AddDocs\"  AddDoc > : * ",
        "  NewRound",
        "} : 1",
    };
    return algLines;
  }
  public void testCollator() throws Exception {
    Benchmark benchmark = execBenchmark(getCollatorConfig("ROOT", "impl:jdk"));
    CollationKeyAnalyzer expected = new CollationKeyAnalyzer(Collator
        .getInstance(new Locale("")));
    assertEqualCollation(expected, benchmark.getRunData().getAnalyzer(), "foobar");
    benchmark = execBenchmark(getCollatorConfig("de", "impl:jdk"));
    expected = new CollationKeyAnalyzer(Collator.getInstance(new Locale("de")));
    assertEqualCollation(expected, benchmark.getRunData().getAnalyzer(), "foobar");
    benchmark = execBenchmark(getCollatorConfig("en,US", "impl:jdk"));
    expected = new CollationKeyAnalyzer(Collator.getInstance(new Locale("en",
        "US")));
    assertEqualCollation(expected, benchmark.getRunData().getAnalyzer(), "foobar");
    benchmark = execBenchmark(getCollatorConfig("no,NO,NY", "impl:jdk"));
    expected = new CollationKeyAnalyzer(Collator.getInstance(new Locale("no",
        "NO", "NY")));
    assertEqualCollation(expected, benchmark.getRunData().getAnalyzer(), "foobar");
  }
  private void assertEqualCollation(Analyzer a1, Analyzer a2, String text)
      throws Exception {
    TokenStream ts1 = a1.tokenStream("bogus", new StringReader(text));
    TokenStream ts2 = a2.tokenStream("bogus", new StringReader(text));
    ts1.reset();
    ts2.reset();
    TermAttribute termAtt1 = ts1.addAttribute(TermAttribute.class);
    TermAttribute termAtt2 = ts2.addAttribute(TermAttribute.class);
    assertTrue(ts1.incrementToken());
    assertTrue(ts2.incrementToken());
    assertEquals(termAtt1.term(), termAtt2.term());
    assertFalse(ts1.incrementToken());
    assertFalse(ts2.incrementToken());
    ts1.close();
    ts2.close();
  }
  private static String[] getCollatorConfig(String localeParam, 
      String collationParam) {
    String algLines[] = {
        "# ----- properties ",
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.log.step=3",
        "content.source.forever=false",
        "directory=RAMDirectory",
        "# ----- alg ",
        "{ \"Rounds\"",
        "  ResetSystemErase",
        "  NewLocale(" + localeParam + ")",
        "  NewCollationAnalyzer(" + collationParam + ")",
        "  CreateIndex",
        "  { \"AddDocs\"  AddDoc > : * ",
        "  NewRound",
        "} : 1",
    };
    return algLines;
  }
  public void testShingleAnalyzer() throws Exception {
    String text = "one,two,three, four five six";
    Benchmark benchmark = execBenchmark(getShingleConfig(""));
    benchmark.getRunData().getAnalyzer().tokenStream
      ("bogus", new StringReader(text)).close();
    assertEqualShingle(benchmark.getRunData().getAnalyzer(), text,
                       new String[] {"one", "one two", "two", "two three",
                                     "three", "three four", "four", "four five",
                                     "five", "five six", "six"});
    benchmark = execBenchmark
      (getShingleConfig("maxShingleSize:3,outputUnigrams:false"));
    assertEqualShingle(benchmark.getRunData().getAnalyzer(), text,
                       new String[] { "one two", "one two three", "two three",
                                      "two three four", "three four", 
                                      "three four five", "four five",
                                      "four five six", "five six" });
    benchmark = execBenchmark
      (getShingleConfig("analyzer:WhitespaceAnalyzer"));
    assertEqualShingle(benchmark.getRunData().getAnalyzer(), text,
                       new String[] { "one,two,three,", "one,two,three, four",
                                      "four", "four five", "five", "five six", 
                                      "six" });
    benchmark = execBenchmark
      (getShingleConfig
        ("outputUnigrams:false,maxShingleSize:3,analyzer:WhitespaceAnalyzer"));
    assertEqualShingle(benchmark.getRunData().getAnalyzer(), text,
                       new String[] { "one,two,three, four", 
                                      "one,two,three, four five",
                                      "four five", "four five six",
                                      "five six" });
  }
  private void assertEqualShingle
    (Analyzer analyzer, String text, String[] expected) throws Exception {
    TokenStream stream = analyzer.tokenStream("bogus", new StringReader(text));
    stream.reset();
    TermAttribute termAtt = stream.addAttribute(TermAttribute.class);
    int termNum = 0;
    while (stream.incrementToken()) {
      assertTrue("Extra output term(s), starting with '"
                 + new String(termAtt.termBuffer(), 0, termAtt.termLength()) + "'",
                 termNum < expected.length);
      assertEquals("Mismatch in output term # " + termNum + " - ", 
                   expected[termNum],
                   new String(termAtt.termBuffer(), 0, termAtt.termLength()));
      ++termNum;
    }
    assertEquals("Too few output terms", expected.length, termNum);
    stream.close();
  }
  private static String[] getShingleConfig(String params) { 
    String algLines[] = {
        "content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource",
        "docs.file=" + getReuters20LinesFile(),
        "content.source.forever=false",
        "directory=RAMDirectory",
        "NewShingleAnalyzer(" + params + ")",
        "CreateIndex",
        "{ \"AddDocs\"  AddDoc > : * "
    };
    return algLines;
  }
  private static String getReuters20LinesFile() {
    return System.getProperty("lucene.common.dir").replace('\\','/') +
      "/contrib/benchmark/src/test/org/apache/lucene/benchmark/reuters.first20.lines.txt";
  }  
}
