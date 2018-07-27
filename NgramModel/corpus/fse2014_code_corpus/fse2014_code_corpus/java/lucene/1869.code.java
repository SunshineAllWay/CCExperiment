package org.apache.lucene.index;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.util.LuceneTestCase;
import java.io.IOException;
public class TestConcurrentMergeScheduler extends LuceneTestCase {
  private static class FailOnlyOnFlush extends MockRAMDirectory.Failure {
    boolean doFail;
    boolean hitExc;
    @Override
    public void setDoFail() {
      this.doFail = true;
      hitExc = false;
    }
    @Override
    public void clearDoFail() {
      this.doFail = false;
    }
    @Override
    public void eval(MockRAMDirectory dir)  throws IOException {
      if (doFail && Thread.currentThread().getName().equals("main")) {
        StackTraceElement[] trace = new Exception().getStackTrace();
        for (int i = 0; i < trace.length; i++) {
          if ("doFlush".equals(trace[i].getMethodName())) {
            hitExc = true;
            throw new IOException("now failing during flush");
          }
        }
      }
    }
  }
  public void testFlushExceptions() throws IOException {
    MockRAMDirectory directory = new MockRAMDirectory();
    FailOnlyOnFlush failure = new FailOnlyOnFlush();
    directory.failOn(failure);
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(2));
    Document doc = new Document();
    Field idField = new Field("id", "", Field.Store.YES, Field.Index.NOT_ANALYZED);
    doc.add(idField);
    int extraCount = 0;
    for(int i=0;i<10;i++) {
      for(int j=0;j<20;j++) {
        idField.setValue(Integer.toString(i*20+j));
        writer.addDocument(doc);
      }
      while(true) {
        writer.addDocument(doc);
        failure.setDoFail();
        try {
          writer.flush(true, false, true);
          if (failure.hitExc) {
            fail("failed to hit IOException");
          }
          extraCount++;
        } catch (IOException ioe) {
          failure.clearDoFail();
          break;
        }
      }
    }
    writer.close();
    IndexReader reader = IndexReader.open(directory, true);
    assertEquals(200+extraCount, reader.numDocs());
    reader.close();
    directory.close();
  }
  public void testDeleteMerging() throws IOException {
    RAMDirectory directory = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    LogDocMergePolicy mp = new LogDocMergePolicy(writer);
    writer.setMergePolicy(mp);
    mp.setMinMergeDocs(1000);
    Document doc = new Document();
    Field idField = new Field("id", "", Field.Store.YES, Field.Index.NOT_ANALYZED);
    doc.add(idField);
    for(int i=0;i<10;i++) {
      for(int j=0;j<100;j++) {
        idField.setValue(Integer.toString(i*100+j));
        writer.addDocument(doc);
      }
      int delID = i;
      while(delID < 100*(1+i)) {
        writer.deleteDocuments(new Term("id", ""+delID));
        delID += 10;
      }
      writer.commit();
    }
    writer.close();
    IndexReader reader = IndexReader.open(directory, true);
    assertEquals(450, reader.numDocs());
    reader.close();
    directory.close();
  }
  public void testNoExtraFiles() throws IOException {
    RAMDirectory directory = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT))
        .setMaxBufferedDocs(2));
    for(int iter=0;iter<7;iter++) {
      for(int j=0;j<21;j++) {
        Document doc = new Document();
        doc.add(new Field("content", "a b c", Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(doc);
      }
      writer.close();
      TestIndexWriter.assertNoUnreferencedFiles(directory, "testNoExtraFiles");
      writer = new IndexWriter(directory, new IndexWriterConfig(
          TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(2));
    }
    writer.close();
    directory.close();
  }
  public void testNoWaitClose() throws IOException {
    RAMDirectory directory = new MockRAMDirectory();
    Document doc = new Document();
    Field idField = new Field("id", "", Field.Store.YES, Field.Index.NOT_ANALYZED);
    doc.add(idField);
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(2));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(100);
    for(int iter=0;iter<10;iter++) {
      for(int j=0;j<201;j++) {
        idField.setValue(Integer.toString(iter*201+j));
        writer.addDocument(doc);
      }
      int delID = iter*201;
      for(int j=0;j<20;j++) {
        writer.deleteDocuments(new Term("id", Integer.toString(delID)));
        delID += 5;
      }
      ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(3);
      writer.addDocument(doc);
      writer.commit();
      writer.close(false);
      IndexReader reader = IndexReader.open(directory, true);
      assertEquals((1+iter)*182, reader.numDocs());
      reader.close();
      writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
      ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(100);
    }
    writer.close();
    directory.close();
  }
}
