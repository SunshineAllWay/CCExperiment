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
import org.apache.lucene.search.PhraseQuery;
public class TestAddIndexesNoOptimize extends LuceneTestCase {
  public void testSimpleCase() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    Directory aux2 = new RAMDirectory();
    IndexWriter writer = null;
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT,
        new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.CREATE));
    addDocs(writer, 100);
    assertEquals(100, writer.maxDoc());
    writer.close();
    writer = newWriter(aux, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false); 
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false); 
    addDocs(writer, 40);
    assertEquals(40, writer.maxDoc());
    writer.close();
    writer = newWriter(aux2, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
    addDocs2(writer, 50);
    assertEquals(50, writer.maxDoc());
    writer.close();
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    assertEquals(100, writer.maxDoc());
    writer.addIndexesNoOptimize(new Directory[] { aux, aux2 });
    assertEquals(190, writer.maxDoc());
    writer.close();
    verifyNumDocs(aux, 40);
    verifyNumDocs(dir, 190);
    Directory aux3 = new RAMDirectory();
    writer = newWriter(aux3, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    addDocs(writer, 40);
    assertEquals(40, writer.maxDoc());
    writer.close();
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    assertEquals(190, writer.maxDoc());
    writer.addIndexesNoOptimize(new Directory[] { aux3 });
    assertEquals(230, writer.maxDoc());
    writer.close();
    verifyNumDocs(dir, 230);
    verifyTermDocs(dir, new Term("content", "aaa"), 180);
    verifyTermDocs(dir, new Term("content", "bbb"), 50);
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    writer.optimize();
    writer.close();
    verifyNumDocs(dir, 230);
    verifyTermDocs(dir, new Term("content", "aaa"), 180);
    verifyTermDocs(dir, new Term("content", "bbb"), 50);
    Directory aux4 = new RAMDirectory();
    writer = newWriter(aux4, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    addDocs2(writer, 1);
    writer.close();
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    assertEquals(230, writer.maxDoc());
    writer.addIndexesNoOptimize(new Directory[] { aux4 });
    assertEquals(231, writer.maxDoc());
    writer.close();
    verifyNumDocs(dir, 231);
    verifyTermDocs(dir, new Term("content", "bbb"), 51);
  }
  public void testWithPendingDeletes() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    setUpDirs(dir, aux);
    IndexWriter writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    writer.addIndexesNoOptimize(new Directory[] {aux});
    for (int i = 0; i < 20; i++) {
      Document doc = new Document();
      doc.add(new Field("id", "" + (i % 10), Field.Store.NO, Field.Index.NOT_ANALYZED));
      doc.add(new Field("content", "bbb " + i, Field.Store.NO,
                        Field.Index.ANALYZED));
      writer.updateDocument(new Term("id", "" + (i%10)), doc);
    }
    PhraseQuery q = new PhraseQuery();
    q.add(new Term("content", "bbb"));
    q.add(new Term("content", "14"));
    writer.deleteDocuments(q);
    writer.optimize();
    writer.commit();
    verifyNumDocs(dir, 1039);
    verifyTermDocs(dir, new Term("content", "aaa"), 1030);
    verifyTermDocs(dir, new Term("content", "bbb"), 9);
    writer.close();
    dir.close();
    aux.close();
  }
  public void testWithPendingDeletes2() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    setUpDirs(dir, aux);
    IndexWriter writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    for (int i = 0; i < 20; i++) {
      Document doc = new Document();
      doc.add(new Field("id", "" + (i % 10), Field.Store.NO, Field.Index.NOT_ANALYZED));
      doc.add(new Field("content", "bbb " + i, Field.Store.NO,
                        Field.Index.ANALYZED));
      writer.updateDocument(new Term("id", "" + (i%10)), doc);
    }
    writer.addIndexesNoOptimize(new Directory[] {aux});
    PhraseQuery q = new PhraseQuery();
    q.add(new Term("content", "bbb"));
    q.add(new Term("content", "14"));
    writer.deleteDocuments(q);
    writer.optimize();
    writer.commit();
    verifyNumDocs(dir, 1039);
    verifyTermDocs(dir, new Term("content", "aaa"), 1030);
    verifyTermDocs(dir, new Term("content", "bbb"), 9);
    writer.close();
    dir.close();
    aux.close();
  }
  public void testWithPendingDeletes3() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    setUpDirs(dir, aux);
    IndexWriter writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    for (int i = 0; i < 20; i++) {
      Document doc = new Document();
      doc.add(new Field("id", "" + (i % 10), Field.Store.NO, Field.Index.NOT_ANALYZED));
      doc.add(new Field("content", "bbb " + i, Field.Store.NO,
                        Field.Index.ANALYZED));
      writer.updateDocument(new Term("id", "" + (i%10)), doc);
    }
    PhraseQuery q = new PhraseQuery();
    q.add(new Term("content", "bbb"));
    q.add(new Term("content", "14"));
    writer.deleteDocuments(q);
    writer.addIndexesNoOptimize(new Directory[] {aux});
    writer.optimize();
    writer.commit();
    verifyNumDocs(dir, 1039);
    verifyTermDocs(dir, new Term("content", "aaa"), 1030);
    verifyTermDocs(dir, new Term("content", "bbb"), 9);
    writer.close();
    dir.close();
    aux.close();
  }
  public void testAddSelf() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    IndexWriter writer = null;
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    addDocs(writer, 100);
    assertEquals(100, writer.maxDoc());
    writer.close();
    writer = newWriter(aux, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE).setMaxBufferedDocs(1000));
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false); 
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false); 
    addDocs(writer, 40);
    writer.close();
    writer = newWriter(aux, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE).setMaxBufferedDocs(1000));
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false); 
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false); 
    addDocs(writer, 100);
    writer.close();
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    try {
      writer.addIndexesNoOptimize(new Directory[] { aux, dir });
      assertTrue(false);
    }
    catch (IllegalArgumentException e) {
      assertEquals(100, writer.maxDoc());
    }
    writer.close();
    verifyNumDocs(dir, 100);
  }
  public void testNoTailSegments() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    setUpDirs(dir, aux);
    IndexWriter writer = newWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(10));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(4);
    addDocs(writer, 10);
    writer.addIndexesNoOptimize(new Directory[] { aux });
    assertEquals(1040, writer.maxDoc());
    assertEquals(2, writer.getSegmentCount());
    assertEquals(1000, writer.getDocCount(0));
    writer.close();
    verifyNumDocs(dir, 1040);
  }
  public void testNoCopySegments() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    setUpDirs(dir, aux);
    IndexWriter writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(9));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(4);
    addDocs(writer, 2);
    writer.addIndexesNoOptimize(new Directory[] { aux });
    assertEquals(1032, writer.maxDoc());
    assertEquals(2, writer.getSegmentCount());
    assertEquals(1000, writer.getDocCount(0));
    writer.close();
    verifyNumDocs(dir, 1032);
  }
  public void testNoMergeAfterCopy() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    setUpDirs(dir, aux);
    IndexWriter writer = newWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(10));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(4);
    writer.addIndexesNoOptimize(new Directory[] { aux, new RAMDirectory(aux) });
    assertEquals(1060, writer.maxDoc());
    assertEquals(1000, writer.getDocCount(0));
    writer.close();
    verifyNumDocs(dir, 1060);
  }
  public void testMergeAfterCopy() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    setUpDirs(dir, aux);
    IndexReader reader = IndexReader.open(aux, false);
    for (int i = 0; i < 20; i++) {
      reader.deleteDocument(i);
    }
    assertEquals(10, reader.numDocs());
    reader.close();
    IndexWriter writer = newWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(4));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(4);
    writer.addIndexesNoOptimize(new Directory[] { aux, new RAMDirectory(aux) });
    assertEquals(1020, writer.maxDoc());
    assertEquals(1000, writer.getDocCount(0));
    writer.close();
    verifyNumDocs(dir, 1020);
  }
  public void testMoreMerges() throws IOException {
    Directory dir = new RAMDirectory();
    Directory aux = new RAMDirectory();
    Directory aux2 = new RAMDirectory();
    setUpDirs(dir, aux);
    IndexWriter writer = newWriter(aux2, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.CREATE).setMaxBufferedDocs(100));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(10);
    writer.addIndexesNoOptimize(new Directory[] { aux });
    assertEquals(30, writer.maxDoc());
    assertEquals(3, writer.getSegmentCount());
    writer.close();
    IndexReader reader = IndexReader.open(aux, false);
    for (int i = 0; i < 27; i++) {
      reader.deleteDocument(i);
    }
    assertEquals(3, reader.numDocs());
    reader.close();
    reader = IndexReader.open(aux2, false);
    for (int i = 0; i < 8; i++) {
      reader.deleteDocument(i);
    }
    assertEquals(22, reader.numDocs());
    reader.close();
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(6));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(4);
    writer.addIndexesNoOptimize(new Directory[] { aux, aux2 });
    assertEquals(1025, writer.maxDoc());
    assertEquals(1000, writer.getDocCount(0));
    writer.close();
    verifyNumDocs(dir, 1025);
  }
  private IndexWriter newWriter(Directory dir, IndexWriterConfig conf)
      throws IOException {
    final IndexWriter writer = new IndexWriter(dir, conf);
    writer.setMergePolicy(new LogDocMergePolicy(writer));
    return writer;
  }
  private void addDocs(IndexWriter writer, int numDocs) throws IOException {
    for (int i = 0; i < numDocs; i++) {
      Document doc = new Document();
      doc.add(new Field("content", "aaa", Field.Store.NO,
                        Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
  }
  private void addDocs2(IndexWriter writer, int numDocs) throws IOException {
    for (int i = 0; i < numDocs; i++) {
      Document doc = new Document();
      doc.add(new Field("content", "bbb", Field.Store.NO,
                        Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
  }
  private void verifyNumDocs(Directory dir, int numDocs) throws IOException {
    IndexReader reader = IndexReader.open(dir, true);
    assertEquals(numDocs, reader.maxDoc());
    assertEquals(numDocs, reader.numDocs());
    reader.close();
  }
  private void verifyTermDocs(Directory dir, Term term, int numDocs)
      throws IOException {
    IndexReader reader = IndexReader.open(dir, true);
    TermDocs termDocs = reader.termDocs(term);
    int count = 0;
    while (termDocs.next())
      count++;
    assertEquals(numDocs, count);
    reader.close();
  }
  private void setUpDirs(Directory dir, Directory aux) throws IOException {
    IndexWriter writer = null;
    writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE).setMaxBufferedDocs(1000));
    addDocs(writer, 1000);
    assertEquals(1000, writer.maxDoc());
    assertEquals(1, writer.getSegmentCount());
    writer.close();
    writer = newWriter(aux, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE).setMaxBufferedDocs(100));
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false); 
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false); 
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(10);
    for (int i = 0; i < 3; i++) {
      addDocs(writer, 10);
      writer.close();
      writer = newWriter(aux, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(100));
      ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false); 
      ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false); 
      ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(10);
    }
    assertEquals(30, writer.maxDoc());
    assertEquals(3, writer.getSegmentCount());
    writer.close();
  }
  public void testHangOnClose() throws IOException {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(5));
    LogByteSizeMergePolicy lmp = new LogByteSizeMergePolicy(writer);
    lmp.setUseCompoundFile(false);
    lmp.setUseCompoundDocStore(false);
    lmp.setMergeFactor(100);
    writer.setMergePolicy(lmp);
    Document doc = new Document();
    doc.add(new Field("content", "aaa bbb ccc ddd eee fff ggg hhh iii", Field.Store.YES,
                      Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
    for(int i=0;i<60;i++)
      writer.addDocument(doc);
    Document doc2 = new Document();
    doc2.add(new Field("content", "aaa bbb ccc ddd eee fff ggg hhh iii", Field.Store.YES,
                      Field.Index.NO));
    doc2.add(new Field("content", "aaa bbb ccc ddd eee fff ggg hhh iii", Field.Store.YES,
                      Field.Index.NO));
    doc2.add(new Field("content", "aaa bbb ccc ddd eee fff ggg hhh iii", Field.Store.YES,
                      Field.Index.NO));
    doc2.add(new Field("content", "aaa bbb ccc ddd eee fff ggg hhh iii", Field.Store.YES,
                      Field.Index.NO));
    for(int i=0;i<10;i++)
      writer.addDocument(doc2);
    writer.close();
    Directory dir2 = new MockRAMDirectory();
    writer = new IndexWriter(dir2, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMergeScheduler(new SerialMergeScheduler()));
    lmp = new LogByteSizeMergePolicy(writer);
    lmp.setMinMergeMB(0.0001);
    lmp.setUseCompoundFile(false);
    lmp.setUseCompoundDocStore(false);
    lmp.setMergeFactor(4);
    writer.setMergePolicy(lmp);
    writer.addIndexesNoOptimize(new Directory[] {dir});
    writer.close();
    dir.close();
    dir2.close();
  }
  public void testTargetCFS() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = newWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false);
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false);
    addDocs(writer, 1);
    writer.close();
    Directory other = new RAMDirectory();
    writer = newWriter(other, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(true);
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(true);
    writer.addIndexesNoOptimize(new Directory[] {dir});
    assertTrue(writer.newestSegment().getUseCompoundFile());
    writer.close();
  }
}
