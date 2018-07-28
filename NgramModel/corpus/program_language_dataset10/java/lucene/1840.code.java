package org.apache.lucene.analysis;
import java.io.StringReader;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.RAMDirectory;
public class TestKeywordAnalyzer extends BaseTokenStreamTestCase {
  private RAMDirectory directory;
  private IndexSearcher searcher;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(
        TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("partnum", "Q36", Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("description", "Illidium Space Modulator", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.close();
    searcher = new IndexSearcher(directory, true);
  }
  public void testPerFieldAnalyzer() throws Exception {
    PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new SimpleAnalyzer(TEST_VERSION_CURRENT));
    analyzer.addAnalyzer("partnum", new KeywordAnalyzer());
    QueryParser queryParser = new QueryParser(TEST_VERSION_CURRENT, "description", analyzer);
    Query query = queryParser.parse("partnum:Q36 AND SPACE");
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("Q36 kept as-is",
              "+partnum:Q36 +space", query.toString("description"));
    assertEquals("doc found!", 1, hits.length);
  }
  public void testMutipleDocument() throws Exception {
    RAMDirectory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new KeywordAnalyzer()));
    Document doc = new Document();
    doc.add(new Field("partnum", "Q36", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("partnum", "Q37", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.close();
    IndexReader reader = IndexReader.open(dir, true);
    TermDocs td = reader.termDocs(new Term("partnum", "Q36"));
    assertTrue(td.next());
    td = reader.termDocs(new Term("partnum", "Q37"));
    assertTrue(td.next());
  }
  public void testOffsets() throws Exception {
    TokenStream stream = new KeywordAnalyzer().tokenStream("field", new StringReader("abcd"));
    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
    assertTrue(stream.incrementToken());
    assertEquals(0, offsetAtt.startOffset());
    assertEquals(4, offsetAtt.endOffset());
  }
}
