package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
public class TestBooleanOr extends LuceneTestCase {
  private static String FIELD_T = "T";
  private static String FIELD_C = "C";
  private TermQuery t1 = new TermQuery(new Term(FIELD_T, "files"));
  private TermQuery t2 = new TermQuery(new Term(FIELD_T, "deleting"));
  private TermQuery c1 = new TermQuery(new Term(FIELD_C, "production"));
  private TermQuery c2 = new TermQuery(new Term(FIELD_C, "optimize"));
  private IndexSearcher searcher = null;
  private int search(Query q) throws IOException {
    QueryUtils.check(q,searcher);
    return searcher.search(q, null, 1000).totalHits;
  }
  public void testElements() throws IOException {
    assertEquals(1, search(t1));
    assertEquals(1, search(t2));
    assertEquals(1, search(c1));
    assertEquals(1, search(c2));
  }
  public void testFlat() throws IOException {
    BooleanQuery q = new BooleanQuery();
    q.add(new BooleanClause(t1, BooleanClause.Occur.SHOULD));
    q.add(new BooleanClause(t2, BooleanClause.Occur.SHOULD));
    q.add(new BooleanClause(c1, BooleanClause.Occur.SHOULD));
    q.add(new BooleanClause(c2, BooleanClause.Occur.SHOULD));
    assertEquals(1, search(q));
  }
  public void testParenthesisMust() throws IOException {
    BooleanQuery q3 = new BooleanQuery();
    q3.add(new BooleanClause(t1, BooleanClause.Occur.SHOULD));
    q3.add(new BooleanClause(t2, BooleanClause.Occur.SHOULD));
    BooleanQuery q4 = new BooleanQuery();
    q4.add(new BooleanClause(c1, BooleanClause.Occur.MUST));
    q4.add(new BooleanClause(c2, BooleanClause.Occur.MUST));
    BooleanQuery q2 = new BooleanQuery();
    q2.add(q3, BooleanClause.Occur.SHOULD);
    q2.add(q4, BooleanClause.Occur.SHOULD);
    assertEquals(1, search(q2));
  }
  public void testParenthesisMust2() throws IOException {
    BooleanQuery q3 = new BooleanQuery();
    q3.add(new BooleanClause(t1, BooleanClause.Occur.SHOULD));
    q3.add(new BooleanClause(t2, BooleanClause.Occur.SHOULD));
    BooleanQuery q4 = new BooleanQuery();
    q4.add(new BooleanClause(c1, BooleanClause.Occur.SHOULD));
    q4.add(new BooleanClause(c2, BooleanClause.Occur.SHOULD));
    BooleanQuery q2 = new BooleanQuery();
    q2.add(q3, BooleanClause.Occur.SHOULD);
    q2.add(q4, BooleanClause.Occur.MUST);
    assertEquals(1, search(q2));
  }
  public void testParenthesisShould() throws IOException {
    BooleanQuery q3 = new BooleanQuery();
    q3.add(new BooleanClause(t1, BooleanClause.Occur.SHOULD));
    q3.add(new BooleanClause(t2, BooleanClause.Occur.SHOULD));
    BooleanQuery q4 = new BooleanQuery();
    q4.add(new BooleanClause(c1, BooleanClause.Occur.SHOULD));
    q4.add(new BooleanClause(c2, BooleanClause.Occur.SHOULD));
    BooleanQuery q2 = new BooleanQuery();
    q2.add(q3, BooleanClause.Occur.SHOULD);
    q2.add(q4, BooleanClause.Occur.SHOULD);
    assertEquals(1, search(q2));
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RAMDirectory rd = new RAMDirectory();
    IndexWriter writer = new IndexWriter(rd, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    Document d = new Document();
    d.add(new Field(
        FIELD_T,
        "Optimize not deleting all files",
        Field.Store.YES,
        Field.Index.ANALYZED));
    d.add(new Field(
        FIELD_C,
        "Deleted When I run an optimize in our production environment.",
        Field.Store.YES,
        Field.Index.ANALYZED));
    writer.addDocument(d);
    writer.close();
    searcher = new IndexSearcher(rd, true);
  }
}
