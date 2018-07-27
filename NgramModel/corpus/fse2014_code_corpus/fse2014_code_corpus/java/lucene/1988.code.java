package org.apache.lucene.search;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestTopScoreDocCollector extends LuceneTestCase {
  public TestTopScoreDocCollector() {
  }
  public TestTopScoreDocCollector(String name) {
    super(name);
  }
  public void testOutOfOrderCollection() throws Exception {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < 10; i++) {
      writer.addDocument(new Document());
    }
    writer.commit();
    writer.close();
    boolean[] inOrder = new boolean[] { false, true };
    String[] actualTSDCClass = new String[] {
        "OutOfOrderTopScoreDocCollector", 
        "InOrderTopScoreDocCollector" 
    };
    BooleanQuery bq = new BooleanQuery();
    bq.add(new MatchAllDocsQuery(), Occur.SHOULD);
    bq.setMinimumNumberShouldMatch(1);
    IndexSearcher searcher = new IndexSearcher(dir, true);
    for (int i = 0; i < inOrder.length; i++) {
      TopDocsCollector<ScoreDoc> tdc = TopScoreDocCollector.create(3, inOrder[i]);
      assertEquals("org.apache.lucene.search.TopScoreDocCollector$" + actualTSDCClass[i], tdc.getClass().getName());
      searcher.search(new MatchAllDocsQuery(), tdc);
      ScoreDoc[] sd = tdc.topDocs().scoreDocs;
      assertEquals(3, sd.length);
      for (int j = 0; j < sd.length; j++) {
        assertEquals("expected doc Id " + j + " found " + sd[j].doc, j, sd[j].doc);
      }
    }
  }
}
