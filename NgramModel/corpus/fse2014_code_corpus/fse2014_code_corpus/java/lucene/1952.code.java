package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestMatchAllDocsQuery extends LuceneTestCase {
  private Analyzer analyzer = new StandardAnalyzer(TEST_VERSION_CURRENT);
  public void testQuery() throws Exception {
    RAMDirectory dir = new RAMDirectory();
    IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer).setMaxBufferedDocs(2));
    addDoc("one", iw, 1f);
    addDoc("two", iw, 20f);
    addDoc("three four", iw, 300f);
    iw.close();
    IndexReader ir = IndexReader.open(dir, false);
    IndexSearcher is = new IndexSearcher(ir);
    ScoreDoc[] hits;
    hits = is.search(new MatchAllDocsQuery(), null, 1000).scoreDocs;
    assertEquals(3, hits.length);
    assertEquals("one", ir.document(hits[0].doc).get("key"));
    assertEquals("two", ir.document(hits[1].doc).get("key"));
    assertEquals("three four", ir.document(hits[2].doc).get("key"));
    MatchAllDocsQuery normsQuery = new MatchAllDocsQuery("key");
    hits = is.search(normsQuery, null, 1000).scoreDocs;
    assertEquals(3, hits.length);
    assertEquals("three four", ir.document(hits[0].doc).get("key"));    
    assertEquals("two", ir.document(hits[1].doc).get("key"));
    assertEquals("one", ir.document(hits[2].doc).get("key"));
    ir.setNorm(0, "key", 400f);
    normsQuery = new MatchAllDocsQuery("key");
    hits = is.search(normsQuery, null, 1000).scoreDocs;
    assertEquals(3, hits.length);
    assertEquals("one", ir.document(hits[0].doc).get("key"));
    assertEquals("three four", ir.document(hits[1].doc).get("key"));    
    assertEquals("two", ir.document(hits[2].doc).get("key"));
    BooleanQuery bq = new BooleanQuery();
    bq.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
    bq.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
    hits = is.search(bq, null, 1000).scoreDocs;
    assertEquals(3, hits.length);
    bq = new BooleanQuery();
    bq.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
    bq.add(new TermQuery(new Term("key", "three")), BooleanClause.Occur.MUST);
    hits = is.search(bq, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    is.getIndexReader().deleteDocument(0);
    hits = is.search(new MatchAllDocsQuery(), null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    QueryParser qp = new QueryParser(TEST_VERSION_CURRENT, "key", analyzer);
    hits = is.search(qp.parse(new MatchAllDocsQuery().toString()), null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    Query maq = new MatchAllDocsQuery();
    maq.setBoost(2.3f);
    Query pq = qp.parse(maq.toString());
    hits = is.search(pq, null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    is.close();
    ir.close();
    dir.close();
  }
  public void testEquals() {
    Query q1 = new MatchAllDocsQuery();
    Query q2 = new MatchAllDocsQuery();
    assertTrue(q1.equals(q2));
    q1.setBoost(1.5f);
    assertFalse(q1.equals(q2));
  }
  private void addDoc(String text, IndexWriter iw, float boost) throws IOException {
    Document doc = new Document();
    Field f = new Field("key", text, Field.Store.YES, Field.Index.ANALYZED);
    f.setBoost(boost);
    doc.add(f);
    iw.addDocument(doc);
  }
}
