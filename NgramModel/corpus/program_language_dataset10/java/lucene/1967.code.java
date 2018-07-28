package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
public class TestPrefixFilter extends LuceneTestCase {
  public void testPrefixFilter() throws Exception {
    RAMDirectory directory = new RAMDirectory();
    String[] categories = new String[] {"/Computers/Linux",
                                        "/Computers/Mac/One",
                                        "/Computers/Mac/Two",
                                        "/Computers/Windows"};
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < categories.length; i++) {
      Document doc = new Document();
      doc.add(new Field("category", categories[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
      writer.addDocument(doc);
    }
    writer.close();
    PrefixFilter filter = new PrefixFilter(new Term("category", "/Computers"));
    Query query = new ConstantScoreQuery(filter);
    IndexSearcher searcher = new IndexSearcher(directory, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(4, hits.length);
    filter = new PrefixFilter(new Term("category", "/Computers/Mac"));
    query = new ConstantScoreQuery(filter);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    filter = new PrefixFilter(new Term("category", "/Computers/Linux"));
    query = new ConstantScoreQuery(filter);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    filter = new PrefixFilter(new Term("category", "/Computers/Windows"));
    query = new ConstantScoreQuery(filter);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    filter = new PrefixFilter(new Term("category", "/Computers/ObsoleteOS"));
    query = new ConstantScoreQuery(filter);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    filter = new PrefixFilter(new Term("category", "/Computers/AAA"));
    query = new ConstantScoreQuery(filter);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    filter = new PrefixFilter(new Term("category", "/Computers/ZZZ"));
    query = new ConstantScoreQuery(filter);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    filter = new PrefixFilter(new Term("category", ""));
    query = new ConstantScoreQuery(filter);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(4, hits.length);
    filter = new PrefixFilter(new Term("nonexistantfield", "/Computers"));
    query = new ConstantScoreQuery(filter);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
  }
}
