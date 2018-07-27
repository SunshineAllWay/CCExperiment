package org.apache.lucene.index;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestDeletionPolicy extends LuceneTestCase {
  private void verifyCommitOrder(List<? extends IndexCommit> commits) throws IOException {
    final IndexCommit firstCommit =  commits.get(0);
    long last = SegmentInfos.generationFromSegmentsFileName(firstCommit.getSegmentsFileName());
    assertEquals(last, firstCommit.getGeneration());
    long lastVersion = firstCommit.getVersion();
    long lastTimestamp = firstCommit.getTimestamp();
    for(int i=1;i<commits.size();i++) {
      final IndexCommit commit =  commits.get(i);
      long now = SegmentInfos.generationFromSegmentsFileName(commit.getSegmentsFileName());
      long nowVersion = commit.getVersion();
      long nowTimestamp = commit.getTimestamp();
      assertTrue("SegmentInfos commits are out-of-order", now > last);
      assertTrue("SegmentInfos versions are out-of-order", nowVersion > lastVersion);
      assertTrue("SegmentInfos timestamps are out-of-order: now=" + nowTimestamp + " vs last=" + lastTimestamp, nowTimestamp >= lastTimestamp);
      assertEquals(now, commit.getGeneration());
      last = now;
      lastVersion = nowVersion;
      lastTimestamp = nowTimestamp;
    }
  }
  class KeepAllDeletionPolicy implements IndexDeletionPolicy {
    int numOnInit;
    int numOnCommit;
    Directory dir;
    public void onInit(List<? extends IndexCommit> commits) throws IOException {
      verifyCommitOrder(commits);
      numOnInit++;
    }
    public void onCommit(List<? extends IndexCommit> commits) throws IOException {
      IndexCommit lastCommit =  commits.get(commits.size()-1);
      IndexReader r = IndexReader.open(dir, true);
      assertEquals("lastCommit.isOptimized()=" + lastCommit.isOptimized() + " vs IndexReader.isOptimized=" + r.isOptimized(), r.isOptimized(), lastCommit.isOptimized());
      r.close();
      verifyCommitOrder(commits);
      numOnCommit++;
    }
  }
  class KeepNoneOnInitDeletionPolicy implements IndexDeletionPolicy {
    int numOnInit;
    int numOnCommit;
    public void onInit(List<? extends IndexCommit> commits) throws IOException {
      verifyCommitOrder(commits);
      numOnInit++;
      for (final IndexCommit commit : commits) {
        commit.delete();
        assertTrue(commit.isDeleted());
      }
    }
    public void onCommit(List<? extends IndexCommit> commits) throws IOException {
      verifyCommitOrder(commits);
      int size = commits.size();
      for(int i=0;i<size-1;i++) {
        ((IndexCommit) commits.get(i)).delete();
      }
      numOnCommit++;
    }
  }
  class KeepLastNDeletionPolicy implements IndexDeletionPolicy {
    int numOnInit;
    int numOnCommit;
    int numToKeep;
    int numDelete;
    Set<String> seen = new HashSet<String>();
    public KeepLastNDeletionPolicy(int numToKeep) {
      this.numToKeep = numToKeep;
    }
    public void onInit(List<? extends IndexCommit> commits) throws IOException {
      verifyCommitOrder(commits);
      numOnInit++;
      doDeletes(commits, false);
    }
    public void onCommit(List<? extends IndexCommit> commits) throws IOException {
      verifyCommitOrder(commits);
      doDeletes(commits, true);
    }
    private void doDeletes(List<? extends IndexCommit> commits, boolean isCommit) {
      if (isCommit) {
        String fileName = ((IndexCommit) commits.get(commits.size()-1)).getSegmentsFileName();
        if (seen.contains(fileName)) {
          throw new RuntimeException("onCommit was called twice on the same commit point: " + fileName);
        }
        seen.add(fileName);
        numOnCommit++;
      }
      int size = commits.size();
      for(int i=0;i<size-numToKeep;i++) {
        ((IndexCommit) commits.get(i)).delete();
        numDelete++;
      }
    }
  }
  class ExpirationTimeDeletionPolicy implements IndexDeletionPolicy {
    Directory dir;
    double expirationTimeSeconds;
    int numDelete;
    public ExpirationTimeDeletionPolicy(Directory dir, double seconds) {
      this.dir = dir;
      this.expirationTimeSeconds = seconds;
    }
    public void onInit(List<? extends IndexCommit> commits) throws IOException {
      verifyCommitOrder(commits);
      onCommit(commits);
    }
    public void onCommit(List<? extends IndexCommit> commits) throws IOException {
      verifyCommitOrder(commits);
      IndexCommit lastCommit = commits.get(commits.size()-1);
      double expireTime = dir.fileModified(lastCommit.getSegmentsFileName())/1000.0 - expirationTimeSeconds;
      for (final IndexCommit commit : commits) {
        double modTime = dir.fileModified(commit.getSegmentsFileName())/1000.0;
        if (commit != lastCommit && modTime < expireTime) {
          commit.delete();
          numDelete += 1;
        }
      }
    }
  }
  public void testExpirationTimeDeletionPolicy() throws IOException, InterruptedException {
    final double SECONDS = 2.0;
    boolean useCompoundFile = true;
    Directory dir = new RAMDirectory();
    ExpirationTimeDeletionPolicy policy = new ExpirationTimeDeletionPolicy(dir, SECONDS);
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(policy));
    LogMergePolicy lmp = (LogMergePolicy) writer.getMergePolicy();
    lmp.setUseCompoundFile(useCompoundFile);
    lmp.setUseCompoundDocStore(useCompoundFile);
    writer.close();
    long lastDeleteTime = 0;
    for(int i=0;i<7;i++) {
      lastDeleteTime = System.currentTimeMillis();
      writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.APPEND).setIndexDeletionPolicy(policy));
      lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      for(int j=0;j<17;j++) {
        addDoc(writer);
      }
      writer.close();
      Thread.sleep((int) (1000.0*(SECONDS/5.0)));
    }
    assertTrue("no commits were deleted", policy.numDelete > 0);
    long gen = SegmentInfos.getCurrentSegmentGeneration(dir);
    String fileName = IndexFileNames.fileNameFromGeneration(IndexFileNames.SEGMENTS,
                                                            "",
                                                            gen);
    dir.deleteFile(IndexFileNames.SEGMENTS_GEN);
    while(gen > 0) {
      try {
        IndexReader reader = IndexReader.open(dir, true);
        reader.close();
        fileName = IndexFileNames.fileNameFromGeneration(IndexFileNames.SEGMENTS,
                                                         "",
                                                         gen);
        long modTime = dir.fileModified(fileName);
        assertTrue("commit point was older than " + SECONDS + " seconds (" + (lastDeleteTime - modTime) + " msec) but did not get deleted", lastDeleteTime - modTime <= (SECONDS*1000));
      } catch (IOException e) {
        break;
      }
      dir.deleteFile(IndexFileNames.fileNameFromGeneration(IndexFileNames.SEGMENTS, "", gen));
      gen--;
    }
    dir.close();
  }
  public void testKeepAllDeletionPolicy() throws IOException {
    for(int pass=0;pass<2;pass++) {
      boolean useCompoundFile = (pass % 2) != 0;
      KeepAllDeletionPolicy policy = new KeepAllDeletionPolicy();
      Directory dir = new RAMDirectory();
      policy.dir = dir;
      IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
          TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setIndexDeletionPolicy(policy).setMaxBufferedDocs(10)
          .setMergeScheduler(new SerialMergeScheduler()));
      LogMergePolicy lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      for(int i=0;i<107;i++) {
        addDoc(writer);
      }
      writer.close();
      writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT,
          new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(
          OpenMode.APPEND).setIndexDeletionPolicy(policy));
      lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      writer.optimize();
      writer.close();
      assertEquals(2, policy.numOnInit);
      assertEquals(2, policy.numOnCommit);
      Collection<IndexCommit> commits = IndexReader.listCommits(dir);
      assertEquals(3, commits.size());
      for (final IndexCommit commit : commits) {
        IndexReader r = IndexReader.open(commit, null, false);
        r.close();
      }
      dir.deleteFile(IndexFileNames.SEGMENTS_GEN);
      long gen = SegmentInfos.getCurrentSegmentGeneration(dir);
      while(gen > 0) {
        IndexReader reader = IndexReader.open(dir, true);
        reader.close();
        dir.deleteFile(IndexFileNames.fileNameFromGeneration(IndexFileNames.SEGMENTS, "", gen));
        gen--;
        if (gen > 0) {
          int preCount = dir.listAll().length;
          writer = new IndexWriter(dir, new IndexWriterConfig(
              TEST_VERSION_CURRENT,
              new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(
              OpenMode.APPEND).setIndexDeletionPolicy(policy));
          writer.close();
          int postCount = dir.listAll().length;
          assertTrue(postCount < preCount);
        }
      }
      dir.close();
    }
  }
  public void testOpenPriorSnapshot() throws IOException {
    KeepAllDeletionPolicy policy = new KeepAllDeletionPolicy();
    Directory dir = new MockRAMDirectory();
    policy.dir = dir;
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setIndexDeletionPolicy(policy).setMaxBufferedDocs(2));
    for(int i=0;i<10;i++) {
      addDoc(writer);
      if ((1+i)%2 == 0)
        writer.commit();
    }
    writer.close();
    Collection<IndexCommit> commits = IndexReader.listCommits(dir);
    assertEquals(6, commits.size());
    IndexCommit lastCommit = null;
    for (final IndexCommit commit : commits) {
      if (lastCommit == null || commit.getGeneration() > lastCommit.getGeneration())
        lastCommit = commit;
    }
    assertTrue(lastCommit != null);
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(policy));
    addDoc(writer);
    assertEquals(11, writer.numDocs());
    writer.optimize();
    writer.close();
    assertEquals(7, IndexReader.listCommits(dir).size());
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setIndexDeletionPolicy(policy).setIndexCommit(lastCommit));
    assertEquals(10, writer.numDocs());
    writer.rollback();
    IndexReader r = IndexReader.open(dir, true);
    assertTrue(r.isOptimized());
    assertEquals(11, r.numDocs());
    r.close();
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
        .setIndexDeletionPolicy(policy).setIndexCommit(lastCommit));
    assertEquals(10, writer.numDocs());
    writer.close();
    assertEquals(8, IndexReader.listCommits(dir).size());
    r = IndexReader.open(dir, true);
    assertTrue(!r.isOptimized());
    assertEquals(10, r.numDocs());
    r.close();
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(policy));
    writer.optimize();
    writer.close();
    r = IndexReader.open(dir, true);
    assertTrue(r.isOptimized());
    assertEquals(10, r.numDocs());
    r.close();
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setIndexCommit(lastCommit));
    assertEquals(10, writer.numDocs());
    r = IndexReader.open(dir, true);
    assertTrue(r.isOptimized());
    assertEquals(10, r.numDocs());
    r.close();
    writer.close();
    r = IndexReader.open(dir, true);
    assertTrue(!r.isOptimized());
    assertEquals(10, r.numDocs());
    r.close();
    dir.close();
  }
  public void testKeepNoneOnInitDeletionPolicy() throws IOException {
    for(int pass=0;pass<2;pass++) {
      boolean useCompoundFile = (pass % 2) != 0;
      KeepNoneOnInitDeletionPolicy policy = new KeepNoneOnInitDeletionPolicy();
      Directory dir = new RAMDirectory();
      IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
          TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.CREATE).setIndexDeletionPolicy(policy)
          .setMaxBufferedDocs(10));
      LogMergePolicy lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      for(int i=0;i<107;i++) {
        addDoc(writer);
      }
      writer.close();
      writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.APPEND).setIndexDeletionPolicy(policy));
      lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      writer.optimize();
      writer.close();
      assertEquals(2, policy.numOnInit);
      assertEquals(2, policy.numOnCommit);
      IndexReader reader = IndexReader.open(dir, true);
      reader.close();
      dir.close();
    }
  }
  public void testKeepLastNDeletionPolicy() throws IOException {
    final int N = 5;
    for(int pass=0;pass<2;pass++) {
      boolean useCompoundFile = (pass % 2) != 0;
      Directory dir = new RAMDirectory();
      KeepLastNDeletionPolicy policy = new KeepLastNDeletionPolicy(N);
      for(int j=0;j<N+1;j++) {
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
            TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
            .setOpenMode(OpenMode.CREATE).setIndexDeletionPolicy(policy)
            .setMaxBufferedDocs(10));
        LogMergePolicy lmp = (LogMergePolicy) writer.getMergePolicy();
        lmp.setUseCompoundFile(useCompoundFile);
        lmp.setUseCompoundDocStore(useCompoundFile);
        for(int i=0;i<17;i++) {
          addDoc(writer);
        }
        writer.optimize();
        writer.close();
      }
      assertTrue(policy.numDelete > 0);
      assertEquals(N+1, policy.numOnInit);
      assertEquals(N+1, policy.numOnCommit);
      dir.deleteFile(IndexFileNames.SEGMENTS_GEN);
      long gen = SegmentInfos.getCurrentSegmentGeneration(dir);
      for(int i=0;i<N+1;i++) {
        try {
          IndexReader reader = IndexReader.open(dir, true);
          reader.close();
          if (i == N) {
            fail("should have failed on commits prior to last " + N);
          }
        } catch (IOException e) {
          if (i != N) {
            throw e;
          }
        }
        if (i < N) {
          dir.deleteFile(IndexFileNames.fileNameFromGeneration(IndexFileNames.SEGMENTS, "", gen));
        }
        gen--;
      }
      dir.close();
    }
  }
  public void testKeepLastNDeletionPolicyWithReader() throws IOException {
    final int N = 10;
    for(int pass=0;pass<2;pass++) {
      boolean useCompoundFile = (pass % 2) != 0;
      KeepLastNDeletionPolicy policy = new KeepLastNDeletionPolicy(N);
      Directory dir = new RAMDirectory();
      IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
          TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.CREATE).setIndexDeletionPolicy(policy));
      LogMergePolicy lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      writer.close();
      Term searchTerm = new Term("content", "aaa");        
      Query query = new TermQuery(searchTerm);
      for(int i=0;i<N+1;i++) {
        writer = new IndexWriter(dir, new IndexWriterConfig(
            TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
            .setOpenMode(OpenMode.APPEND).setIndexDeletionPolicy(policy));
        lmp = (LogMergePolicy) writer.getMergePolicy();
        lmp.setUseCompoundFile(useCompoundFile);
        lmp.setUseCompoundDocStore(useCompoundFile);
        for(int j=0;j<17;j++) {
          addDoc(writer);
        }
        writer.close();
        IndexReader reader = IndexReader.open(dir, policy, false);
        reader.deleteDocument(3*i+1);
        reader.setNorm(4*i+1, "content", 2.0F);
        IndexSearcher searcher = new IndexSearcher(reader);
        ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
        assertEquals(16*(1+i), hits.length);
        reader.close();
        searcher.close();
      }
      writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.APPEND).setIndexDeletionPolicy(policy));
      lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      writer.optimize();
      writer.close();
      assertEquals(2*(N+2), policy.numOnInit);
      assertEquals(2*(N+2)-1, policy.numOnCommit);
      IndexSearcher searcher = new IndexSearcher(dir, false);
      ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
      assertEquals(176, hits.length);
      long gen = SegmentInfos.getCurrentSegmentGeneration(dir);
      dir.deleteFile(IndexFileNames.SEGMENTS_GEN);
      int expectedCount = 176;
      for(int i=0;i<N+1;i++) {
        try {
          IndexReader reader = IndexReader.open(dir, true);
          searcher = new IndexSearcher(reader);
          hits = searcher.search(query, null, 1000).scoreDocs;
          if (i > 1) {
            if (i % 2 == 0) {
              expectedCount += 1;
            } else {
              expectedCount -= 17;
            }
          }
          assertEquals(expectedCount, hits.length);
          searcher.close();
          reader.close();
          if (i == N) {
            fail("should have failed on commits before last 5");
          }
        } catch (IOException e) {
          if (i != N) {
            throw e;
          }
        }
        if (i < N) {
          dir.deleteFile(IndexFileNames.fileNameFromGeneration(IndexFileNames.SEGMENTS, "", gen));
        }
        gen--;
      }
      dir.close();
    }
  }
  public void testKeepLastNDeletionPolicyWithCreates() throws IOException {
    final int N = 10;
    for(int pass=0;pass<2;pass++) {
      boolean useCompoundFile = (pass % 2) != 0;
      KeepLastNDeletionPolicy policy = new KeepLastNDeletionPolicy(N);
      Directory dir = new RAMDirectory();
      IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
          TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.CREATE).setIndexDeletionPolicy(policy)
          .setMaxBufferedDocs(10));
      LogMergePolicy lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      writer.close();
      Term searchTerm = new Term("content", "aaa");        
      Query query = new TermQuery(searchTerm);
      for(int i=0;i<N+1;i++) {
        writer = new IndexWriter(dir, new IndexWriterConfig(
            TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
            .setOpenMode(OpenMode.APPEND).setIndexDeletionPolicy(policy)
            .setMaxBufferedDocs(10));
        lmp = (LogMergePolicy) writer.getMergePolicy();
        lmp.setUseCompoundFile(useCompoundFile);
        lmp.setUseCompoundDocStore(useCompoundFile);
        for(int j=0;j<17;j++) {
          addDoc(writer);
        }
        writer.close();
        IndexReader reader = IndexReader.open(dir, policy, false);
        reader.deleteDocument(3);
        reader.setNorm(5, "content", 2.0F);
        IndexSearcher searcher = new IndexSearcher(reader);
        ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
        assertEquals(16, hits.length);
        reader.close();
        searcher.close();
        writer = new IndexWriter(dir, new IndexWriterConfig(
            TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
            .setOpenMode(OpenMode.CREATE).setIndexDeletionPolicy(policy));
        writer.close();
      }
      assertEquals(1+3*(N+1), policy.numOnInit);
      assertEquals(3*(N+1), policy.numOnCommit);
      IndexSearcher searcher = new IndexSearcher(dir, false);
      ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
      assertEquals(0, hits.length);
      long gen = SegmentInfos.getCurrentSegmentGeneration(dir);
      dir.deleteFile(IndexFileNames.SEGMENTS_GEN);
      int expectedCount = 0;
      for(int i=0;i<N+1;i++) {
        try {
          IndexReader reader = IndexReader.open(dir, true);
          searcher = new IndexSearcher(reader);
          hits = searcher.search(query, null, 1000).scoreDocs;
          assertEquals(expectedCount, hits.length);
          searcher.close();
          if (expectedCount == 0) {
            expectedCount = 16;
          } else if (expectedCount == 16) {
            expectedCount = 17;
          } else if (expectedCount == 17) {
            expectedCount = 0;
          }
          reader.close();
          if (i == N) {
            fail("should have failed on commits before last " + N);
          }
        } catch (IOException e) {
          if (i != N) {
            throw e;
          }
        }
        if (i < N) {
          dir.deleteFile(IndexFileNames.fileNameFromGeneration(IndexFileNames.SEGMENTS, "", gen));
        }
        gen--;
      }
      dir.close();
    }
  }
  private void addDoc(IndexWriter writer) throws IOException
  {
    Document doc = new Document();
    doc.add(new Field("content", "aaa", Field.Store.NO, Field.Index.ANALYZED));
    writer.addDocument(doc);
  }
}
