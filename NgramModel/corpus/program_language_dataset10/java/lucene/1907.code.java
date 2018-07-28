package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.MockRAMDirectory;
public class TestSegmentTermEnum extends LuceneTestCase {
  Directory dir = new RAMDirectory();
  public void testTermEnum() throws IOException {
    IndexWriter writer = null;
    writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < 100; i++) {
      addDoc(writer, "aaa");
      addDoc(writer, "aaa bbb");
    }
    writer.close();
    verifyDocFreq();
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    writer.optimize();
    writer.close();
    verifyDocFreq();
  }
  public void testPrevTermAtEnd() throws IOException
  {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    addDoc(writer, "aaa bbb");
    writer.close();
    SegmentReader reader = SegmentReader.getOnlySegmentReader(dir);
    SegmentTermEnum termEnum = (SegmentTermEnum) reader.terms();
    assertTrue(termEnum.next());
    assertEquals("aaa", termEnum.term().text());
    assertTrue(termEnum.next());
    assertEquals("aaa", termEnum.prev().text());
    assertEquals("bbb", termEnum.term().text());
    assertFalse(termEnum.next());
    assertEquals("bbb", termEnum.prev().text());
  }
  private void verifyDocFreq()
      throws IOException
  {
      IndexReader reader = IndexReader.open(dir, true);
      TermEnum termEnum = null;
    termEnum = reader.terms();
    termEnum.next();
    assertEquals("aaa", termEnum.term().text());
    assertEquals(200, termEnum.docFreq());
    termEnum.next();
    assertEquals("bbb", termEnum.term().text());
    assertEquals(100, termEnum.docFreq());
    termEnum.close();
    termEnum = reader.terms(new Term("content", "aaa"));
    assertEquals("aaa", termEnum.term().text());
    assertEquals(200, termEnum.docFreq());
    termEnum.next();
    assertEquals("bbb", termEnum.term().text());
    assertEquals(100, termEnum.docFreq());
    termEnum.close();
  }
  private void addDoc(IndexWriter writer, String value) throws IOException
  {
    Document doc = new Document();
    doc.add(new Field("content", value, Field.Store.NO, Field.Index.ANALYZED));
    writer.addDocument(doc);
  }
}
