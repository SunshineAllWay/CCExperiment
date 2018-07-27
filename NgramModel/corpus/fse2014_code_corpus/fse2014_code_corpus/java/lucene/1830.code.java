package org.apache.lucene;
import java.util.Collection;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.TestIndexWriter;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.util.ThreadInterruptedException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
public class TestSnapshotDeletionPolicy extends LuceneTestCase
{
  public static final String INDEX_PATH = "test.snapshots";
  public void testSnapshotDeletionPolicy() throws Exception {
    File dir = _TestUtil.getTempDir(INDEX_PATH);
    try {
      Directory fsDir = FSDirectory.open(dir);
      runTest(fsDir);
      fsDir.close();
    } finally {
      _TestUtil.rmDir(dir);
    }
    MockRAMDirectory dir2 = new MockRAMDirectory();
    runTest(dir2);
    dir2.close();
  }
  public void testReuseAcrossWriters() throws Exception {
    Directory dir = new MockRAMDirectory();
    SnapshotDeletionPolicy dp = new SnapshotDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, 
        new StandardAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(dp)
        .setMaxBufferedDocs(2));
    Document doc = new Document();
    doc.add(new Field("content", "aaa", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
    for(int i=0;i<7;i++) {
      writer.addDocument(doc);
      if (i % 2 == 0) {
        writer.commit();
      }
    }
    IndexCommit cp = dp.snapshot();
    copyFiles(dir, cp);
    writer.close();
    copyFiles(dir, cp);
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT,
        new StandardAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(dp));
    copyFiles(dir, cp);
    for(int i=0;i<7;i++) {
      writer.addDocument(doc);
      if (i % 2 == 0) {
        writer.commit();
      }
    }
    copyFiles(dir, cp);
    writer.close();
    copyFiles(dir, cp);
    dp.release();
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT,
        new StandardAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(dp));
    writer.close();
    try {
      copyFiles(dir, cp);
      fail("did not hit expected IOException");
    } catch (IOException ioe) {
    }
    dir.close();
  }
  private void runTest(Directory dir) throws Exception {
    final long stopTime = System.currentTimeMillis() + 1000;
    SnapshotDeletionPolicy dp = new SnapshotDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
    final IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, 
        new StandardAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(dp)
        .setMaxBufferedDocs(2));
    final Thread t = new Thread() {
        @Override
        public void run() {
          Document doc = new Document();
          doc.add(new Field("content", "aaa", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
          do {
            for(int i=0;i<27;i++) {
              try {
                writer.addDocument(doc);
              } catch (Throwable t) {
                t.printStackTrace(System.out);
                fail("addDocument failed");
              }
              if (i%2 == 0) {
                try {
                  writer.commit();
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              }
            }
            try {
              Thread.sleep(1);
            } catch (InterruptedException ie) {
              throw new ThreadInterruptedException(ie);
            }
          } while(System.currentTimeMillis() < stopTime);
        }
      };
    t.start();
    do {
      backupIndex(dir, dp);
      Thread.sleep(20);
    } while(t.isAlive());
    t.join();
    Document doc = new Document();
    doc.add(new Field("content", "aaa", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
    writer.addDocument(doc);
    writer.close();
    TestIndexWriter.assertNoUnreferencedFiles(dir, "some files were not deleted but should have been");
  }
  public void backupIndex(Directory dir, SnapshotDeletionPolicy dp) throws Exception {
    try {
      copyFiles(dir,  dp.snapshot());
    } finally {
      dp.release();
    }
  }
  private void copyFiles(Directory dir, IndexCommit cp) throws Exception {
    Collection<String> files = cp.getFileNames();
    for (final String fileName : files) { 
      readFile(dir, fileName);
    }
  }
  byte[] buffer = new byte[4096];
  private void readFile(Directory dir, String name) throws Exception {
    IndexInput input = dir.openInput(name);
    try {
      long size = dir.fileLength(name);
      long bytesLeft = size;
      while (bytesLeft > 0) {
        final int numToRead;
        if (bytesLeft < buffer.length)
          numToRead = (int) bytesLeft;
        else
          numToRead = buffer.length;
        input.readBytes(buffer, 0, numToRead, false);
        bytesLeft -= numToRead;
      }
      Thread.sleep(1);
    } finally {
      input.close();
    }
  }
}
