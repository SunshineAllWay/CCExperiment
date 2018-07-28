package org.apache.lucene.index;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import java.io.IOException;
public class TestDirectoryReader extends LuceneTestCase {
  protected Directory dir;
  private Document doc1;
  private Document doc2;
  protected SegmentReader [] readers = new SegmentReader[2];
  protected SegmentInfos sis;
  public TestDirectoryReader(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dir = new RAMDirectory();
    doc1 = new Document();
    doc2 = new Document();
    DocHelper.setupDoc(doc1);
    DocHelper.setupDoc(doc2);
    DocHelper.writeDoc(dir, doc1);
    DocHelper.writeDoc(dir, doc2);
    sis = new SegmentInfos();
    sis.read(dir);
  }
  protected IndexReader openReader() throws IOException {
    IndexReader reader;
    reader = IndexReader.open(dir, false);
    assertTrue(reader instanceof DirectoryReader);
    assertTrue(dir != null);
    assertTrue(sis != null);
    assertTrue(reader != null);
    return reader;
  }
  public void test() throws Exception {
    setUp();
    doTestDocument();
    doTestUndeleteAll();
  }    
  public void doTestDocument() throws IOException {
    sis.read(dir);
    IndexReader reader = openReader();
    assertTrue(reader != null);
    Document newDoc1 = reader.document(0);
    assertTrue(newDoc1 != null);
    assertTrue(DocHelper.numFields(newDoc1) == DocHelper.numFields(doc1) - DocHelper.unstored.size());
    Document newDoc2 = reader.document(1);
    assertTrue(newDoc2 != null);
    assertTrue(DocHelper.numFields(newDoc2) == DocHelper.numFields(doc2) - DocHelper.unstored.size());
    TermFreqVector vector = reader.getTermFreqVector(0, DocHelper.TEXT_FIELD_2_KEY);
    assertTrue(vector != null);
    TestSegmentReader.checkNorms(reader);
  }
  public void doTestUndeleteAll() throws IOException {
    sis.read(dir);
    IndexReader reader = openReader();
    assertTrue(reader != null);
    assertEquals( 2, reader.numDocs() );
    reader.deleteDocument(0);
    assertEquals( 1, reader.numDocs() );
    reader.undeleteAll();
    assertEquals( 2, reader.numDocs() );
    reader.commit();
    reader.close();
    if (reader instanceof MultiReader)
      sis.commit(dir);
    sis.read(dir);
    reader = openReader();
    assertEquals( 2, reader.numDocs() );
    reader.deleteDocument(0);
    assertEquals( 1, reader.numDocs() );
    reader.commit();
    reader.close();
    if (reader instanceof MultiReader)
      sis.commit(dir);
    sis.read(dir);
    reader = openReader();
    assertEquals( 1, reader.numDocs() );
  }
  public void testIsCurrent() throws IOException {
    RAMDirectory ramDir1=new RAMDirectory();
    addDoc(ramDir1, "test foo", true);
    RAMDirectory ramDir2=new RAMDirectory();
    addDoc(ramDir2, "test blah", true);
    IndexReader[] readers = new IndexReader[]{IndexReader.open(ramDir1, false), IndexReader.open(ramDir2, false)};
    MultiReader mr = new MultiReader(readers);
    assertTrue(mr.isCurrent());   
    addDoc(ramDir1, "more text", false);
    assertFalse(mr.isCurrent());   
    addDoc(ramDir2, "even more text", false);
    assertFalse(mr.isCurrent());   
    try {
      mr.getVersion();
      fail();
    } catch (UnsupportedOperationException e) {
    }
    mr.close();
  }
  public void testMultiTermDocs() throws IOException {
    RAMDirectory ramDir1=new RAMDirectory();
    addDoc(ramDir1, "test foo", true);
    RAMDirectory ramDir2=new RAMDirectory();
    addDoc(ramDir2, "test blah", true);
    RAMDirectory ramDir3=new RAMDirectory();
    addDoc(ramDir3, "test wow", true);
    IndexReader[] readers1 = new IndexReader[]{IndexReader.open(ramDir1, false), IndexReader.open(ramDir3, false)};
    IndexReader[] readers2 = new IndexReader[]{IndexReader.open(ramDir1, false), IndexReader.open(ramDir2, false), IndexReader.open(ramDir3, false)};
    MultiReader mr2 = new MultiReader(readers1);
    MultiReader mr3 = new MultiReader(readers2);
    TermDocs td2 = mr2.termDocs();
    TermEnum te3 = mr3.terms(new Term("body","wow"));
    td2.seek(te3);
    int ret = 0;
    while (td2.next()) ret += td2.doc();
    td2.close();
    te3.close();
    assertTrue(ret > 0);
  }
  public void testAllTermDocs() throws IOException {
    IndexReader reader = openReader();
    int NUM_DOCS = 2;
    TermDocs td = reader.termDocs(null);
    for(int i=0;i<NUM_DOCS;i++) {
      assertTrue(td.next());
      assertEquals(i, td.doc());
      assertEquals(1, td.freq());
    }
    td.close();
    reader.close();
  }
  private void addDoc(RAMDirectory ramDir1, String s, boolean create) throws IOException {
    IndexWriter iw = new IndexWriter(ramDir1, new IndexWriterConfig(
        TEST_VERSION_CURRENT, 
        new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(
        create ? OpenMode.CREATE : OpenMode.APPEND));
    Document doc = new Document();
    doc.add(new Field("body", s, Field.Store.YES, Field.Index.ANALYZED));
    iw.addDocument(doc);
    iw.close();
  }
}
