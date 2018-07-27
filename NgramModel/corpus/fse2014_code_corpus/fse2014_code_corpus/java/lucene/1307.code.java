package org.apache.lucene.search.spell;
import java.io.IOException;
import java.util.Iterator;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestLuceneDictionary extends LuceneTestCase {
  private Directory store = new RAMDirectory();
  private IndexReader indexReader = null;
  private LuceneDictionary ld;
  private Iterator<String> it;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    IndexWriter writer = new IndexWriter(store, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc;
    doc = new  Document();
    doc.add(new Field("aaa", "foo", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new  Document();
    doc.add(new Field("aaa", "foo", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new  Document();
    doc.add(new  Field("contents", "Tom", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new  Document();
    doc.add(new  Field("contents", "Jerry", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("zzz", "bar", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.optimize();
    writer.close();
  }
  public void testFieldNonExistent() throws IOException {
    try {
      indexReader = IndexReader.open(store, true);
      ld = new LuceneDictionary(indexReader, "nonexistent_field");
      it = ld.getWordsIterator();
      assertFalse("More elements than expected", it.hasNext());
      assertTrue("Nonexistent element is really null", it.next() == null);
    } finally {
      if  (indexReader != null) { indexReader.close(); }
    }
  }
  public void testFieldAaa() throws IOException {
    try {
      indexReader = IndexReader.open(store, true);
      ld = new LuceneDictionary(indexReader, "aaa");
      it = ld.getWordsIterator();
      assertTrue("First element doesn't exist.", it.hasNext());
      assertTrue("First element isn't correct", it.next().equals("foo"));
      assertFalse("More elements than expected", it.hasNext());
      assertTrue("Nonexistent element is really null", it.next() == null);
    } finally {
      if  (indexReader != null) { indexReader.close(); }
    }
  }
  public void testFieldContents_1() throws IOException {
    try {
      indexReader = IndexReader.open(store, true);
      ld = new LuceneDictionary(indexReader, "contents");
      it = ld.getWordsIterator();
      assertTrue("First element doesn't exist.", it.hasNext());
      assertTrue("First element isn't correct", it.next().equals("Jerry"));
      assertTrue("Second element doesn't exist.", it.hasNext());
      assertTrue("Second element isn't correct", it.next().equals("Tom"));
      assertFalse("More elements than expected", it.hasNext());
      assertTrue("Nonexistent element is really null", it.next() == null);
      ld = new LuceneDictionary(indexReader, "contents");
      it = ld.getWordsIterator();
      int counter = 2;
      while (it.hasNext()) {
        it.next();
        counter--;
      }
      assertTrue("Number of words incorrect", counter == 0);
    }
    finally {
      if  (indexReader != null) { indexReader.close(); }
    }
  }
  public void testFieldContents_2() throws IOException {
    try {
      indexReader = IndexReader.open(store, true);
      ld = new LuceneDictionary(indexReader, "contents");
      it = ld.getWordsIterator();
      assertTrue("First element isn't were it should be.", it.hasNext());
      assertTrue("First element isn't were it should be.", it.hasNext());
      assertTrue("First element isn't were it should be.", it.hasNext());
      assertTrue("First element isn't correct", it.next().equals("Jerry"));
      assertTrue("Second element isn't correct", it.next().equals("Tom"));
      assertTrue("Nonexistent element is really null", it.next() == null);
      assertFalse("There should be any more elements", it.hasNext());
      assertFalse("There should be any more elements", it.hasNext());
      assertFalse("There should be any more elements", it.hasNext());
      assertTrue("Nonexistent element is really null", it.next() == null);
      assertTrue("Nonexistent element is really null", it.next() == null);
      assertTrue("Nonexistent element is really null", it.next() == null);
    }
    finally {
      if  (indexReader != null) { indexReader.close(); }
    }
  }
  public void testFieldZzz() throws IOException {
    try {
      indexReader = IndexReader.open(store, true);
      ld = new LuceneDictionary(indexReader, "zzz");
      it = ld.getWordsIterator();
      assertTrue("First element doesn't exist.", it.hasNext());
      assertTrue("First element isn't correct", it.next().equals("bar"));
      assertFalse("More elements than expected", it.hasNext());
      assertTrue("Nonexistent element is really null", it.next() == null);
    }
    finally {
      if  (indexReader != null) { indexReader.close(); }
    }
  }
  public void testSpellchecker() throws IOException {
    SpellChecker sc = new SpellChecker(new RAMDirectory());
    indexReader = IndexReader.open(store, true);
    sc.indexDictionary(new LuceneDictionary(indexReader, "contents"));
    String[] suggestions = sc.suggestSimilar("Tam", 1);
    assertEquals(1, suggestions.length);
    assertEquals("Tom", suggestions[0]);
    suggestions = sc.suggestSimilar("Jarry", 1);
    assertEquals(1, suggestions.length);
    assertEquals("Jerry", suggestions[0]);
    indexReader.close();
  }
}
