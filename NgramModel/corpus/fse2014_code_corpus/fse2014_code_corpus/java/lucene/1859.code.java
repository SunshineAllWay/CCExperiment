package org.apache.lucene.document;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestDocument extends LuceneTestCase
{
  String binaryVal = "this text will be stored as a byte array in the index";
  String binaryVal2 = "this text will be also stored as a byte array in the index";
  public void testBinaryField()
    throws Exception
  {
    Document doc = new Document();
    Fieldable stringFld = new Field("string", binaryVal, Field.Store.YES, Field.Index.NO);
    Fieldable binaryFld = new Field("binary", binaryVal.getBytes());
    Fieldable binaryFld2 = new Field("binary", binaryVal2.getBytes());
    doc.add(stringFld);
    doc.add(binaryFld);
    assertEquals(2, doc.fields.size());
    assertTrue(binaryFld.isBinary());
    assertTrue(binaryFld.isStored());
    assertFalse(binaryFld.isIndexed());
    assertFalse(binaryFld.isTokenized());
    String binaryTest = new String(doc.getBinaryValue("binary"));
    assertTrue(binaryTest.equals(binaryVal));
    String stringTest = doc.get("string");
    assertTrue(binaryTest.equals(stringTest));
    doc.add(binaryFld2);
    assertEquals(3, doc.fields.size());
    byte[][] binaryTests = doc.getBinaryValues("binary");
    assertEquals(2, binaryTests.length);
    binaryTest = new String(binaryTests[0]);
    String binaryTest2 = new String(binaryTests[1]);
    assertFalse(binaryTest.equals(binaryTest2));
    assertTrue(binaryTest.equals(binaryVal));
    assertTrue(binaryTest2.equals(binaryVal2));
    doc.removeField("string");
    assertEquals(2, doc.fields.size());
    doc.removeFields("binary");
    assertEquals(0, doc.fields.size());
  }
  public void testRemoveForNewDocument() throws Exception
  {
    Document doc = makeDocumentWithFields();
    assertEquals(8, doc.fields.size());
    doc.removeFields("keyword");
    assertEquals(6, doc.fields.size());
    doc.removeFields("doesnotexists");      
    doc.removeFields("keyword");		
    assertEquals(6, doc.fields.size());
    doc.removeField("text");
    assertEquals(5, doc.fields.size());
    doc.removeField("text");
    assertEquals(4, doc.fields.size());
    doc.removeField("text");
    assertEquals(4, doc.fields.size());
    doc.removeField("doesnotexists");       
    assertEquals(4, doc.fields.size());
    doc.removeFields("unindexed");
    assertEquals(2, doc.fields.size());
    doc.removeFields("unstored");
    assertEquals(0, doc.fields.size());
    doc.removeFields("doesnotexists");	
    assertEquals(0, doc.fields.size());
  }
  public void testConstructorExceptions()
  {
    new Field("name", "value", Field.Store.YES, Field.Index.NO);  
    new Field("name", "value", Field.Store.NO, Field.Index.NOT_ANALYZED);  
    try {
      new Field("name", "value", Field.Store.NO, Field.Index.NO);
      fail();
    } catch(IllegalArgumentException e) {
    }
    new Field("name", "value", Field.Store.YES, Field.Index.NO, Field.TermVector.NO); 
    try {
      new Field("name", "value", Field.Store.YES, Field.Index.NO, Field.TermVector.YES);
      fail();
    } catch(IllegalArgumentException e) {
    }
  }
    public void testGetValuesForNewDocument() throws Exception
    {
        doAssert(makeDocumentWithFields(), false);
    }
    public void testGetValuesForIndexedDocument() throws Exception {
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(
        TEST_VERSION_CURRENT)));
        writer.addDocument(makeDocumentWithFields());
        writer.close();
        Searcher searcher = new IndexSearcher(dir, true);
	Query query = new TermQuery(new Term("keyword", "test1"));
        ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
  assertEquals(1, hits.length);
         doAssert(searcher.doc(hits[0].doc), true);
         searcher.close();
    }
    private Document makeDocumentWithFields()
    {
        Document doc = new Document();
        doc.add(new Field(  "keyword",   "test1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(  "keyword",   "test2", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(     "text",      "test1", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field(     "text",      "test2", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("unindexed", "test1", Field.Store.YES, Field.Index.NO));
        doc.add(new Field("unindexed", "test2", Field.Store.YES, Field.Index.NO));
        doc.add(new Field( "unstored",  "test1", Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field( "unstored",  "test2", Field.Store.NO, Field.Index.ANALYZED));
        return doc;
    }
    private void doAssert(Document doc, boolean fromIndex)
    {
        String[] keywordFieldValues   = doc.getValues("keyword");
        String[] textFieldValues      = doc.getValues("text");
        String[] unindexedFieldValues = doc.getValues("unindexed");
        String[] unstoredFieldValues  = doc.getValues("unstored");
        assertTrue(keywordFieldValues.length   == 2);
        assertTrue(textFieldValues.length      == 2);
        assertTrue(unindexedFieldValues.length == 2);
        if (! fromIndex)
        {
            assertTrue(unstoredFieldValues.length  == 2);
        }
        assertTrue(keywordFieldValues[0].equals("test1"));
        assertTrue(keywordFieldValues[1].equals("test2"));
        assertTrue(textFieldValues[0].equals("test1"));
        assertTrue(textFieldValues[1].equals("test2"));
        assertTrue(unindexedFieldValues[0].equals("test1"));
        assertTrue(unindexedFieldValues[1].equals("test2"));
        if (! fromIndex)
        {
            assertTrue(unstoredFieldValues[0].equals("test1"));
            assertTrue(unstoredFieldValues[1].equals("test2"));
        }
    }
    public void testFieldSetValue() throws Exception {
      Field field = new Field("id", "id1", Field.Store.YES, Field.Index.NOT_ANALYZED);
      Document doc = new Document();
      doc.add(field);
      doc.add(new Field("keyword", "test", Field.Store.YES, Field.Index.NOT_ANALYZED));
      RAMDirectory dir = new RAMDirectory();
      IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(
        TEST_VERSION_CURRENT)));
      writer.addDocument(doc);
      field.setValue("id2");
      writer.addDocument(doc);
      field.setValue("id3");
      writer.addDocument(doc);
      writer.close();
      Searcher searcher = new IndexSearcher(dir, true);
      Query query = new TermQuery(new Term("keyword", "test"));
      ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
      assertEquals(3, hits.length);
      int result = 0;
      for(int i=0;i<3;i++) {
        Document doc2 = searcher.doc(hits[i].doc);
        Field f = doc2.getField("id");
        if (f.stringValue().equals("id1"))
          result |= 1;
        else if (f.stringValue().equals("id2"))
          result |= 2;
        else if (f.stringValue().equals("id3"))
          result |= 4;
        else
          fail("unexpected id field");
      }
      searcher.close();
      dir.close();
      assertEquals("did not see all IDs", 7, result);
    }
  public void testFieldSetValueChangeBinary() {
    Field field1 = new Field("field1", new byte[0]);
    Field field2 = new Field("field2", "",
                             Field.Store.YES, Field.Index.ANALYZED);
    try {
      field1.setValue("abc");
      fail("did not hit expected exception");
    } catch (IllegalArgumentException iae) {
    }
    try {
      field2.setValue(new byte[0]);
      fail("did not hit expected exception");
    } catch (IllegalArgumentException iae) {
    }
  }
}
