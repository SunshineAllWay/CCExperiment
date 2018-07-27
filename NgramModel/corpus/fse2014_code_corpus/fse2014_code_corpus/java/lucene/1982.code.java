package org.apache.lucene.search;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.LuceneTestCase;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.text.Collator;
public class TestTermRangeQuery extends LuceneTestCase {
  private int docCount = 0;
  private RAMDirectory dir;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dir = new RAMDirectory();
  }
  public void testExclusive() throws Exception {
    Query query = new TermRangeQuery("content", "A", "C", false, false);
    initializeIndex(new String[] {"A", "B", "C", "D"});
    IndexSearcher searcher = new IndexSearcher(dir, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("A,B,C,D, only B in range", 1, hits.length);
    searcher.close();
    initializeIndex(new String[] {"A", "B", "D"});
    searcher = new IndexSearcher(dir, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("A,B,D, only B in range", 1, hits.length);
    searcher.close();
    addDoc("C");
    searcher = new IndexSearcher(dir, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("C added, still only B in range", 1, hits.length);
    searcher.close();
  }
  public void testInclusive() throws Exception {
    Query query = new TermRangeQuery("content", "A", "C", true, true);
    initializeIndex(new String[]{"A", "B", "C", "D"});
    IndexSearcher searcher = new IndexSearcher(dir, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("A,B,C,D - A,B,C in range", 3, hits.length);
    searcher.close();
    initializeIndex(new String[]{"A", "B", "D"});
    searcher = new IndexSearcher(dir, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("A,B,D - A and B in range", 2, hits.length);
    searcher.close();
    addDoc("C");
    searcher = new IndexSearcher(dir, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("C added - A, B, C in range", 3, hits.length);
    searcher.close();
  }
  public void testTopTermsRewrite() throws Exception {
    initializeIndex(new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"});
    IndexSearcher searcher = new IndexSearcher(dir, true);
    TermRangeQuery query = new TermRangeQuery("content", "B", "J", true, true);
    checkBooleanTerms(searcher, query, "B", "C", "D", "E", "F", "G", "H", "I", "J");
    final int savedClauseCount = BooleanQuery.getMaxClauseCount();
    try {
      BooleanQuery.setMaxClauseCount(3);
      checkBooleanTerms(searcher, query, "B", "C", "D");
    } finally {
      BooleanQuery.setMaxClauseCount(savedClauseCount);
    }
    searcher.close();
  }
  private void checkBooleanTerms(Searcher searcher, TermRangeQuery query, String... terms) throws IOException {
    query.setRewriteMethod(new MultiTermQuery.TopTermsScoringBooleanQueryRewrite());
    final BooleanQuery bq = (BooleanQuery) searcher.rewrite(query);
    final Set<String> allowedTerms = new HashSet<String>(Arrays.asList(terms));
    assertEquals(allowedTerms.size(), bq.clauses().size());
    for (BooleanClause c : bq.clauses()) {
      assertTrue(c.getQuery() instanceof TermQuery);
      final TermQuery tq = (TermQuery) c.getQuery();
      final String term = tq.getTerm().text();
      assertTrue("invalid term: "+ term, allowedTerms.contains(term));
      allowedTerms.remove(term); 
    }
    assertEquals(0, allowedTerms.size());
  }
  public void testEqualsHashcode() {
    Query query = new TermRangeQuery("content", "A", "C", true, true);
    query.setBoost(1.0f);
    Query other = new TermRangeQuery("content", "A", "C", true, true);
    other.setBoost(1.0f);
    assertEquals("query equals itself is true", query, query);
    assertEquals("equivalent queries are equal", query, other);
    assertEquals("hashcode must return same value when equals is true", query.hashCode(), other.hashCode());
    other.setBoost(2.0f);
    assertFalse("Different boost queries are not equal", query.equals(other));
    other = new TermRangeQuery("notcontent", "A", "C", true, true);
    assertFalse("Different fields are not equal", query.equals(other));
    other = new TermRangeQuery("content", "X", "C", true, true);
    assertFalse("Different lower terms are not equal", query.equals(other));
    other = new TermRangeQuery("content", "A", "Z", true, true);
    assertFalse("Different upper terms are not equal", query.equals(other));
    query = new TermRangeQuery("content", null, "C", true, true);
    other = new TermRangeQuery("content", null, "C", true, true);
    assertEquals("equivalent queries with null lowerterms are equal()", query, other);
    assertEquals("hashcode must return same value when equals is true", query.hashCode(), other.hashCode());
    query = new TermRangeQuery("content", "C", null, true, true);
    other = new TermRangeQuery("content", "C", null, true, true);
    assertEquals("equivalent queries with null upperterms are equal()", query, other);
    assertEquals("hashcode returns same value", query.hashCode(), other.hashCode());
    query = new TermRangeQuery("content", null, "C", true, true);
    other = new TermRangeQuery("content", "C", null, true, true);
    assertFalse("queries with different upper and lower terms are not equal", query.equals(other));
    query = new TermRangeQuery("content", "A", "C", false, false);
    other = new TermRangeQuery("content", "A", "C", true, true);
    assertFalse("queries with different inclusive are not equal", query.equals(other));
    query = new TermRangeQuery("content", "A", "C", false, false);
    other = new TermRangeQuery("content", "A", "C", false, false, Collator.getInstance());
    assertFalse("a query with a collator is not equal to one without", query.equals(other));
  }
  public void testExclusiveCollating() throws Exception {
    Query query = new TermRangeQuery("content", "A", "C", false, false, Collator.getInstance(Locale.ENGLISH));
    initializeIndex(new String[] {"A", "B", "C", "D"});
    IndexSearcher searcher = new IndexSearcher(dir, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("A,B,C,D, only B in range", 1, hits.length);
    searcher.close();
    initializeIndex(new String[] {"A", "B", "D"});
    searcher = new IndexSearcher(dir, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("A,B,D, only B in range", 1, hits.length);
    searcher.close();
    addDoc("C");
    searcher = new IndexSearcher(dir, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("C added, still only B in range", 1, hits.length);
    searcher.close();
  }
  public void testInclusiveCollating() throws Exception {
    Query query = new TermRangeQuery("content", "A", "C",true, true, Collator.getInstance(Locale.ENGLISH));
    initializeIndex(new String[]{"A", "B", "C", "D"});
    IndexSearcher searcher = new IndexSearcher(dir, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("A,B,C,D - A,B,C in range", 3, hits.length);
    searcher.close();
    initializeIndex(new String[]{"A", "B", "D"});
    searcher = new IndexSearcher(dir, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("A,B,D - A and B in range", 2, hits.length);
    searcher.close();
    addDoc("C");
    searcher = new IndexSearcher(dir, true);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("C added - A, B, C in range", 3, hits.length);
    searcher.close();
  }
  public void testFarsi() throws Exception {
    Collator collator = Collator.getInstance(new Locale("ar"));
    Query query = new TermRangeQuery("content", "\u062F", "\u0698", true, true, collator);
    initializeIndex(new String[]{ "\u0633\u0627\u0628"});
    IndexSearcher searcher = new IndexSearcher(dir, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("The index Term should not be included.", 0, hits.length);
    query = new TermRangeQuery("content", "\u0633", "\u0638",true, true, collator);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("The index Term should be included.", 1, hits.length);
    searcher.close();
  }
  public void testDanish() throws Exception {
    Collator collator = Collator.getInstance(new Locale("da", "dk"));
    String[] words = { "H\u00D8T", "H\u00C5T", "MAND" };
    Query query = new TermRangeQuery("content", "H\u00D8T", "MAND", false, false, collator);
    initializeIndex(words);
    IndexSearcher searcher = new IndexSearcher(dir, true);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("The index Term should be included.", 1, hits.length);
    query = new TermRangeQuery("content", "H\u00C5T", "MAND", false, false, collator);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("The index Term should not be included.", 0, hits.length);
    searcher.close();
  }
  private static class SingleCharAnalyzer extends Analyzer {
    private static class SingleCharTokenizer extends Tokenizer {
      char[] buffer = new char[1];
      boolean done;
      TermAttribute termAtt;
      public SingleCharTokenizer(Reader r) {
        super(r);
        termAtt = addAttribute(TermAttribute.class);
      }
      @Override
      public boolean incrementToken() throws IOException {
        int count = input.read(buffer);
        if (done)
          return false;
        else {
          clearAttributes();
          done = true;
          if (count == 1) {
            termAtt.termBuffer()[0] = buffer[0];
            termAtt.setTermLength(1);
          } else
            termAtt.setTermLength(0);
          return true;
        }
      }
      @Override
      public final void reset(Reader reader) throws IOException {
        super.reset(reader);
        done = false;
      }
    }
    @Override
    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
      Tokenizer tokenizer = (Tokenizer) getPreviousTokenStream();
      if (tokenizer == null) {
        tokenizer = new SingleCharTokenizer(reader);
        setPreviousTokenStream(tokenizer);
      } else
        tokenizer.reset(reader);
      return tokenizer;
    }
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      return new SingleCharTokenizer(reader);
    }
  }
  private void initializeIndex(String[] values) throws IOException {
    initializeIndex(values, new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
  }
  private void initializeIndex(String[] values, Analyzer analyzer) throws IOException {
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer).setOpenMode(OpenMode.CREATE));
    for (int i = 0; i < values.length; i++) {
      insertDoc(writer, values[i]);
    }
    writer.close();
  }
  private void addDoc(String content) throws IOException {
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    insertDoc(writer, content);
    writer.close();
  }
  private void insertDoc(IndexWriter writer, String content) throws IOException {
    Document doc = new Document();
    doc.add(new Field("id", "id" + docCount, Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("content", content, Field.Store.NO, Field.Index.ANALYZED));
    writer.addDocument(doc);
    docCount++;
  }
  public void testExclusiveLowerNull() throws Exception {
    Analyzer analyzer = new SingleCharAnalyzer();
    Query query = new TermRangeQuery("content", null, "C",
                                 false, false);
    initializeIndex(new String[] {"A", "B", "", "C", "D"}, analyzer);
    IndexSearcher searcher = new IndexSearcher(dir, true);
    int numHits = searcher.search(query, null, 1000).totalHits;
    assertEquals("A,B,<empty string>,C,D => A, B & <empty string> are in range", 3, numHits);
    searcher.close();
    initializeIndex(new String[] {"A", "B", "", "D"}, analyzer);
    searcher = new IndexSearcher(dir, true);
    numHits = searcher.search(query, null, 1000).totalHits;
    assertEquals("A,B,<empty string>,D => A, B & <empty string> are in range", 3, numHits);
    searcher.close();
    addDoc("C");
    searcher = new IndexSearcher(dir, true);
    numHits = searcher.search(query, null, 1000).totalHits;
    assertEquals("C added, still A, B & <empty string> are in range", 3, numHits);
    searcher.close();
  }
  public void testInclusiveLowerNull() throws Exception {
    Analyzer analyzer = new SingleCharAnalyzer();
    Query query = new TermRangeQuery("content", null, "C", true, true);
    initializeIndex(new String[]{"A", "B", "","C", "D"}, analyzer);
    IndexSearcher searcher = new IndexSearcher(dir, true);
    int numHits = searcher.search(query, null, 1000).totalHits;
    assertEquals("A,B,<empty string>,C,D => A,B,<empty string>,C in range", 4, numHits);
    searcher.close();
    initializeIndex(new String[]{"A", "B", "", "D"}, analyzer);
    searcher = new IndexSearcher(dir, true);
    numHits = searcher.search(query, null, 1000).totalHits;
    assertEquals("A,B,<empty string>,D - A, B and <empty string> in range", 3, numHits);
    searcher.close();
    addDoc("C");
    searcher = new IndexSearcher(dir, true);
    numHits = searcher.search(query, null, 1000).totalHits;
    assertEquals("C added => A,B,<empty string>,C in range", 4, numHits);
     searcher.close();
  }
}
