package org.apache.lucene.store.instantiated;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
public class TestUnoptimizedReaderOnConstructor extends LuceneTestCase {
  public void test() throws Exception {
    Directory dir = new RAMDirectory();
    IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    addDocument(iw, "Hello, world!");
    addDocument(iw, "All work and no play makes jack a dull boy");
    iw.close();
    iw = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    addDocument(iw, "Hello, tellus!");
    addDocument(iw, "All work and no play makes danny a dull boy");
    iw.close();
    iw = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    addDocument(iw, "Hello, earth!");
    addDocument(iw, "All work and no play makes wendy a dull girl");
    iw.close();
    IndexReader unoptimizedReader = IndexReader.open(dir, false);
    unoptimizedReader.deleteDocument(2);
    try {
     new InstantiatedIndex(unoptimizedReader);
    } catch (Exception e) {
      fail("No exceptions when loading an unoptimized reader!");
    }
  }
  private void addDocument(IndexWriter iw, String text) throws IOException {
    Document doc = new Document();
    doc.add(new Field("field", text, Field.Store.NO, Field.Index.ANALYZED));
    iw.addDocument(doc);
  }
}
