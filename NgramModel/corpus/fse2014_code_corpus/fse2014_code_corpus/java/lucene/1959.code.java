package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
public class TestNot extends LuceneTestCase {
  public TestNot(String name) {
    super(name);
  }
  public void testNot() throws Exception {
    RAMDirectory store = new RAMDirectory();
    IndexWriter writer = new IndexWriter(store, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(
        TEST_VERSION_CURRENT)));
    Document d1 = new Document();
    d1.add(new Field("field", "a b", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(d1);
    writer.optimize();
    writer.close();
    Searcher searcher = new IndexSearcher(store, true);
      QueryParser parser = new QueryParser(TEST_VERSION_CURRENT, "field", new SimpleAnalyzer(TEST_VERSION_CURRENT));
    Query query = parser.parse("a NOT b");
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
  }
}
