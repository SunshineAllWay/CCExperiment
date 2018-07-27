package org.apache.lucene.index;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.store.NoSuchDirectoryException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
public class TestIndexReader extends LuceneTestCase
{
    public static void main(String args[]) {
        TestRunner.run (new TestSuite(TestIndexReader.class));
    }
    public TestIndexReader(String name) {
        super(name);
    }
    public void testCommitUserData() throws Exception {
      RAMDirectory d = new MockRAMDirectory();
      Map<String,String> commitUserData = new HashMap<String,String>();
      commitUserData.put("foo", "fighters");
      IndexWriter writer = new IndexWriter(d, new IndexWriterConfig(
          TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT))
      .setMaxBufferedDocs(2));
      for(int i=0;i<27;i++)
        addDocumentWithFields(writer);
      writer.close();
      IndexReader r = IndexReader.open(d, false);
      r.deleteDocument(5);
      r.flush(commitUserData);
      r.close();
      SegmentInfos sis = new SegmentInfos();
      sis.read(d);
      IndexReader r2 = IndexReader.open(d, false);
      IndexCommit c = r.getIndexCommit();
      assertEquals(c.getUserData(), commitUserData);
      assertEquals(sis.getCurrentSegmentFileName(), c.getSegmentsFileName());
      assertTrue(c.equals(r.getIndexCommit()));
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
          new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(
              OpenMode.APPEND).setMaxBufferedDocs(2));
      for(int i=0;i<7;i++)
        addDocumentWithFields(writer);
      writer.close();
      IndexReader r3 = r2.reopen();
      assertFalse(c.equals(r3.getIndexCommit()));
      assertFalse(r2.getIndexCommit().isOptimized());
      r3.close();
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
        new StandardAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND));
      writer.optimize();
      writer.close();
      r3 = r2.reopen();
      assertTrue(r3.getIndexCommit().isOptimized());
      r2.close();
      r3.close();
      d.close();
    }
    public void testIsCurrent() throws Exception {
      RAMDirectory d = new MockRAMDirectory();
      IndexWriter writer = new IndexWriter(d, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
      addDocumentWithFields(writer);
      writer.close();
      IndexReader reader = IndexReader.open(d, false);
      assertTrue(reader.isCurrent());
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
          new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
      addDocumentWithFields(writer);
      writer.close();
      assertFalse(reader.isCurrent());
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
          new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
      addDocumentWithFields(writer);
      writer.close();
      assertFalse(reader.isCurrent());
      reader.close();
      d.close();
    }
    public void testGetFieldNames() throws Exception {
        RAMDirectory d = new MockRAMDirectory();
        IndexWriter writer = new IndexWriter(d, new IndexWriterConfig(
            TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
        addDocumentWithFields(writer);
        writer.close();
        IndexReader reader = IndexReader.open(d, false);
        Collection<String> fieldNames = reader.getFieldNames(IndexReader.FieldOption.ALL);
        assertTrue(fieldNames.contains("keyword"));
        assertTrue(fieldNames.contains("text"));
        assertTrue(fieldNames.contains("unindexed"));
        assertTrue(fieldNames.contains("unstored"));
        reader.close();
        writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
            new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
        int mergeFactor = ((LogMergePolicy) writer.getMergePolicy()).getMergeFactor();
        for (int i = 0; i < 5*mergeFactor; i++) {
            addDocumentWithFields(writer);
        }
        for (int i = 0; i < 5*mergeFactor; i++) {
            addDocumentWithDifferentFields(writer);
        }
        for (int i = 0; i < 5*mergeFactor; i++) {
          addDocumentWithTermVectorFields(writer);
        }
        writer.close();
        reader = IndexReader.open(d, false);
        fieldNames = reader.getFieldNames(IndexReader.FieldOption.ALL);
        assertEquals(13, fieldNames.size());    
        assertTrue(fieldNames.contains("keyword"));
        assertTrue(fieldNames.contains("text"));
        assertTrue(fieldNames.contains("unindexed"));
        assertTrue(fieldNames.contains("unstored"));
        assertTrue(fieldNames.contains("keyword2"));
        assertTrue(fieldNames.contains("text2"));
        assertTrue(fieldNames.contains("unindexed2"));
        assertTrue(fieldNames.contains("unstored2"));
        assertTrue(fieldNames.contains("tvnot"));
        assertTrue(fieldNames.contains("termvector"));
        assertTrue(fieldNames.contains("tvposition"));
        assertTrue(fieldNames.contains("tvoffset"));
        assertTrue(fieldNames.contains("tvpositionoffset"));
        fieldNames = reader.getFieldNames(IndexReader.FieldOption.INDEXED);
        assertEquals(11, fieldNames.size());    
        assertTrue(fieldNames.contains("keyword"));
        assertTrue(fieldNames.contains("text"));
        assertTrue(fieldNames.contains("unstored"));
        assertTrue(fieldNames.contains("keyword2"));
        assertTrue(fieldNames.contains("text2"));
        assertTrue(fieldNames.contains("unstored2"));
        assertTrue(fieldNames.contains("tvnot"));
        assertTrue(fieldNames.contains("termvector"));
        assertTrue(fieldNames.contains("tvposition"));
        assertTrue(fieldNames.contains("tvoffset"));
        assertTrue(fieldNames.contains("tvpositionoffset"));
        fieldNames = reader.getFieldNames(IndexReader.FieldOption.UNINDEXED);
        assertEquals(2, fieldNames.size());    
        assertTrue(fieldNames.contains("unindexed"));
        assertTrue(fieldNames.contains("unindexed2"));
        fieldNames = reader.getFieldNames(IndexReader.FieldOption.TERMVECTOR);
        assertEquals(1, fieldNames.size());    
        assertTrue(fieldNames.contains("termvector"));
        fieldNames = reader.getFieldNames(IndexReader.FieldOption.TERMVECTOR_WITH_POSITION);
        assertEquals(1, fieldNames.size());    
        assertTrue(fieldNames.contains("tvposition"));
        fieldNames = reader.getFieldNames(IndexReader.FieldOption.TERMVECTOR_WITH_OFFSET);
        assertEquals(1, fieldNames.size());    
        assertTrue(fieldNames.contains("tvoffset"));
        fieldNames = reader.getFieldNames(IndexReader.FieldOption.TERMVECTOR_WITH_POSITION_OFFSET);
        assertEquals(1, fieldNames.size());    
        assertTrue(fieldNames.contains("tvpositionoffset"));
        reader.close();
        d.close();
    }
  public void testTermVectors() throws Exception {
    RAMDirectory d = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(d, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    int mergeFactor = ((LogMergePolicy) writer.getMergePolicy()).getMergeFactor();
    for (int i = 0; i < 5 * mergeFactor; i++) {
      Document doc = new Document();
        doc.add(new Field("tvnot","one two two three three three", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        doc.add(new Field("termvector","one two two three three three", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
        doc.add(new Field("tvoffset","one two two three three three", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_OFFSETS));
        doc.add(new Field("tvposition","one two two three three three", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
        doc.add(new Field("tvpositionoffset","one two two three three three", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        writer.addDocument(doc);
    }
    writer.close();
    IndexReader reader = IndexReader.open(d, false);
    FieldSortedTermVectorMapper mapper = new FieldSortedTermVectorMapper(new TermVectorEntryFreqSortedComparator());
    reader.getTermFreqVector(0, mapper);
    Map<String,SortedSet<TermVectorEntry>> map = mapper.getFieldToTerms();
    assertTrue("map is null and it shouldn't be", map != null);
    assertTrue("map Size: " + map.size() + " is not: " + 4, map.size() == 4);
    Set<TermVectorEntry> set = map.get("termvector");
    for (Iterator<TermVectorEntry> iterator = set.iterator(); iterator.hasNext();) {
      TermVectorEntry entry =  iterator.next();
      assertTrue("entry is null and it shouldn't be", entry != null);
      if (VERBOSE) System.out.println("Entry: " + entry);
    }
  }
  private void assertTermDocsCount(String msg,
                                     IndexReader reader,
                                     Term term,
                                     int expected)
    throws IOException
    {
        TermDocs tdocs = null;
        try {
            tdocs = reader.termDocs(term);
            assertNotNull(msg + ", null TermDocs", tdocs);
            int count = 0;
            while(tdocs.next()) {
                count++;
            }
            assertEquals(msg + ", count mismatch", expected, count);
        } finally {
            if (tdocs != null)
                tdocs.close();
        }
    }
    public void testBasicDelete() throws IOException {
        Directory dir = new MockRAMDirectory();
        IndexWriter writer = null;
        IndexReader reader = null;
        Term searchTerm = new Term("content", "aaa");
        writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        for (int i = 0; i < 100; i++) {
            addDoc(writer, searchTerm.text());
        }
        writer.close();
        reader = IndexReader.open(dir, false);
        assertEquals("first docFreq", 100, reader.docFreq(searchTerm));
        assertTermDocsCount("first reader", reader, searchTerm, 100);
        reader.close();
        int deleted = 0;
        reader = IndexReader.open(dir, false);
        deleted = reader.deleteDocuments(searchTerm);
        assertEquals("deleted count", 100, deleted);
        assertEquals("deleted docFreq", 100, reader.docFreq(searchTerm));
        assertTermDocsCount("deleted termDocs", reader, searchTerm, 0);
        IndexReader reader2 = IndexReader.open(dir, false);
        reader.close();
        reader = IndexReader.open(dir, false);
        assertEquals("deleted docFreq", 100, reader.docFreq(searchTerm));
        assertTermDocsCount("deleted termDocs", reader, searchTerm, 0);
        reader.close();
        reader2.close();
        dir.close();
    }
    public void testBinaryFields() throws IOException {
        Directory dir = new RAMDirectory();
        byte[] bin = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        for (int i = 0; i < 10; i++) {
          addDoc(writer, "document number " + (i + 1));
          addDocumentWithFields(writer);
          addDocumentWithDifferentFields(writer);
          addDocumentWithTermVectorFields(writer);
        }
        writer.close();
        writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
        Document doc = new Document();
        doc.add(new Field("bin1", bin));
        doc.add(new Field("junk", "junk text", Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();
        IndexReader reader = IndexReader.open(dir, false);
        doc = reader.document(reader.maxDoc() - 1);
        Field[] fields = doc.getFields("bin1");
        assertNotNull(fields);
        assertEquals(1, fields.length);
        Field b1 = fields[0];
        assertTrue(b1.isBinary());
        byte[] data1 = b1.getBinaryValue();
        assertEquals(bin.length, b1.getBinaryLength());
        for (int i = 0; i < bin.length; i++) {
          assertEquals(bin[i], data1[i + b1.getBinaryOffset()]);
        }
        Set<String> lazyFields = new HashSet<String>();
        lazyFields.add("bin1");
        FieldSelector sel = new SetBasedFieldSelector(new HashSet<String>(), lazyFields);
        doc = reader.document(reader.maxDoc() - 1, sel);
        Fieldable[] fieldables = doc.getFieldables("bin1");
        assertNotNull(fieldables);
        assertEquals(1, fieldables.length);
        Fieldable fb1 = fieldables[0];
        assertTrue(fb1.isBinary());
        assertEquals(bin.length, fb1.getBinaryLength());
        data1 = fb1.getBinaryValue();
        assertEquals(bin.length, fb1.getBinaryLength());
        for (int i = 0; i < bin.length; i++) {
          assertEquals(bin[i], data1[i + fb1.getBinaryOffset()]);
        }
        reader.close();
        writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
        writer.optimize();
        writer.close();
        reader = IndexReader.open(dir, false);
        doc = reader.document(reader.maxDoc() - 1);
        fields = doc.getFields("bin1");
        assertNotNull(fields);
        assertEquals(1, fields.length);
        b1 = fields[0];
        assertTrue(b1.isBinary());
        data1 = b1.getBinaryValue();
        assertEquals(bin.length, b1.getBinaryLength());
        for (int i = 0; i < bin.length; i++) {
          assertEquals(bin[i], data1[i + b1.getBinaryOffset()]);
        }
        reader.close();
    }
    public void testChangesAfterClose() throws IOException {
        Directory dir = new RAMDirectory();
        IndexWriter writer = null;
        IndexReader reader = null;
        Term searchTerm = new Term("content", "aaa");
        writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        for (int i = 0; i < 11; i++) {
            addDoc(writer, searchTerm.text());
        }
        writer.close();
        reader = IndexReader.open(dir, false);
        reader.close();
        try {
          reader.deleteDocument(4);
          fail("deleteDocument after close failed to throw IOException");
        } catch (AlreadyClosedException e) {
        }
        try {
          reader.setNorm(5, "aaa", 2.0f);
          fail("setNorm after close failed to throw IOException");
        } catch (AlreadyClosedException e) {
        }
        try {
          reader.undeleteAll();
          fail("undeleteAll after close failed to throw IOException");
        } catch (AlreadyClosedException e) {
        }
    }
    public void testLockObtainFailed() throws IOException {
        Directory dir = new RAMDirectory();
        IndexWriter writer = null;
        IndexReader reader = null;
        Term searchTerm = new Term("content", "aaa");
        writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        for (int i = 0; i < 11; i++) {
            addDoc(writer, searchTerm.text());
        }
        reader = IndexReader.open(dir, false);
        try {
          reader.deleteDocument(4);
          fail("deleteDocument should have hit LockObtainFailedException");
        } catch (LockObtainFailedException e) {
        }
        try {
          reader.setNorm(5, "aaa", 2.0f);
          fail("setNorm should have hit LockObtainFailedException");
        } catch (LockObtainFailedException e) {
        }
        try {
          reader.undeleteAll();
          fail("undeleteAll should have hit LockObtainFailedException");
        } catch (LockObtainFailedException e) {
        }
        writer.close();
        reader.close();
    }
    public void testWritingNorms() throws IOException {
        File indexDir = new File(TEMP_DIR, "lucenetestnormwriter");
        Directory dir = FSDirectory.open(indexDir);
        IndexWriter writer;
        IndexReader reader;
        Term searchTerm = new Term("content", "aaa");
        writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        addDoc(writer, searchTerm.text());
        writer.close();
        reader = IndexReader.open(dir, false);
        reader.setNorm(0, "content", (float) 2.0);
        assertTrue("locked", IndexWriter.isLocked(dir));
        reader.commit();
        assertTrue("not locked", !IndexWriter.isLocked(dir));
        IndexReader reader2 = IndexReader.open(dir, false);
        reader.setNorm(0, "content", (float) 3.0);
        assertTrue("locked", IndexWriter.isLocked(dir));
        reader.close();
        assertTrue("not locked", !IndexWriter.isLocked(dir));
        reader2.close();
        dir.close();
        rmDir(indexDir);
    }
    public void testWritingNormsNoReader() throws IOException {
        Directory dir = new MockRAMDirectory();
        IndexWriter writer = null;
        IndexReader reader = null;
        Term searchTerm = new Term("content", "aaa");
        writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false);
        ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false);
        addDoc(writer, searchTerm.text());
        writer.close();
        reader = IndexReader.open(dir, false);
        reader.setNorm(0, "content", (float) 2.0);
        reader.close();
        reader = IndexReader.open(dir, false);
        reader.setNorm(0, "content", (float) 2.0);
        reader.close();
        assertFalse("failed to remove first generation norms file on writing second generation",
                    dir.fileExists("_0_1.s0"));
        dir.close();
    }
    public void testDeleteReaderWriterConflictUnoptimized() throws IOException{
      deleteReaderWriterConflict(false);
    }
    public void testDeleteReaderWriterConflictOptimized() throws IOException{
        deleteReaderWriterConflict(true);
    }
    private void deleteReaderWriterConflict(boolean optimize) throws IOException {
        Directory dir = getDirectory();
        Term searchTerm = new Term("content", "aaa");
        Term searchTerm2 = new Term("content", "bbb");
        IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
        for (int i = 0; i < 100; i++) {
            addDoc(writer, searchTerm.text());
        }
        writer.close();
        IndexReader reader = IndexReader.open(dir, false);
        assertEquals("first docFreq", 100, reader.docFreq(searchTerm));
        assertEquals("first docFreq", 0, reader.docFreq(searchTerm2));
        assertTermDocsCount("first reader", reader, searchTerm, 100);
        assertTermDocsCount("first reader", reader, searchTerm2, 0);
        writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
        for (int i = 0; i < 100; i++) {
            addDoc(writer, searchTerm2.text());
        }
        if(optimize)
          writer.optimize();
        writer.close();
        assertEquals("first docFreq", 100, reader.docFreq(searchTerm));
        assertEquals("first docFreq", 0, reader.docFreq(searchTerm2));
        assertTermDocsCount("first reader", reader, searchTerm, 100);
        assertTermDocsCount("first reader", reader, searchTerm2, 0);
        int deleted = 0;
        try {
            deleted = reader.deleteDocuments(searchTerm);
            fail("Delete allowed on an index reader with stale segment information");
        } catch (StaleReaderException e) {
        }
        reader.close();
        reader = IndexReader.open(dir, false);
        assertEquals("first docFreq", 100, reader.docFreq(searchTerm));
        assertEquals("first docFreq", 100, reader.docFreq(searchTerm2));
        assertTermDocsCount("first reader", reader, searchTerm, 100);
        assertTermDocsCount("first reader", reader, searchTerm2, 100);
        deleted = reader.deleteDocuments(searchTerm);
        assertEquals("deleted count", 100, deleted);
        assertEquals("deleted docFreq", 100, reader.docFreq(searchTerm));
        assertEquals("deleted docFreq", 100, reader.docFreq(searchTerm2));
        assertTermDocsCount("deleted termDocs", reader, searchTerm, 0);
        assertTermDocsCount("deleted termDocs", reader, searchTerm2, 100);
        reader.close();
        reader = IndexReader.open(dir, false);
        assertEquals("deleted docFreq", 100, reader.docFreq(searchTerm));
        assertEquals("deleted docFreq", 100, reader.docFreq(searchTerm2));
        assertTermDocsCount("deleted termDocs", reader, searchTerm, 0);
        assertTermDocsCount("deleted termDocs", reader, searchTerm2, 100);
        reader.close();
    }
  private Directory getDirectory() throws IOException {
    return FSDirectory.open(new File(TEMP_DIR, "testIndex"));
  }
  public void testFilesOpenClose() throws IOException {
        File dirFile = new File(TEMP_DIR, "testIndex");
        Directory dir = getDirectory();
        IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        addDoc(writer, "test");
        writer.close();
        dir.close();
        _TestUtil.rmDir(dirFile);
        dir = getDirectory();
        writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
        addDoc(writer, "test");
        writer.close();
        dir.close();
        dir = getDirectory();
        IndexReader reader1 = IndexReader.open(dir, false);
        reader1.close();
        dir.close();
        _TestUtil.rmDir(dirFile);
    }
    public void testLastModified() throws Exception {
      final File fileDir = new File(TEMP_DIR, "testIndex");
      for(int i=0;i<2;i++) {
        try {
          final Directory dir;
          if (0 == i)
            dir = new MockRAMDirectory();
          else
            dir = getDirectory();
          assertFalse(IndexReader.indexExists(dir));
          IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
          addDocumentWithFields(writer);
          assertTrue(IndexWriter.isLocked(dir));		
          writer.close();
          assertTrue(IndexReader.indexExists(dir));
          IndexReader reader = IndexReader.open(dir, false);
          assertFalse(IndexWriter.isLocked(dir));		
          long version = IndexReader.lastModified(dir);
          if (i == 1) {
            long version2 = IndexReader.lastModified(dir);
            assertEquals(version, version2);
          }
          reader.close();
          Thread.sleep(1000);
          writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
          addDocumentWithFields(writer);
          writer.close();
          reader = IndexReader.open(dir, false);
          assertTrue("old lastModified is " + version + "; new lastModified is " + IndexReader.lastModified(dir), version <= IndexReader.lastModified(dir));
          reader.close();
          dir.close();
        } finally {
          if (i == 1)
            _TestUtil.rmDir(fileDir);
        }
      }
    }
    public void testVersion() throws IOException {
      Directory dir = new MockRAMDirectory();
      assertFalse(IndexReader.indexExists(dir));
      IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      addDocumentWithFields(writer);
      assertTrue(IndexWriter.isLocked(dir));		
      writer.close();
      assertTrue(IndexReader.indexExists(dir));
      IndexReader reader = IndexReader.open(dir, false);
      assertFalse(IndexWriter.isLocked(dir));		
      long version = IndexReader.getCurrentVersion(dir);
      reader.close();
      writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
      addDocumentWithFields(writer);
      writer.close();
      reader = IndexReader.open(dir, false);
      assertTrue("old version is " + version + "; new version is " + IndexReader.getCurrentVersion(dir), version < IndexReader.getCurrentVersion(dir));
      reader.close();
      dir.close();
    }
    public void testLock() throws IOException {
      Directory dir = new MockRAMDirectory();
      IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      addDocumentWithFields(writer);
      writer.close();
      writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
      IndexReader reader = IndexReader.open(dir, false);
      try {
        reader.deleteDocument(0);
        fail("expected lock");
      } catch(IOException e) {
      }
      IndexWriter.unlock(dir);		
      reader.deleteDocument(0);
      reader.close();
      writer.close();
      dir.close();
    }
    public void testUndeleteAll() throws IOException {
      Directory dir = new MockRAMDirectory();
      IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      addDocumentWithFields(writer);
      addDocumentWithFields(writer);
      writer.close();
      IndexReader reader = IndexReader.open(dir, false);
      reader.deleteDocument(0);
      reader.deleteDocument(1);
      reader.undeleteAll();
      reader.close();
      reader = IndexReader.open(dir, false);
      assertEquals(2, reader.numDocs());	
      reader.close();
      dir.close();
    }
    public void testUndeleteAllAfterClose() throws IOException {
      Directory dir = new MockRAMDirectory();
      IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      addDocumentWithFields(writer);
      addDocumentWithFields(writer);
      writer.close();
      IndexReader reader = IndexReader.open(dir, false);
      reader.deleteDocument(0);
      reader.deleteDocument(1);
      reader.close();
      reader = IndexReader.open(dir, false);
      reader.undeleteAll();
      assertEquals(2, reader.numDocs());	
      reader.close();
      dir.close();
    }
    public void testUndeleteAllAfterCloseThenReopen() throws IOException {
      Directory dir = new MockRAMDirectory();
      IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      addDocumentWithFields(writer);
      addDocumentWithFields(writer);
      writer.close();
      IndexReader reader = IndexReader.open(dir, false);
      reader.deleteDocument(0);
      reader.deleteDocument(1);
      reader.close();
      reader = IndexReader.open(dir, false);
      reader.undeleteAll();
      reader.close();
      reader = IndexReader.open(dir, false);
      assertEquals(2, reader.numDocs());	
      reader.close();
      dir.close();
    }
    public void testDeleteReaderReaderConflictUnoptimized() throws IOException{
      deleteReaderReaderConflict(false);
    }
    public void testDeleteReaderReaderConflictOptimized() throws IOException{
      deleteReaderReaderConflict(true);
    }
    public void testDiskFull() throws IOException {
      Term searchTerm = new Term("content", "aaa");
      int START_COUNT = 157;
      int END_COUNT = 144;
      RAMDirectory startDir = new MockRAMDirectory();
      IndexWriter writer = new IndexWriter(startDir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      for(int i=0;i<157;i++) {
        Document d = new Document();
        d.add(new Field("id", Integer.toString(i), Field.Store.YES, Field.Index.NOT_ANALYZED));
        d.add(new Field("content", "aaa " + i, Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(d);
      }
      writer.close();
      long diskUsage = startDir.sizeInBytes();
      long diskFree = diskUsage+100;      
      IOException err = null;
      boolean done = false;
      while(!done) {
        MockRAMDirectory dir = new MockRAMDirectory(startDir);
        dir.setPreventDoubleWrite(false);
        IndexReader reader = IndexReader.open(dir, false);
        boolean success = false;
        for(int x=0;x<2;x++) {
          double rate = 0.05;
          double diskRatio = ((double) diskFree)/diskUsage;
          long thisDiskFree;
          String testName;
          if (0 == x) {
            thisDiskFree = diskFree;
            if (diskRatio >= 2.0) {
              rate /= 2;
            }
            if (diskRatio >= 4.0) {
              rate /= 2;
            }
            if (diskRatio >= 6.0) {
              rate = 0.0;
            }
            if (VERBOSE) {
              System.out.println("\ncycle: " + diskFree + " bytes");
            }
            testName = "disk full during reader.close() @ " + thisDiskFree + " bytes";
          } else {
            thisDiskFree = 0;
            rate = 0.0;
            if (VERBOSE) {
              System.out.println("\ncycle: same writer: unlimited disk space");
            }
            testName = "reader re-use after disk full";
          }
          dir.setMaxSizeInBytes(thisDiskFree);
          dir.setRandomIOExceptionRate(rate, diskFree);
          try {
            if (0 == x) {
              int docId = 12;
              for(int i=0;i<13;i++) {
                reader.deleteDocument(docId);
                reader.setNorm(docId, "contents", (float) 2.0);
                docId += 12;
              }
            }
            reader.close();
            success = true;
            if (0 == x) {
              done = true;
            }
          } catch (IOException e) {
            if (VERBOSE) {
              System.out.println("  hit IOException: " + e);
              e.printStackTrace(System.out);
            }
            err = e;
            if (1 == x) {
              e.printStackTrace();
              fail(testName + " hit IOException after disk space was freed up");
            }
          }
          String[] startFiles = dir.listAll();
          SegmentInfos infos = new SegmentInfos();
          infos.read(dir);
          new IndexFileDeleter(dir, new KeepOnlyLastCommitDeletionPolicy(), infos, null, null);
          String[] endFiles = dir.listAll();
          Arrays.sort(startFiles);
          Arrays.sort(endFiles);
          if (!Arrays.equals(startFiles, endFiles)) {
            String successStr;
            if (success) {
              successStr = "success";
            } else {
              successStr = "IOException";
              err.printStackTrace();
            }
            fail("reader.close() failed to delete unreferenced files after " + successStr + " (" + diskFree + " bytes): before delete:\n    " + arrayToString(startFiles) + "\n  after delete:\n    " + arrayToString(endFiles));
          }
          IndexReader newReader = null;
          try {
            newReader = IndexReader.open(dir, false);
          } catch (IOException e) {
            e.printStackTrace();
            fail(testName + ":exception when creating IndexReader after disk full during close: " + e);
          }
          IndexSearcher searcher = new IndexSearcher(newReader);
          ScoreDoc[] hits = null;
          try {
            hits = searcher.search(new TermQuery(searchTerm), null, 1000).scoreDocs;
          } catch (IOException e) {
            e.printStackTrace();
            fail(testName + ": exception when searching: " + e);
          }
          int result2 = hits.length;
          if (success) {
            if (result2 != END_COUNT) {
              fail(testName + ": method did not throw exception but hits.length for search on term 'aaa' is " + result2 + " instead of expected " + END_COUNT);
            }
          } else {
            if (result2 != START_COUNT && result2 != END_COUNT) {
              err.printStackTrace();
              fail(testName + ": method did throw exception but hits.length for search on term 'aaa' is " + result2 + " instead of expected " + START_COUNT);
            }
          }
          searcher.close();
          newReader.close();
          if (result2 == END_COUNT) {
            break;
          }
        }
        dir.close();
        diskFree += 10;
      }
      startDir.close();
    }
    public void testDocsOutOfOrderJIRA140() throws IOException {
      Directory dir = new MockRAMDirectory();      
      IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      for(int i=0;i<11;i++) {
        addDoc(writer, "aaa");
      }
      writer.close();
      IndexReader reader = IndexReader.open(dir, false);
      boolean gotException = false;
      try {
        reader.deleteDocument(11);
      } catch (ArrayIndexOutOfBoundsException e) {
        gotException = true;
      }
      reader.close();
      writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
      for(int i=0;i<11;i++) {
        addDoc(writer, "aaa");
      }
      writer.optimize();
      writer.close();
      if (!gotException) {
        fail("delete of out-of-bounds doc number failed to hit exception");
      }
      dir.close();
    }
    public void testExceptionReleaseWriteLockJIRA768() throws IOException {
      Directory dir = new MockRAMDirectory();      
      IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      addDoc(writer, "aaa");
      writer.close();
      IndexReader reader = IndexReader.open(dir, false);
      try {
        reader.deleteDocument(1);
        fail("did not hit exception when deleting an invalid doc number");
      } catch (ArrayIndexOutOfBoundsException e) {
      }
      reader.close();
      if (IndexWriter.isLocked(dir)) {
        fail("write lock is still held after close");
      }
      reader = IndexReader.open(dir, false);
      try {
        reader.setNorm(1, "content", (float) 2.0);
        fail("did not hit exception when calling setNorm on an invalid doc number");
      } catch (ArrayIndexOutOfBoundsException e) {
      }
      reader.close();
      if (IndexWriter.isLocked(dir)) {
        fail("write lock is still held after close");
      }
      dir.close();
    }
    private String arrayToString(String[] l) {
      String s = "";
      for(int i=0;i<l.length;i++) {
        if (i > 0) {
          s += "\n    ";
        }
        s += l[i];
      }
      return s;
    }
    public void testOpenReaderAfterDelete() throws IOException {
      File dirFile = new File(TEMP_DIR, "deletetest");
      Directory dir = FSDirectory.open(dirFile);
      try {
        IndexReader.open(dir, false);
        fail("expected FileNotFoundException");
      } catch (FileNotFoundException e) {
      }
      dirFile.delete();
      try {
        IndexReader.open(dir, false);
        fail("expected FileNotFoundException");
      } catch (FileNotFoundException e) {
      }
      dir.close();
    }
    private void deleteReaderReaderConflict(boolean optimize) throws IOException {
        Directory dir = getDirectory();
        Term searchTerm1 = new Term("content", "aaa");
        Term searchTerm2 = new Term("content", "bbb");
        Term searchTerm3 = new Term("content", "ccc");
        IndexWriter writer  = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
        for (int i = 0; i < 100; i++) {
            addDoc(writer, searchTerm1.text());
            addDoc(writer, searchTerm2.text());
            addDoc(writer, searchTerm3.text());
        }
        if(optimize)
          writer.optimize();
        writer.close();
        IndexReader reader1 = IndexReader.open(dir, false);
        assertEquals("first opened", 100, reader1.docFreq(searchTerm1));
        assertEquals("first opened", 100, reader1.docFreq(searchTerm2));
        assertEquals("first opened", 100, reader1.docFreq(searchTerm3));
        assertTermDocsCount("first opened", reader1, searchTerm1, 100);
        assertTermDocsCount("first opened", reader1, searchTerm2, 100);
        assertTermDocsCount("first opened", reader1, searchTerm3, 100);
        IndexReader reader2 = IndexReader.open(dir, false);
        assertEquals("first opened", 100, reader2.docFreq(searchTerm1));
        assertEquals("first opened", 100, reader2.docFreq(searchTerm2));
        assertEquals("first opened", 100, reader2.docFreq(searchTerm3));
        assertTermDocsCount("first opened", reader2, searchTerm1, 100);
        assertTermDocsCount("first opened", reader2, searchTerm2, 100);
        assertTermDocsCount("first opened", reader2, searchTerm3, 100);
        reader2.deleteDocuments(searchTerm1);
        assertEquals("after delete 1", 100, reader2.docFreq(searchTerm1));
        assertEquals("after delete 1", 100, reader2.docFreq(searchTerm2));
        assertEquals("after delete 1", 100, reader2.docFreq(searchTerm3));
        assertTermDocsCount("after delete 1", reader2, searchTerm1, 0);
        assertTermDocsCount("after delete 1", reader2, searchTerm2, 100);
        assertTermDocsCount("after delete 1", reader2, searchTerm3, 100);
        reader2.close();
        assertEquals("after delete 1", 100, reader1.docFreq(searchTerm1));
        assertEquals("after delete 1", 100, reader1.docFreq(searchTerm2));
        assertEquals("after delete 1", 100, reader1.docFreq(searchTerm3));
        assertTermDocsCount("after delete 1", reader1, searchTerm1, 100);
        assertTermDocsCount("after delete 1", reader1, searchTerm2, 100);
        assertTermDocsCount("after delete 1", reader1, searchTerm3, 100);
        try {
            reader1.deleteDocuments(searchTerm2);
            fail("Delete allowed from a stale index reader");
        } catch (IOException e) {
        }
        reader1.close();
        reader1 = IndexReader.open(dir, false);
        assertEquals("reopened", 100, reader1.docFreq(searchTerm1));
        assertEquals("reopened", 100, reader1.docFreq(searchTerm2));
        assertEquals("reopened", 100, reader1.docFreq(searchTerm3));
        assertTermDocsCount("reopened", reader1, searchTerm1, 0);
        assertTermDocsCount("reopened", reader1, searchTerm2, 100);
        assertTermDocsCount("reopened", reader1, searchTerm3, 100);
        reader1.deleteDocuments(searchTerm2);
        assertEquals("deleted 2", 100, reader1.docFreq(searchTerm1));
        assertEquals("deleted 2", 100, reader1.docFreq(searchTerm2));
        assertEquals("deleted 2", 100, reader1.docFreq(searchTerm3));
        assertTermDocsCount("deleted 2", reader1, searchTerm1, 0);
        assertTermDocsCount("deleted 2", reader1, searchTerm2, 0);
        assertTermDocsCount("deleted 2", reader1, searchTerm3, 100);
        reader1.close();
        reader2 = IndexReader.open(dir, false);
        assertEquals("reopened 2", 100, reader2.docFreq(searchTerm1));
        assertEquals("reopened 2", 100, reader2.docFreq(searchTerm2));
        assertEquals("reopened 2", 100, reader2.docFreq(searchTerm3));
        assertTermDocsCount("reopened 2", reader2, searchTerm1, 0);
        assertTermDocsCount("reopened 2", reader2, searchTerm2, 0);
        assertTermDocsCount("reopened 2", reader2, searchTerm3, 100);
        reader2.close();
        dir.close();
    }
    private void addDocumentWithFields(IndexWriter writer) throws IOException
    {
        Document doc = new Document();
        doc.add(new Field("keyword","test1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("text","test1", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("unindexed","test1", Field.Store.YES, Field.Index.NO));
        doc.add(new Field("unstored","test1", Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(doc);
    }
    private void addDocumentWithDifferentFields(IndexWriter writer) throws IOException
    {
        Document doc = new Document();
        doc.add(new Field("keyword2","test1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("text2","test1", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("unindexed2","test1", Field.Store.YES, Field.Index.NO));
        doc.add(new Field("unstored2","test1", Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(doc);
    }
    private void addDocumentWithTermVectorFields(IndexWriter writer) throws IOException
    {
        Document doc = new Document();
        doc.add(new Field("tvnot","tvnot", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        doc.add(new Field("termvector","termvector", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
        doc.add(new Field("tvoffset","tvoffset", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_OFFSETS));
        doc.add(new Field("tvposition","tvposition", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
        doc.add(new Field("tvpositionoffset","tvpositionoffset", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        writer.addDocument(doc);
    }
    private void addDoc(IndexWriter writer, String value) throws IOException
    {
        Document doc = new Document();
        doc.add(new Field("content", value, Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(doc);
    }
    private void rmDir(File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        dir.delete();
    }
    public static void assertIndexEquals(IndexReader index1, IndexReader index2) throws IOException {
      assertEquals("IndexReaders have different values for numDocs.", index1.numDocs(), index2.numDocs());
      assertEquals("IndexReaders have different values for maxDoc.", index1.maxDoc(), index2.maxDoc());
      assertEquals("Only one IndexReader has deletions.", index1.hasDeletions(), index2.hasDeletions());
      assertEquals("Only one index is optimized.", index1.isOptimized(), index2.isOptimized());
      Collection<String> fields1 = index1.getFieldNames(FieldOption.ALL);
      Collection<String> fields2 = index1.getFieldNames(FieldOption.ALL);
      assertEquals("IndexReaders have different numbers of fields.", fields1.size(), fields2.size());
      Iterator<String> it1 = fields1.iterator();
      Iterator<String> it2 = fields1.iterator();
      while (it1.hasNext()) {
        assertEquals("Different field names.", it1.next(), it2.next());
      }
      it1 = fields1.iterator();
      while (it1.hasNext()) {
        String curField = it1.next();
        byte[] norms1 = index1.norms(curField);
        byte[] norms2 = index2.norms(curField);
        if (norms1 != null && norms2 != null)
        {
          assertEquals(norms1.length, norms2.length);
	        for (int i = 0; i < norms1.length; i++) {
	          assertEquals("Norm different for doc " + i + " and field '" + curField + "'.", norms1[i], norms2[i]);
	        }
        }
        else
        {
          assertSame(norms1, norms2);
        }
      }
      for (int i = 0; i < index1.maxDoc(); i++) {
        assertEquals("Doc " + i + " only deleted in one index.", index1.isDeleted(i), index2.isDeleted(i));
      }
      for (int i = 0; i < index1.maxDoc(); i++) {
        if (!index1.isDeleted(i)) {
          Document doc1 = index1.document(i);
          Document doc2 = index2.document(i);
          List<Fieldable> fieldable1 = doc1.getFields();
          List<Fieldable> fieldable2 = doc2.getFields();
          assertEquals("Different numbers of fields for doc " + i + ".", fieldable1.size(), fieldable2.size());
          Iterator<Fieldable> itField1 = fieldable1.iterator();
          Iterator<Fieldable> itField2 = fieldable2.iterator();
          while (itField1.hasNext()) {
            Field curField1 = (Field) itField1.next();
            Field curField2 = (Field) itField2.next();
            assertEquals("Different fields names for doc " + i + ".", curField1.name(), curField2.name());
            assertEquals("Different field values for doc " + i + ".", curField1.stringValue(), curField2.stringValue());
          }          
        }
      }
      TermEnum enum1 = index1.terms();
      TermEnum enum2 = index2.terms();
      TermPositions tp1 = index1.termPositions();
      TermPositions tp2 = index2.termPositions();
      while(enum1.next()) {
        assertTrue(enum2.next());
        assertEquals("Different term in dictionary.", enum1.term(), enum2.term());
        tp1.seek(enum1.term());
        tp2.seek(enum1.term());
        while(tp1.next()) {
          assertTrue(tp2.next());
          assertEquals("Different doc id in postinglist of term " + enum1.term() + ".", tp1.doc(), tp2.doc());
          assertEquals("Different term frequence in postinglist of term " + enum1.term() + ".", tp1.freq(), tp2.freq());
          for (int i = 0; i < tp1.freq(); i++) {
            assertEquals("Different positions in postinglist of term " + enum1.term() + ".", tp1.nextPosition(), tp2.nextPosition());
          }
        }
      }
    }
    public void testGetIndexCommit() throws IOException {
      RAMDirectory d = new MockRAMDirectory();
      IndexWriter writer = new IndexWriter(d, new IndexWriterConfig(
          TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT))
      .setMaxBufferedDocs(2));
      for(int i=0;i<27;i++)
        addDocumentWithFields(writer);
      writer.close();
      SegmentInfos sis = new SegmentInfos();
      sis.read(d);
      IndexReader r = IndexReader.open(d, false);
      IndexCommit c = r.getIndexCommit();
      assertEquals(sis.getCurrentSegmentFileName(), c.getSegmentsFileName());
      assertTrue(c.equals(r.getIndexCommit()));
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
          new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(
              OpenMode.APPEND).setMaxBufferedDocs(2));
      for(int i=0;i<7;i++)
        addDocumentWithFields(writer);
      writer.close();
      IndexReader r2 = r.reopen();
      assertFalse(c.equals(r2.getIndexCommit()));
      assertFalse(r2.getIndexCommit().isOptimized());
      r2.close();
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
        new StandardAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND));
      writer.optimize();
      writer.close();
      r2 = r.reopen();
      assertTrue(r2.getIndexCommit().isOptimized());
      r.close();
      r2.close();
      d.close();
    }      
    public void testReadOnly() throws Throwable {
      RAMDirectory d = new MockRAMDirectory();
      IndexWriter writer = new IndexWriter(d, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
      addDocumentWithFields(writer);
      writer.commit();
      addDocumentWithFields(writer);
      writer.close();
      IndexReader r = IndexReader.open(d, true);
      try {
        r.deleteDocument(0);
        fail();
      } catch (UnsupportedOperationException uoe) {
      }
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
        new StandardAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND));
      addDocumentWithFields(writer);
      writer.close();
      IndexReader r2 = r.reopen();
      r.close();
      assertFalse(r == r2);
      try {
        r2.deleteDocument(0);
        fail();
      } catch (UnsupportedOperationException uoe) {
      }
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
        new StandardAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.APPEND));
      writer.optimize();
      writer.close();
      IndexReader r3 = r2.reopen();
      r2.close();
      assertFalse(r == r2);
      try {
        r3.deleteDocument(0);
        fail();
      } catch (UnsupportedOperationException uoe) {
      }
      writer = new IndexWriter(d, new IndexWriterConfig(TEST_VERSION_CURRENT,
          new StandardAnalyzer(TEST_VERSION_CURRENT))
      .setOpenMode(OpenMode.APPEND));
      writer.close();
      r3.close();
    }
  public void testIndexReader() throws Exception {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    writer.addDocument(createDocument("a"));
    writer.addDocument(createDocument("b"));
    writer.addDocument(createDocument("c"));
    writer.close();
    IndexReader reader = IndexReader.open(dir, false);
    reader.deleteDocuments(new Term("id", "a"));
    reader.flush();
    reader.deleteDocuments(new Term("id", "b"));
    reader.close();
    IndexReader.open(dir,true).close();
  }
  public void testIndexReaderUnDeleteAll() throws Exception {
    MockRAMDirectory dir = new MockRAMDirectory();
    dir.setPreventDoubleWrite(false);
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    writer.addDocument(createDocument("a"));
    writer.addDocument(createDocument("b"));
    writer.addDocument(createDocument("c"));
    writer.close();
    IndexReader reader = IndexReader.open(dir, false);
    reader.deleteDocuments(new Term("id", "a"));
    reader.flush();
    reader.deleteDocuments(new Term("id", "b"));
    reader.undeleteAll();
    reader.deleteDocuments(new Term("id", "b"));
    reader.close();
    IndexReader.open(dir,true).close();
    dir.close();
  }
  private Document createDocument(String id) {
    Document doc = new Document();
    doc.add(new Field("id", id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    return doc;
  }
  public void testNoDir() throws Throwable {
    Directory dir = FSDirectory.open(_TestUtil.getTempDir("doesnotexist"));
    try {
      IndexReader.open(dir, true);
      fail("did not hit expected exception");
    } catch (NoSuchDirectoryException nsde) {
    }
    dir.close();
  }
  public void testNoDupCommitFileNames() throws Throwable {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT))
        .setMaxBufferedDocs(2));
    writer.addDocument(createDocument("a"));
    writer.addDocument(createDocument("a"));
    writer.addDocument(createDocument("a"));
    writer.close();
    Collection<IndexCommit> commits = IndexReader.listCommits(dir);
    for (final IndexCommit commit : commits) {
      Collection<String> files = commit.getFileNames();
      HashSet<String> seen = new HashSet<String>();
      for (final String fileName : files) { 
        assertTrue("file " + fileName + " was duplicated", !seen.contains(fileName));
        seen.add(fileName);
      }
    }
    dir.close();
  }
  public void testFieldCacheReuseAfterClone() throws Exception {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("number", "17", Field.Store.NO, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
    writer.close();
    IndexReader r = SegmentReader.getOnlySegmentReader(dir);
    final int[] ints = FieldCache.DEFAULT.getInts(r, "number");
    assertEquals(1, ints.length);
    assertEquals(17, ints[0]);
    IndexReader r2 = (IndexReader) r.clone();
    r.close();
    assertTrue(r2 != r);
    final int[] ints2 = FieldCache.DEFAULT.getInts(r2, "number");
    r2.close();
    assertEquals(1, ints2.length);
    assertEquals(17, ints2[0]);
    assertTrue(ints == ints2);
    dir.close();
  }
  public void testFieldCacheReuseAfterReopen() throws Exception {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("number", "17", Field.Store.NO, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
    writer.commit();
    IndexReader r = IndexReader.open(dir, false);
    IndexReader r1 = SegmentReader.getOnlySegmentReader(r);
    final int[] ints = FieldCache.DEFAULT.getInts(r1, "number");
    assertEquals(1, ints.length);
    assertEquals(17, ints[0]);
    writer.addDocument(doc);
    writer.commit();
    IndexReader r2 = r.reopen();
    r.close();
    IndexReader sub0 = r2.getSequentialSubReaders()[0];
    final int[] ints2 = FieldCache.DEFAULT.getInts(sub0, "number");
    r2.close();
    assertTrue(ints == ints2);
    dir.close();
  }
  public void testReopenChangeReadonly() throws Exception {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("number", "17", Field.Store.NO, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
    writer.commit();
    IndexReader r = IndexReader.open(dir, false);
    assertTrue(r instanceof DirectoryReader);
    IndexReader r1 = SegmentReader.getOnlySegmentReader(r);
    final int[] ints = FieldCache.DEFAULT.getInts(r1, "number");
    assertEquals(1, ints.length);
    assertEquals(17, ints[0]);
    IndexReader r3 = r.reopen(true);
    assertTrue(r3 instanceof ReadOnlyDirectoryReader);
    r3.close();
    writer.addDocument(doc);
    writer.commit();
    IndexReader r2 = r.reopen(true);
    r.close();
    assertTrue(r2 instanceof ReadOnlyDirectoryReader);
    IndexReader[] subs = r2.getSequentialSubReaders();
    final int[] ints2 = FieldCache.DEFAULT.getInts(subs[0], "number");
    r2.close();
    assertTrue(subs[0] instanceof ReadOnlySegmentReader);
    assertTrue(subs[1] instanceof ReadOnlySegmentReader);
    assertTrue(ints == ints2);
    dir.close();
  }
  public void testUniqueTermCount() throws Exception {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("field", "a b c d e f g h i j k l m n o p q r s t u v w x y z", Field.Store.NO, Field.Index.ANALYZED));
    doc.add(new Field("number", "0 1 2 3 4 5 6 7 8 9", Field.Store.NO, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.addDocument(doc);
    writer.commit();
    IndexReader r = IndexReader.open(dir, false);
    IndexReader r1 = SegmentReader.getOnlySegmentReader(r);
    assertEquals(36, r1.getUniqueTermCount());
    writer.addDocument(doc);
    writer.commit();
    IndexReader r2 = r.reopen();
    r.close();
    try {
      r2.getUniqueTermCount();
      fail("expected exception");
    } catch (UnsupportedOperationException uoe) {
    }
    IndexReader[] subs = r2.getSequentialSubReaders();
    for(int i=0;i<subs.length;i++) {
      assertEquals(36, subs[i].getUniqueTermCount());
    }
    r2.close();
    writer.close();
    dir.close();
  }
  public void testNoTermsIndex() throws Throwable {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("field", "a b c d e f g h i j k l m n o p q r s t u v w x y z", Field.Store.NO, Field.Index.ANALYZED));
    doc.add(new Field("number", "0 1 2 3 4 5 6 7 8 9", Field.Store.NO, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.addDocument(doc);
    writer.close();
    IndexReader r = IndexReader.open(dir, null, true, -1);
    try {
      r.docFreq(new Term("field", "f"));
      fail("did not hit expected exception");
    } catch (IllegalStateException ise) {
    }
    assertFalse(((SegmentReader) r.getSequentialSubReaders()[0]).termsIndexLoaded());
    assertEquals(-1, ((SegmentReader) r.getSequentialSubReaders()[0]).getTermInfosIndexDivisor());
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    writer.addDocument(doc);
    writer.close();
    IndexReader r2 = r.reopen();
    r.close();
    IndexReader[] subReaders = r2.getSequentialSubReaders();
    assertEquals(2, subReaders.length);
    for(int i=0;i<2;i++) {
      assertFalse(((SegmentReader) subReaders[i]).termsIndexLoaded());
    }
    r2.close();
    dir.close();
  }
  public void testPrepareCommitIsCurrent() throws Throwable {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    writer.addDocument(doc);
    IndexReader r = IndexReader.open(dir, true);
    assertTrue(r.isCurrent());
    writer.addDocument(doc);
    writer.prepareCommit();
    assertTrue(r.isCurrent());
    IndexReader r2 = r.reopen();
    assertTrue(r == r2);
    writer.commit();
    assertFalse(r.isCurrent());
    writer.close();
    r.close();
    dir.close();
  }
}
