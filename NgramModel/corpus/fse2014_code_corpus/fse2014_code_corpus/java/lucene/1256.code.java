package org.apache.lucene.search;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
public class TestRemoteCachingWrapperFilter extends LuceneTestCase {
  public TestRemoteCachingWrapperFilter(String name) {
    super(name);
  }
  private static Searchable getRemote() throws Exception {
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
  private static int port;
  private static void startServer() throws Exception {
    RAMDirectory indexStore = new RAMDirectory();
    IndexWriter writer = new IndexWriter(indexStore, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(
        TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("test", "test text", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("type", "A", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("other", "other test text", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("test", "test text", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("type", "B", Field.Store.YES, Field.Index.ANALYZED));
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
  private static void search(Query query, Filter filter, int hitNumber, String typeValue) throws Exception {
    Searchable[] searchables = { getRemote() };
    Searcher searcher = new MultiSearcher(searchables);
    ScoreDoc[] result = searcher.search(query,filter, 1000).scoreDocs;
    assertEquals(1, result.length);
    Document document = searcher.doc(result[hitNumber].doc);
    assertTrue("document is null and it shouldn't be", document != null);
    assertEquals(typeValue, document.get("type"));
    assertTrue("document.getFields() Size: " + document.getFields().size() + " is not: " + 3, document.getFields().size() == 3);
  }
  public void testTermRemoteFilter() throws Exception {
    CachingWrapperFilterHelper cwfh = new CachingWrapperFilterHelper(new QueryWrapperFilter(new TermQuery(new Term("type", "a"))));
    cwfh.setShouldHaveCache(false);
    search(new TermQuery(new Term("test", "test")), cwfh, 0, "A");
    cwfh.setShouldHaveCache(false);
    search(new TermQuery(new Term("test", "test")), cwfh, 0, "A");
    RemoteCachingWrapperFilterHelper rcwfh = new RemoteCachingWrapperFilterHelper(cwfh, false);
    search(new TermQuery(new Term("test", "test")), rcwfh, 0, "A");
    rcwfh.shouldHaveCache(true);
    search(new TermQuery(new Term("test", "test")), rcwfh, 0, "A");
    rcwfh = new RemoteCachingWrapperFilterHelper(new QueryWrapperFilter(new TermQuery(new Term("type", "a"))), false);
    rcwfh.shouldHaveCache(false);
    search(new TermQuery(new Term("test", "test")), rcwfh, 0, "A");
    rcwfh = new RemoteCachingWrapperFilterHelper(new QueryWrapperFilter(new TermQuery(new Term("type", "a"))), false);
    rcwfh.shouldHaveCache(true);
    search(new TermQuery(new Term("test", "test")), rcwfh, 0, "A");
    rcwfh = new RemoteCachingWrapperFilterHelper(new QueryWrapperFilter(new TermQuery(new Term("type", "b"))), false);
    rcwfh.shouldHaveCache(false);
    search(new TermQuery(new Term("type", "b")), rcwfh, 0, "B");
  }
}
