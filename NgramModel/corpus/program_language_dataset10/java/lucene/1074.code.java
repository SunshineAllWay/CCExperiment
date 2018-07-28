package org.apache.lucene.queryParser.complexPhrase;
import java.util.HashSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestComplexPhraseQuery extends LuceneTestCase {
  Analyzer analyzer = new StandardAnalyzer(TEST_VERSION_CURRENT);
  DocData docsContent[] = { new DocData("john smith", "1"),
      new DocData("johathon smith", "2"),
      new DocData("john percival smith", "3"),
      new DocData("jackson waits tom", "4") };
  private IndexSearcher searcher;
  String defaultFieldName = "name";
  public void testComplexPhrases() throws Exception {
    checkMatches("\"john smith\"", "1"); 
    checkMatches("\"j*   smyth~\"", "1,2"); 
    checkMatches("\"(jo* -john)  smith\"", "2"); 
    checkMatches("\"jo*  smith\"~2", "1,2,3"); 
    checkMatches("\"jo* [sma TO smZ]\" ", "1,2"); 
    checkMatches("\"john\"", "1,3"); 
    checkMatches("\"(john OR johathon)  smith\"", "1,2"); 
    checkMatches("\"(jo* -john) smyth~\"", "2"); 
    checkMatches("\"john  nosuchword*\"", ""); 
    checkBadQuery("\"jo*  id:1 smith\""); 
    checkBadQuery("\"jo* \"smith\" \""); 
  }
  private void checkBadQuery(String qString) {
    QueryParser qp = new ComplexPhraseQueryParser(TEST_VERSION_CURRENT, defaultFieldName, analyzer);
    Throwable expected = null;
    try {
      qp.parse(qString);
    } catch (Throwable e) {
      expected = e;
    }
    assertNotNull("Expected parse error in " + qString, expected);
  }
  private void checkMatches(String qString, String expectedVals)
      throws Exception {
    QueryParser qp = new ComplexPhraseQueryParser(TEST_VERSION_CURRENT, defaultFieldName, analyzer);
    qp.setFuzzyPrefixLength(1); 
    Query q = qp.parse(qString);
    HashSet<String> expecteds = new HashSet<String>();
    String[] vals = expectedVals.split(",");
    for (int i = 0; i < vals.length; i++) {
      if (vals[i].length() > 0)
        expecteds.add(vals[i]);
    }
    TopDocs td = searcher.search(q, 10);
    ScoreDoc[] sd = td.scoreDocs;
    for (int i = 0; i < sd.length; i++) {
      Document doc = searcher.doc(sd[i].doc);
      String id = doc.get("id");
      assertTrue(qString + "matched doc#" + id + " not expected", expecteds
          .contains(id));
      expecteds.remove(id);
    }
    assertEquals(qString + " missing some matches ", 0, expecteds.size());
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RAMDirectory rd = new RAMDirectory();
    IndexWriter w = new IndexWriter(rd, new IndexWriterConfig(TEST_VERSION_CURRENT, analyzer));
    for (int i = 0; i < docsContent.length; i++) {
      Document doc = new Document();
      doc.add(new Field("name", docsContent[i].name, Field.Store.YES,
          Field.Index.ANALYZED));
      doc.add(new Field("id", docsContent[i].id, Field.Store.YES,
          Field.Index.ANALYZED));
      w.addDocument(doc);
    }
    w.close();
    searcher = new IndexSearcher(rd, true);
  }
  @Override
  protected void tearDown() throws Exception {
    searcher.close();
    super.tearDown();
  }
  static class DocData {
    String name;
    String id;
    public DocData(String name, String id) {
      super();
      this.name = name;
      this.id = id;
    }
  }
}
