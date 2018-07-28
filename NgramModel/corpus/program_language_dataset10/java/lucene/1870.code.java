package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
public class TestCrash extends LuceneTestCase {
  private IndexWriter initIndex() throws IOException {
    return initIndex(new MockRAMDirectory());
  }
  private IndexWriter initIndex(MockRAMDirectory dir) throws IOException {
    dir.setLockFactory(NoLockFactory.getNoLockFactory());
    IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(10));
    ((ConcurrentMergeScheduler) writer.getConfig().getMergeScheduler()).setSuppressExceptions();
    Document doc = new Document();
    doc.add(new Field("content", "aaa", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("id", "0", Field.Store.YES, Field.Index.ANALYZED));
    for(int i=0;i<157;i++)
      writer.addDocument(doc);
    return writer;
  }
  private void crash(final IndexWriter writer) throws IOException {
    final MockRAMDirectory dir = (MockRAMDirectory) writer.getDirectory();
    ConcurrentMergeScheduler cms = (ConcurrentMergeScheduler) writer.getConfig().getMergeScheduler();
    dir.crash();
    cms.sync();
    dir.clearCrash();
  }
  public void testCrashWhileIndexing() throws IOException {
    IndexWriter writer = initIndex();
    MockRAMDirectory dir = (MockRAMDirectory) writer.getDirectory();
    crash(writer);
    IndexReader reader = IndexReader.open(dir, false);
    assertTrue(reader.numDocs() < 157);
  }
  public void testWriterAfterCrash() throws IOException {
    IndexWriter writer = initIndex();
    MockRAMDirectory dir = (MockRAMDirectory) writer.getDirectory();
    dir.setPreventDoubleWrite(false);
    crash(writer);
    writer = initIndex(dir);
    writer.close();
    IndexReader reader = IndexReader.open(dir, false);
    assertTrue(reader.numDocs() < 314);
  }
  public void testCrashAfterReopen() throws IOException {
    IndexWriter writer = initIndex();
    MockRAMDirectory dir = (MockRAMDirectory) writer.getDirectory();
    writer.close();
    writer = initIndex(dir);
    assertEquals(314, writer.maxDoc());
    crash(writer);
    IndexReader reader = IndexReader.open(dir, false);
    assertTrue(reader.numDocs() >= 157);
  }
  public void testCrashAfterClose() throws IOException {
    IndexWriter writer = initIndex();
    MockRAMDirectory dir = (MockRAMDirectory) writer.getDirectory();
    writer.close();
    dir.crash();
    IndexReader reader = IndexReader.open(dir, false);
    assertEquals(157, reader.numDocs());
  }
  public void testCrashAfterCloseNoWait() throws IOException {
    IndexWriter writer = initIndex();
    MockRAMDirectory dir = (MockRAMDirectory) writer.getDirectory();
    writer.close(false);
    dir.crash();
    IndexReader reader = IndexReader.open(dir, false);
    assertEquals(157, reader.numDocs());
  }
  public void testCrashReaderDeletes() throws IOException {
    IndexWriter writer = initIndex();
    MockRAMDirectory dir = (MockRAMDirectory) writer.getDirectory();
    writer.close(false);
    IndexReader reader = IndexReader.open(dir, false);
    reader.deleteDocument(3);
    dir.crash();
    reader = IndexReader.open(dir, false);
    assertEquals(157, reader.numDocs());
  }
  public void testCrashReaderDeletesAfterClose() throws IOException {
    IndexWriter writer = initIndex();
    MockRAMDirectory dir = (MockRAMDirectory) writer.getDirectory();
    writer.close(false);
    IndexReader reader = IndexReader.open(dir, false);
    reader.deleteDocument(3);
    reader.close();
    dir.crash();
    reader = IndexReader.open(dir, false);
    assertEquals(156, reader.numDocs());
  }
}
