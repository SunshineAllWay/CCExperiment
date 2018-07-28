package org.apache.lucene.search;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestExplanations extends LuceneTestCase {
  protected IndexSearcher searcher;
  public static final String KEY = "KEY";
  public static final String FIELD = "field";
  public static final QueryParser qp =
    new QueryParser(TEST_VERSION_CURRENT, FIELD, new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
  @Override
  protected void tearDown() throws Exception {
    searcher.close();
    super.tearDown();
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RAMDirectory directory = new RAMDirectory();
    IndexWriter writer= new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < docFields.length; i++) {
      Document doc = new Document();
      doc.add(new Field(KEY, ""+i, Field.Store.NO, Field.Index.NOT_ANALYZED));
      doc.add(new Field(FIELD, docFields[i], Field.Store.NO, Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.close();
    searcher = new IndexSearcher(directory, true);
  }
  protected String[] docFields = {
    "w1 w2 w3 w4 w5",
    "w1 w3 w2 w3 zz",
    "w1 xx w2 yy w3",
    "w1 w3 xx w2 yy w3 zz"
  };
  public Query makeQuery(String queryText) throws ParseException {
    return qp.parse(queryText);
  }
  public void qtest(String queryText, int[] expDocNrs) throws Exception {
    qtest(makeQuery(queryText), expDocNrs);
  }
  public void qtest(Query q, int[] expDocNrs) throws Exception {
    CheckHits.checkHitCollector(q, FIELD, searcher, expDocNrs);
  }
  public void bqtest(Query q, int[] expDocNrs) throws Exception {
    qtest(reqB(q), expDocNrs);
    qtest(optB(q), expDocNrs);
  }
  public void bqtest(String queryText, int[] expDocNrs) throws Exception {
    bqtest(makeQuery(queryText), expDocNrs);
  }
  public static class ItemizedFilter extends FieldCacheTermsFilter {
    private static String[] int2str(int [] terms) {
      String [] out = new String[terms.length];
      for (int i = 0; i < terms.length; i++) {
        out[i] = ""+terms[i];
      }
      return out;
    }
    public ItemizedFilter(String keyField, int [] keys) {
      super(keyField, int2str(keys));
    }
    public ItemizedFilter(int [] keys) {
      super(KEY, int2str(keys));
    }
  }
  public static Term[] ta(String[] s) {
    Term[] t = new Term[s.length];
    for (int i = 0; i < s.length; i++) {
      t[i] = new Term(FIELD, s[i]);
    }
    return t;
  }
  public SpanTermQuery st(String s) {
    return new SpanTermQuery(new Term(FIELD,s));
  }
  public SpanNotQuery snot(SpanQuery i, SpanQuery e) {
    return new SpanNotQuery(i,e);
  }
  public SpanOrQuery sor(String s, String e) {
    return sor(st(s), st(e));
  }
  public SpanOrQuery sor(SpanQuery s, SpanQuery e) {
    return new SpanOrQuery(new SpanQuery[] { s, e });
  }
  public SpanOrQuery sor(String s, String m, String e) {
    return sor(st(s), st(m), st(e));
  }
  public SpanOrQuery sor(SpanQuery s, SpanQuery m, SpanQuery e) {
    return new SpanOrQuery(new SpanQuery[] { s, m, e });
  }
  public SpanNearQuery snear(String s, String e, int slop, boolean inOrder) {
    return snear(st(s), st(e), slop, inOrder);
  }
  public SpanNearQuery snear(SpanQuery s, SpanQuery e,
                             int slop, boolean inOrder) {
    return new SpanNearQuery(new SpanQuery[] { s, e }, slop, inOrder);
  }
  public SpanNearQuery snear(String s, String m, String e,
                             int slop, boolean inOrder) {
    return snear(st(s), st(m), st(e), slop, inOrder);
  }
  public SpanNearQuery snear(SpanQuery s, SpanQuery m, SpanQuery e,
                             int slop, boolean inOrder) {
    return new SpanNearQuery(new SpanQuery[] { s, m, e }, slop, inOrder);
  }
  public SpanFirstQuery sf(String s, int b) {
    return new SpanFirstQuery(st(s), b);
  }
  public Query optB(String q) throws Exception {
    return optB(makeQuery(q));
  }
  public Query optB(Query q) throws Exception {
    BooleanQuery bq = new BooleanQuery(true);
    bq.add(q, BooleanClause.Occur.SHOULD);
    bq.add(new TermQuery(new Term("NEVER","MATCH")), BooleanClause.Occur.MUST_NOT);
    return bq;
  }
  public Query reqB(String q) throws Exception {
    return reqB(makeQuery(q));
  }
  public Query reqB(Query q) throws Exception {
    BooleanQuery bq = new BooleanQuery(true);
    bq.add(q, BooleanClause.Occur.MUST);
    bq.add(new TermQuery(new Term(FIELD,"w1")), BooleanClause.Occur.SHOULD);
    return bq;
  }
  public void testNoop() {
  }
}
