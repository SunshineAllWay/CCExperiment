package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
public class TestRemoteSearchable extends LuceneTestCase {
  public TestRemoteSearchable(String name) {
    super(name);
  }
  private static int port = -1;
  private static Searchable getRemote() throws Exception {
    if (port == -1) {
      startServer();
    }
    try {
      return lookupRemote();
    } catch (Throwable e) {
      startServer();
      return lookupRemote();
    }
  }
  private static Searchable lookupRemote() throws Exception {
    return (Searchable)Naming.lookup("//localhost:" + port + "/Searchable");
  }
  private static void startServer() throws Exception {
    RAMDirectory indexStore = new RAMDirectory();
    IndexWriter writer = new IndexWriter(indexStore, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("test", "test text", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("other", "other test text", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.optimize();
    writer.close();
    port = _TestUtil.getRandomSocketPort();
    LocateRegistry.createRegistry(port);
    Searchable local = new IndexSearcher(indexStore, true);
    RemoteSearchable impl = new RemoteSearchable(local);
    Naming.rebind("//localhost:" + port + "/Searchable", impl);
  }
  private static void search(Query query) throws Exception {
    Searchable[] searchables = { getRemote() };
    Searcher searcher = new MultiSearcher(searchables);
    ScoreDoc[] result = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(1, result.length);
    Document document = searcher.doc(result[0].doc);
    assertTrue("document is null and it shouldn't be", document != null);
    assertEquals("test text", document.get("test"));
    assertTrue("document.getFields() Size: " + document.getFields().size() + " is not: " + 2, document.getFields().size() == 2);
    Set<String> ftl = new HashSet<String>();
    ftl.add("other");
    FieldSelector fs = new SetBasedFieldSelector(ftl, Collections.<String>emptySet());
    document = searcher.doc(0, fs);
    assertTrue("document is null and it shouldn't be", document != null);
    assertTrue("document.getFields() Size: " + document.getFields().size() + " is not: " + 1, document.getFields().size() == 1);
    fs = new MapFieldSelector(new String[]{"other"});
    document = searcher.doc(0, fs);
    assertTrue("document is null and it shouldn't be", document != null);
    assertTrue("document.getFields() Size: " + document.getFields().size() + " is not: " + 1, document.getFields().size() == 1);
  }
  public void testTermQuery() throws Exception {
    search(new TermQuery(new Term("test", "test")));
  }
  public void testBooleanQuery() throws Exception {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("test", "test")), BooleanClause.Occur.MUST);
    search(query);
  }
  public void testPhraseQuery() throws Exception {
    PhraseQuery query = new PhraseQuery();
    query.add(new Term("test", "test"));
    query.add(new Term("test", "text"));
    search(query);
  }
  public void testQueryFilter() throws Exception {
    Searchable[] searchables = { getRemote() };
    Searcher searcher = new MultiSearcher(searchables);
    ScoreDoc[] hits = searcher.search(
          new TermQuery(new Term("test", "text")),
          new QueryWrapperFilter(new TermQuery(new Term("test", "test"))), 1000).scoreDocs;
    assertEquals(1, hits.length);
    ScoreDoc[] nohits = searcher.search(
          new TermQuery(new Term("test", "text")),
          new QueryWrapperFilter(new TermQuery(new Term("test", "non-existent-term"))), 1000).scoreDocs;
    assertEquals(0, nohits.length);
  }
  public void testConstantScoreQuery() throws Exception {
    Searchable[] searchables = { getRemote() };
    Searcher searcher = new MultiSearcher(searchables);
    ScoreDoc[] hits = searcher.search(
          new ConstantScoreQuery(new QueryWrapperFilter(
                                   new TermQuery(new Term("test", "test")))), null, 1000).scoreDocs;
    assertEquals(1, hits.length);
  }
}
