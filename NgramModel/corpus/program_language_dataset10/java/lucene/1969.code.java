package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
public class TestPrefixQuery extends LuceneTestCase {
  public void testPrefixQuery() throws Exception {
    RAMDirectory directory = new RAMDirectory();
    String[] categories = new String[] {"/Computers",
                                        "/Computers/Mac",
                                        "/Computers/Windows"};
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < categories.length; i++) {
      Document doc = new Document();
      doc.add(new Field("category", categories[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
      writer.addDocument(doc);
    }
    writer.close();
    PrefixQuery query = new PrefixQuery(new Term("category", "/Computers"));
    IndexSearcher searcher = new IndexSearcher(directory, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("All documents in /Computers category and below", 3, hits.length);
    query = new PrefixQuery(new Term("category", "/Computers/Mac"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("One in /Computers/Mac", 1, hits.length);
  }
}
