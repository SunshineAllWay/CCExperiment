package org.apache.lucene.collation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.IndexableBinaryStringTools;
import org.apache.lucene.util.LuceneTestCase;
import java.io.IOException;
public class CollationTestBase extends LuceneTestCase {
  protected String firstRangeBeginningOriginal = "\u062F";
  protected String firstRangeEndOriginal = "\u0698";
  protected String secondRangeBeginningOriginal = "\u0633";
  protected String secondRangeEndOriginal = "\u0638";
  protected String encodeCollationKey(byte[] keyBits) {
    int encodedLength = IndexableBinaryStringTools.getEncodedLength(keyBits, 0, keyBits.length);
    char[] encodedBegArray = new char[encodedLength];
    IndexableBinaryStringTools.encode(keyBits, 0, keyBits.length, encodedBegArray, 0, encodedLength);
    return new String(encodedBegArray);
  }
  public void testFarsiRangeFilterCollating(Analyzer analyzer, String firstBeg, 
                                            String firstEnd, String secondBeg,
                                            String secondEnd) throws Exception {
    RAMDirectory ramDir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(ramDir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer));
    Document doc = new Document();
    doc.add(new Field("content", "\u0633\u0627\u0628", 
                      Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("body", "body",
                      Field.Store.YES, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
    writer.close();
    IndexSearcher searcher = new IndexSearcher(ramDir, true);
    Query query = new TermQuery(new Term("body","body"));
    ScoreDoc[] result = searcher.search
      (query, new TermRangeFilter("content", firstBeg, firstEnd, true, true), 1).scoreDocs;
    assertEquals("The index Term should not be included.", 0, result.length);
    result = searcher.search
      (query, new TermRangeFilter("content", secondBeg, secondEnd, true, true), 1).scoreDocs;
    assertEquals("The index Term should be included.", 1, result.length);
    searcher.close();
  }
  public void testFarsiRangeQueryCollating(Analyzer analyzer, String firstBeg, 
                                            String firstEnd, String secondBeg,
                                            String secondEnd) throws Exception {
    RAMDirectory ramDir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(ramDir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer));
    Document doc = new Document();
    doc.add(new Field("content", "\u0633\u0627\u0628", 
                      Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.close();
    IndexSearcher searcher = new IndexSearcher(ramDir, true);
    Query query = new TermRangeQuery("content", firstBeg, firstEnd, true, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("The index Term should not be included.", 0, hits.length);
    query = new TermRangeQuery("content", secondBeg, secondEnd, true, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("The index Term should be included.", 1, hits.length);
    searcher.close();
  }
  public void testFarsiTermRangeQuery(Analyzer analyzer, String firstBeg,
      String firstEnd, String secondBeg, String secondEnd) throws Exception {
    RAMDirectory farsiIndex = new RAMDirectory();
    IndexWriter writer = new IndexWriter(farsiIndex, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer));
    Document doc = new Document();
    doc.add(new Field("content", "\u0633\u0627\u0628", 
                      Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("body", "body",
                      Field.Store.YES, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
    writer.close();
    IndexReader reader = IndexReader.open(farsiIndex, true);
    IndexSearcher search = new IndexSearcher(reader);
    Query csrq 
      = new TermRangeQuery("content", firstBeg, firstEnd, true, true);
    ScoreDoc[] result = search.search(csrq, null, 1000).scoreDocs;
    assertEquals("The index Term should not be included.", 0, result.length);
    csrq = new TermRangeQuery
      ("content", secondBeg, secondEnd, true, true);
    result = search.search(csrq, null, 1000).scoreDocs;
    assertEquals("The index Term should be included.", 1, result.length);
    search.close();
  }
  public void testCollationKeySort(Analyzer usAnalyzer,
                                   Analyzer franceAnalyzer,
                                   Analyzer swedenAnalyzer,
                                   Analyzer denmarkAnalyzer,
                                   String usResult) throws Exception {
    RAMDirectory indexStore = new RAMDirectory();
    PerFieldAnalyzerWrapper analyzer
      = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    analyzer.addAnalyzer("US", usAnalyzer);
    analyzer.addAnalyzer("France", franceAnalyzer);
    analyzer.addAnalyzer("Sweden", swedenAnalyzer);
    analyzer.addAnalyzer("Denmark", denmarkAnalyzer);
    IndexWriter writer = new IndexWriter(indexStore, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer));
    String[][] sortData = new String[][] {
      {  "A",   "x",     "p\u00EAche",      "p\u00EAche",      "p\u00EAche",      "p\u00EAche"      },
      {  "B",   "y",     "HAT",             "HAT",             "HAT",             "HAT"             },
      {  "C",   "x",     "p\u00E9ch\u00E9", "p\u00E9ch\u00E9", "p\u00E9ch\u00E9", "p\u00E9ch\u00E9" },
      {  "D",   "y",     "HUT",             "HUT",             "HUT",             "HUT"             },
      {  "E",   "x",     "peach",           "peach",           "peach",           "peach"           },
      {  "F",   "y",     "H\u00C5T",        "H\u00C5T",        "H\u00C5T",        "H\u00C5T"        },
      {  "G",   "x",     "sin",             "sin",             "sin",             "sin"             },
      {  "H",   "y",     "H\u00D8T",        "H\u00D8T",        "H\u00D8T",        "H\u00D8T"        },
      {  "I",   "x",     "s\u00EDn",        "s\u00EDn",        "s\u00EDn",        "s\u00EDn"        },
      {  "J",   "y",     "HOT",             "HOT",             "HOT",             "HOT"             },
    };
    for (int i = 0 ; i < sortData.length ; ++i) {
      Document doc = new Document();
      doc.add(new Field("tracer", sortData[i][0], 
                        Field.Store.YES, Field.Index.NO));
      doc.add(new Field("contents", sortData[i][1], 
                        Field.Store.NO, Field.Index.ANALYZED));
      if (sortData[i][2] != null) 
        doc.add(new Field("US", sortData[i][2], 
                          Field.Store.NO, Field.Index.ANALYZED));
      if (sortData[i][3] != null) 
        doc.add(new Field("France", sortData[i][3], 
                          Field.Store.NO, Field.Index.ANALYZED));
      if (sortData[i][4] != null)
        doc.add(new Field("Sweden", sortData[i][4], 
                          Field.Store.NO, Field.Index.ANALYZED));
      if (sortData[i][5] != null) 
        doc.add(new Field("Denmark", sortData[i][5], 
                          Field.Store.NO, Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.optimize();
    writer.close();
    Searcher searcher = new IndexSearcher(indexStore, true);
    Sort sort = new Sort();
    Query queryX = new TermQuery(new Term ("contents", "x"));
    Query queryY = new TermQuery(new Term ("contents", "y"));
    sort.setSort(new SortField("US", SortField.STRING));
    assertMatches(searcher, queryY, sort, usResult);
    sort.setSort(new SortField("France", SortField.STRING));
    assertMatches(searcher, queryX, sort, "EACGI");
    sort.setSort(new SortField("Sweden", SortField.STRING));
    assertMatches(searcher, queryY, sort, "BJDFH");
    sort.setSort(new SortField("Denmark", SortField.STRING));
    assertMatches(searcher, queryY, sort, "BJDHF");
  }
  private void assertMatches(Searcher searcher, Query query, Sort sort, 
                             String expectedResult) throws IOException {
    ScoreDoc[] result = searcher.search(query, null, 1000, sort).scoreDocs;
    StringBuilder buff = new StringBuilder(10);
    int n = result.length;
    for (int i = 0 ; i < n ; ++i) {
      Document doc = searcher.doc(result[i].doc);
      String[] v = doc.getValues("tracer");
      for (int j = 0 ; j < v.length ; ++j) {
        buff.append(v[j]);
      }
    }
    assertEquals(expectedResult, buff.toString());
  }
}
