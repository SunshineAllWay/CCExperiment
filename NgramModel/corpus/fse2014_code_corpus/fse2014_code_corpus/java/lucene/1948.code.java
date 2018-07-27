package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MockRAMDirectory;
import java.util.ArrayList;
import java.util.List;
public class TestFieldCacheTermsFilter extends LuceneTestCase {
  public void testMissingTerms() throws Exception {
    String fieldName = "field1";
    MockRAMDirectory rd = new MockRAMDirectory();
    IndexWriter w = new IndexWriter(rd, new IndexWriterConfig(TEST_VERSION_CURRENT, new KeywordAnalyzer()));
    for (int i = 0; i < 100; i++) {
      Document doc = new Document();
      int term = i * 10; 
      doc.add(new Field(fieldName, "" + term, Field.Store.YES, Field.Index.NOT_ANALYZED));
      w.addDocument(doc);
    }
    w.close();
    IndexReader reader = IndexReader.open(rd, true);
    IndexSearcher searcher = new IndexSearcher(reader);
    int numDocs = reader.numDocs();
    ScoreDoc[] results;
    MatchAllDocsQuery q = new MatchAllDocsQuery();
    List<String> terms = new ArrayList<String>();
    terms.add("5");
    results = searcher.search(q, new FieldCacheTermsFilter(fieldName,  terms.toArray(new String[0])), numDocs).scoreDocs;
    assertEquals("Must match nothing", 0, results.length);
    terms = new ArrayList<String>();
    terms.add("10");
    results = searcher.search(q, new FieldCacheTermsFilter(fieldName,  terms.toArray(new String[0])), numDocs).scoreDocs;
    assertEquals("Must match 1", 1, results.length);
    terms = new ArrayList<String>();
    terms.add("10");
    terms.add("20");
    results = searcher.search(q, new FieldCacheTermsFilter(fieldName,  terms.toArray(new String[0])), numDocs).scoreDocs;
    assertEquals("Must match 2", 2, results.length);
    reader.close();
    rd.close();
  }
}
