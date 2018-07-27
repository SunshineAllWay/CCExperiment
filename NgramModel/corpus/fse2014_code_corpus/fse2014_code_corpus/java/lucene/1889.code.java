package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util._TestUtil;
import org.apache.lucene.util.LuceneTestCase;
public class TestIndexWriterMergePolicy extends LuceneTestCase {
  public void testNormalCase() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(10));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(10);
    writer.setMergePolicy(new LogDocMergePolicy(writer));
    for (int i = 0; i < 100; i++) {
      addDoc(writer);
      checkInvariants(writer);
    }
    writer.close();
  }
  public void testNoOverMerge() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(10));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(10);
    writer.setMergePolicy(new LogDocMergePolicy(writer));
    boolean noOverMerge = false;
    for (int i = 0; i < 100; i++) {
      addDoc(writer);
      checkInvariants(writer);
      if (writer.getNumBufferedDocuments() + writer.getSegmentCount() >= 18) {
        noOverMerge = true;
      }
    }
    assertTrue(noOverMerge);
    writer.close();
  }
  public void testForceFlush() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(10));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(10);
    LogDocMergePolicy mp = new LogDocMergePolicy(writer);
    mp.setMinMergeDocs(100);
    writer.setMergePolicy(mp);
    for (int i = 0; i < 100; i++) {
      addDoc(writer);
      writer.close();
      writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(10));
      writer.setMergePolicy(mp);
      mp.setMinMergeDocs(100);
      ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(10);
      checkInvariants(writer);
    }
    writer.close();
  }
  public void testMergeFactorChange() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(10));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(100);
    writer.setMergePolicy(new LogDocMergePolicy(writer));
    for (int i = 0; i < 250; i++) {
      addDoc(writer);
      checkInvariants(writer);
    }
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(5);
    for (int i = 0; i < 10; i++) {
      addDoc(writer);
    }
    checkInvariants(writer);
    writer.close();
  }
  public void testMaxBufferedDocsChange() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(101));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(101);
    writer.setMergePolicy(new LogDocMergePolicy(writer));
    for (int i = 1; i <= 100; i++) {
      for (int j = 0; j < i; j++) {
        addDoc(writer);
        checkInvariants(writer);
      }
      writer.close();
      writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(101));
      ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(101);
      writer.setMergePolicy(new LogDocMergePolicy(writer));
    }
    writer.close();
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(10));
    writer.setMergePolicy(new LogDocMergePolicy(writer));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(10);
    for (int i = 0; i < 100; i++) {
      addDoc(writer);
    }
    checkInvariants(writer);
    for (int i = 100; i < 1000; i++) {
      addDoc(writer);
    }
    writer.commit();
    ((ConcurrentMergeScheduler) writer.getConfig().getMergeScheduler()).sync();
    writer.commit();
    checkInvariants(writer);
    writer.close();
  }
  public void testMergeDocCount0() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(10));
    writer.setMergePolicy(new LogDocMergePolicy(writer));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(100);
    for (int i = 0; i < 250; i++) {
      addDoc(writer);
      checkInvariants(writer);
    }
    writer.close();
    IndexReader reader = IndexReader.open(dir, false);
    reader.deleteDocuments(new Term("content", "aaa"));
    reader.close();
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(10));
    writer.setMergePolicy(new LogDocMergePolicy(writer));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(5);
    for (int i = 0; i < 10; i++) {
      addDoc(writer);
    }
    writer.commit();
    ((ConcurrentMergeScheduler) writer.getConfig().getMergeScheduler()).sync();
    writer.commit();
    checkInvariants(writer);
    assertEquals(10, writer.maxDoc());
    writer.close();
  }
  private void addDoc(IndexWriter writer) throws IOException {
    Document doc = new Document();
    doc.add(new Field("content", "aaa", Field.Store.NO, Field.Index.ANALYZED));
    writer.addDocument(doc);
  }
  private void checkInvariants(IndexWriter writer) throws IOException {
    _TestUtil.syncConcurrentMerges(writer);
    int maxBufferedDocs = writer.getConfig().getMaxBufferedDocs();
    int mergeFactor = ((LogMergePolicy) writer.getMergePolicy()).getMergeFactor();
    int maxMergeDocs = ((LogMergePolicy) writer.getMergePolicy()).getMaxMergeDocs();
    int ramSegmentCount = writer.getNumBufferedDocuments();
    assertTrue(ramSegmentCount < maxBufferedDocs);
    int lowerBound = -1;
    int upperBound = maxBufferedDocs;
    int numSegments = 0;
    int segmentCount = writer.getSegmentCount();
    for (int i = segmentCount - 1; i >= 0; i--) {
      int docCount = writer.getDocCount(i);
      assertTrue(docCount > lowerBound);
      if (docCount <= upperBound) {
        numSegments++;
      } else {
        if (upperBound * mergeFactor <= maxMergeDocs) {
          assertTrue("maxMergeDocs=" + maxMergeDocs + "; numSegments=" + numSegments + "; upperBound=" + upperBound + "; mergeFactor=" + mergeFactor + "; segs=" + writer.segString(), numSegments < mergeFactor);
        }
        do {
          lowerBound = upperBound;
          upperBound *= mergeFactor;
        } while (docCount > upperBound);
        numSegments = 1;
      }
    }
    if (upperBound * mergeFactor <= maxMergeDocs) {
      assertTrue(numSegments < mergeFactor);
    }
    String[] files = writer.getDirectory().listAll();
    int segmentCfsCount = 0;
    for (int i = 0; i < files.length; i++) {
      if (files[i].endsWith(".cfs")) {
        segmentCfsCount++;
      }
    }
    assertEquals(segmentCount, segmentCfsCount);
  }
}
