package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import java.io.IOException;
public class TestDateFilter extends LuceneTestCase {
  public TestDateFilter(String name) {
    super(name);
  }
  public static void testBefore() throws IOException {
    RAMDirectory indexStore = new RAMDirectory();
    IndexWriter writer = new IndexWriter(indexStore, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    long now = System.currentTimeMillis();
 	Document doc = new Document();
 	doc.add(new Field("datefield", DateTools.timeToString(now - 1000, DateTools.Resolution.MILLISECOND), Field.Store.YES, Field.Index.NOT_ANALYZED));
 	doc.add(new Field("body", "Today is a very sunny day in New York City", Field.Store.YES, Field.Index.ANALYZED));
  	writer.addDocument(doc);
 	writer.optimize();
	writer.close();
	IndexSearcher searcher = new IndexSearcher(indexStore, true);
    TermRangeFilter df1 = new TermRangeFilter("datefield", DateTools.timeToString(now - 2000, DateTools.Resolution.MILLISECOND),
                                      DateTools.timeToString(now, DateTools.Resolution.MILLISECOND), false, true);
    TermRangeFilter df2 = new TermRangeFilter("datefield", DateTools.timeToString(0, DateTools.Resolution.MILLISECOND),
                                      DateTools.timeToString(now - 2000, DateTools.Resolution.MILLISECOND), true, false);
	Query query1 = new TermQuery(new Term("body", "NoMatchForThis"));
	Query query2 = new TermQuery(new Term("body", "sunny"));
  ScoreDoc[] result;
  result = searcher.search(query1, null, 1000).scoreDocs;
  assertEquals(0, result.length);
  result = searcher.search(query2, null, 1000).scoreDocs;
  assertEquals(1, result.length);
  result = searcher.search(query1, df1, 1000).scoreDocs;
  assertEquals(0, result.length);
  result = searcher.search(query1, df2, 1000).scoreDocs;
  assertEquals(0, result.length);
   result = searcher.search(query2, df1, 1000).scoreDocs;
   assertEquals(1, result.length);
  result = searcher.search(query2, df2, 1000).scoreDocs;
  assertEquals(0, result.length);
    }
    public static void testAfter()
	throws IOException
    {
        RAMDirectory indexStore = new RAMDirectory();
        IndexWriter writer = new IndexWriter(indexStore, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
 	long now = System.currentTimeMillis();
 	Document doc = new Document();
 	doc.add(new Field("datefield", DateTools.timeToString(now + 888888, DateTools.Resolution.MILLISECOND), Field.Store.YES, Field.Index.NOT_ANALYZED));
 	doc.add(new Field("body", "Today is a very sunny day in New York City", Field.Store.YES, Field.Index.ANALYZED));
  	writer.addDocument(doc);
 	writer.optimize();
	writer.close();
	IndexSearcher searcher = new IndexSearcher(indexStore, true);
    TermRangeFilter df1 = new TermRangeFilter("datefield", DateTools.timeToString(now, DateTools.Resolution.MILLISECOND),
                                      DateTools.timeToString(now + 999999, DateTools.Resolution.MILLISECOND), true, false);
    TermRangeFilter df2 = new TermRangeFilter("datefield", DateTools.timeToString(now + 999999, DateTools.Resolution.MILLISECOND),
                                          DateTools.timeToString(now + 999999999, DateTools.Resolution.MILLISECOND), false, true);
	Query query1 = new TermQuery(new Term("body", "NoMatchForThis"));
	Query query2 = new TermQuery(new Term("body", "sunny"));
  ScoreDoc[] result;
  result = searcher.search(query1, null, 1000).scoreDocs;
  assertEquals(0, result.length);
  result = searcher.search(query2, null, 1000).scoreDocs;
  assertEquals(1, result.length);
  result = searcher.search(query1, df1, 1000).scoreDocs;
  assertEquals(0, result.length);
  result = searcher.search(query1, df2, 1000).scoreDocs;
  assertEquals(0, result.length);
   result = searcher.search(query2, df1, 1000).scoreDocs;
   assertEquals(1, result.length);
  result = searcher.search(query2, df2, 1000).scoreDocs;
  assertEquals(0, result.length);
    }
}
