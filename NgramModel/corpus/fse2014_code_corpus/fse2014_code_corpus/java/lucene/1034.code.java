package org.apache.lucene.store.instantiated;
import java.io.IOException;
import java.util.Arrays;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestEmptyIndex extends LuceneTestCase {
  public void testSearch() throws Exception {
    InstantiatedIndex ii = new InstantiatedIndex();
    IndexReader r = new InstantiatedIndexReader(ii);
    IndexSearcher s = new IndexSearcher(r);
    TopDocs td = s.search(new TermQuery(new Term("foo", "bar")), 1);
    assertEquals(0, td.totalHits);
    s.close();
    r.close();
    ii.close();
  }
  public void testNorms() throws Exception {
    InstantiatedIndex ii = new InstantiatedIndex();
    IndexReader r = new InstantiatedIndexReader(ii);
    testNorms(r);
    r.close();
    ii.close();
    Directory d = new RAMDirectory();
    new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))).close();
    r = IndexReader.open(d, false);
    testNorms(r);
    r.close();
    d.close();
  }
  private void testNorms(IndexReader r) throws IOException {
    byte[] norms;
    norms = r.norms("foo");
    if (norms != null) {
      assertEquals(0, norms.length);
      norms = new byte[10];
      Arrays.fill(norms, (byte)10);
      r.norms("foo", norms, 10);
      for (byte b : norms) {
        assertEquals((byte)10, b);
      }
    }
  }
  public void testTermEnum() throws Exception {
    InstantiatedIndex ii = new InstantiatedIndex();
    IndexReader r = new InstantiatedIndexReader(ii);
    termEnumTest(r);
    r.close();
    ii.close();
    Directory d = new RAMDirectory();
    new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))).close();
    r = IndexReader.open(d, false);
    termEnumTest(r);
    r.close();
    d.close();
  }
  public void termEnumTest(IndexReader r) throws Exception {
    TermEnum terms = r.terms();
    assertNull(terms.term());
    assertFalse(terms.next());
  }
}
