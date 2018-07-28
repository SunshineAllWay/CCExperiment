package org.apache.lucene.document;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MockRAMDirectory;
public class TestBinaryDocument extends LuceneTestCase {
  String binaryValStored = "this text will be stored as a byte array in the index";
  String binaryValCompressed = "this text will be also stored and compressed as a byte array in the index";
  public void testBinaryFieldInIndex()
    throws Exception
  {
    Fieldable binaryFldStored = new Field("binaryStored", binaryValStored.getBytes());
    Fieldable stringFldStored = new Field("stringStored", binaryValStored, Field.Store.YES, Field.Index.NO, Field.TermVector.NO);
    try {
      new Field("fail", binaryValStored.getBytes(), Field.Store.NO);
      fail();
    }
    catch (IllegalArgumentException iae) {
    }
    Document doc = new Document();
    doc.add(binaryFldStored);
    doc.add(stringFldStored);
    assertEquals(2, doc.fields.size());
    MockRAMDirectory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    writer.addDocument(doc);
    writer.close();
    IndexReader reader = IndexReader.open(dir, false);
    Document docFromReader = reader.document(0);
    assertTrue(docFromReader != null);
    String binaryFldStoredTest = new String(docFromReader.getBinaryValue("binaryStored"));
    assertTrue(binaryFldStoredTest.equals(binaryValStored));
    String stringFldStoredTest = docFromReader.get("stringStored");
    assertTrue(stringFldStoredTest.equals(binaryValStored));
    reader.deleteDocument(0);
    assertEquals(0, reader.numDocs());
    reader.close();
    dir.close();
  }
  public void testCompressionTools() throws Exception {
    Fieldable binaryFldCompressed = new Field("binaryCompressed", CompressionTools.compress(binaryValCompressed.getBytes()));
    Fieldable stringFldCompressed = new Field("stringCompressed", CompressionTools.compressString(binaryValCompressed));
    Document doc = new Document();
    doc.add(binaryFldCompressed);
    doc.add(stringFldCompressed);
    MockRAMDirectory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    writer.addDocument(doc);
    writer.close();
    IndexReader reader = IndexReader.open(dir, false);
    Document docFromReader = reader.document(0);
    assertTrue(docFromReader != null);
    String binaryFldCompressedTest = new String(CompressionTools.decompress(docFromReader.getBinaryValue("binaryCompressed")));
    assertTrue(binaryFldCompressedTest.equals(binaryValCompressed));
    assertTrue(CompressionTools.decompressString(docFromReader.getBinaryValue("stringCompressed")).equals(binaryValCompressed));
    reader.close();
    dir.close();
  }
}
