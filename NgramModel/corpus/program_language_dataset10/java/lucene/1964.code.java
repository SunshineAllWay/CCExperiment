package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import java.io.IOException;
import java.io.Reader;
public class TestPhraseQuery extends LuceneTestCase {
  public static final float SCORE_COMP_THRESH = 1e-6f;
  private IndexSearcher searcher;
  private PhraseQuery query;
  private RAMDirectory directory;
  @Override
  public void setUp() throws Exception {
    super.setUp();
    directory = new RAMDirectory();
    Analyzer analyzer = new Analyzer() {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader);
      }
      @Override
      public int getPositionIncrementGap(String fieldName) {
        return 100;
      }
    };
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, analyzer));
    Document doc = new Document();
    doc.add(new Field("field", "one two three four five", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("repeated", "this is a repeated field - first part", Field.Store.YES, Field.Index.ANALYZED));
    Fieldable repeatedField = new Field("repeated", "second part of a repeated field", Field.Store.YES, Field.Index.ANALYZED);
    doc.add(repeatedField);
    doc.add(new Field("palindrome", "one two three two one", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("nonexist", "phrase exist notexist exist found", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("nonexist", "phrase exist notexist exist found", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.optimize();
    writer.close();
    searcher = new IndexSearcher(directory, true);
    query = new PhraseQuery();
  }
  @Override
  protected void tearDown() throws Exception {
    searcher.close();
    directory.close();
    super.tearDown();
  }
  public void testNotCloseEnough() throws Exception {
    query.setSlop(2);
    query.add(new Term("field", "one"));
    query.add(new Term("field", "five"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testBarelyCloseEnough() throws Exception {
    query.setSlop(3);
    query.add(new Term("field", "one"));
    query.add(new Term("field", "five"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testExact() throws Exception {
    query.add(new Term("field", "four"));
    query.add(new Term("field", "five"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("exact match", 1, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.add(new Term("field", "two"));
    query.add(new Term("field", "one"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("reverse not exact", 0, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testSlop1() throws Exception {
    query.setSlop(1);
    query.add(new Term("field", "one"));
    query.add(new Term("field", "two"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("in order", 1, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.setSlop(1);
    query.add(new Term("field", "two"));
    query.add(new Term("field", "one"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("reversed, slop not 2 or more", 0, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testOrderDoesntMatter() throws Exception {
    query.setSlop(2); 
    query.add(new Term("field", "two"));
    query.add(new Term("field", "one"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("just sloppy enough", 1, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.setSlop(2);
    query.add(new Term("field", "three"));
    query.add(new Term("field", "one"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("not sloppy enough", 0, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testMulipleTerms() throws Exception {
    query.setSlop(2);
    query.add(new Term("field", "one"));
    query.add(new Term("field", "three"));
    query.add(new Term("field", "five"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("two total moves", 1, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.setSlop(5); 
    query.add(new Term("field", "five"));
    query.add(new Term("field", "three"));
    query.add(new Term("field", "one"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("slop of 5 not close enough", 0, hits.length);
    QueryUtils.check(query,searcher);
    query.setSlop(6);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("slop of 6 just right", 1, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testPhraseQueryWithStopAnalyzer() throws Exception {
    RAMDirectory directory = new RAMDirectory();
    StopAnalyzer stopAnalyzer = new StopAnalyzer(Version.LUCENE_24);
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        Version.LUCENE_24, stopAnalyzer));
    Document doc = new Document();
    doc.add(new Field("field", "the stop words are here", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.close();
    IndexSearcher searcher = new IndexSearcher(directory, true);
    PhraseQuery query = new PhraseQuery();
    query.add(new Term("field","stop"));
    query.add(new Term("field","words"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.add(new Term("field", "words"));
    query.add(new Term("field", "here"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    QueryUtils.check(query,searcher);
    searcher.close();
  }
  public void testPhraseQueryInConjunctionScorer() throws Exception {
    RAMDirectory directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("source", "marketing info", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("contents", "foobar", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("source", "marketing info", Field.Store.YES, Field.Index.ANALYZED)); 
    writer.addDocument(doc);
    writer.optimize();
    writer.close();
    IndexSearcher searcher = new IndexSearcher(directory, true);
    PhraseQuery phraseQuery = new PhraseQuery();
    phraseQuery.add(new Term("source", "marketing"));
    phraseQuery.add(new Term("source", "info"));
    ScoreDoc[] hits = searcher.search(phraseQuery, null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    QueryUtils.check(phraseQuery,searcher);
    TermQuery termQuery = new TermQuery(new Term("contents","foobar"));
    BooleanQuery booleanQuery = new BooleanQuery();
    booleanQuery.add(termQuery, BooleanClause.Occur.MUST);
    booleanQuery.add(phraseQuery, BooleanClause.Occur.MUST);
    hits = searcher.search(booleanQuery, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    QueryUtils.check(termQuery,searcher);
    searcher.close();
    writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
    doc = new Document();
    doc.add(new Field("contents", "map entry woo", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("contents", "woo map entry", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("contents", "map foobarword entry woo", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.optimize();
    writer.close();
    searcher = new IndexSearcher(directory, true);
    termQuery = new TermQuery(new Term("contents","woo"));
    phraseQuery = new PhraseQuery();
    phraseQuery.add(new Term("contents","map"));
    phraseQuery.add(new Term("contents","entry"));
    hits = searcher.search(termQuery, null, 1000).scoreDocs;
    assertEquals(3, hits.length);
    hits = searcher.search(phraseQuery, null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    booleanQuery = new BooleanQuery();
    booleanQuery.add(termQuery, BooleanClause.Occur.MUST);
    booleanQuery.add(phraseQuery, BooleanClause.Occur.MUST);
    hits = searcher.search(booleanQuery, null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    booleanQuery = new BooleanQuery();
    booleanQuery.add(phraseQuery, BooleanClause.Occur.MUST);
    booleanQuery.add(termQuery, BooleanClause.Occur.MUST);
    hits = searcher.search(booleanQuery, null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    QueryUtils.check(booleanQuery,searcher);
    searcher.close();
    directory.close();
  }
  public void testSlopScoring() throws IOException {
    Directory directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("field", "foo firstname lastname foo", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    Document doc2 = new Document();
    doc2.add(new Field("field", "foo firstname xxx lastname foo", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc2);
    Document doc3 = new Document();
    doc3.add(new Field("field", "foo firstname xxx yyy lastname foo", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc3);
    writer.optimize();
    writer.close();
    Searcher searcher = new IndexSearcher(directory, true);
    PhraseQuery query = new PhraseQuery();
    query.add(new Term("field", "firstname"));
    query.add(new Term("field", "lastname"));
    query.setSlop(Integer.MAX_VALUE);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals(3, hits.length);
    assertEquals(0.71, hits[0].score, 0.01);
    assertEquals(0, hits[0].doc);
    assertEquals(0.44, hits[1].score, 0.01);
    assertEquals(1, hits[1].doc);
    assertEquals(0.31, hits[2].score, 0.01);
    assertEquals(2, hits[2].doc);
    QueryUtils.check(query,searcher);        
  }
  public void testToString() throws Exception {
    StopAnalyzer analyzer = new StopAnalyzer(TEST_VERSION_CURRENT);
    QueryParser qp = new QueryParser(TEST_VERSION_CURRENT, "field", analyzer);
    qp.setEnablePositionIncrements(true);
    PhraseQuery q = (PhraseQuery)qp.parse("\"this hi this is a test is\"");
    assertEquals("field:\"? hi ? ? ? test\"", q.toString());
    q.add(new Term("field", "hello"), 1);
    assertEquals("field:\"? hi|hello ? ? ? test\"", q.toString());
  }
  public void testWrappedPhrase() throws IOException {
    query.add(new Term("repeated", "first"));
    query.add(new Term("repeated", "part"));
    query.add(new Term("repeated", "second"));
    query.add(new Term("repeated", "part"));
    query.setSlop(100);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("slop of 100 just right", 1, hits.length);
    QueryUtils.check(query,searcher);
    query.setSlop(99);
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("slop of 99 not enough", 0, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testNonExistingPhrase() throws IOException {
    query.add(new Term("nonexist", "phrase"));
    query.add(new Term("nonexist", "notexist"));
    query.add(new Term("nonexist", "found"));
    query.setSlop(2); 
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("phrase without repetitions exists in 2 docs", 2, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.add(new Term("nonexist", "phrase"));
    query.add(new Term("nonexist", "exist"));
    query.add(new Term("nonexist", "exist"));
    query.setSlop(1); 
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("phrase with repetitions exists in two docs", 2, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.add(new Term("nonexist", "phrase"));
    query.add(new Term("nonexist", "notexist"));
    query.add(new Term("nonexist", "phrase"));
    query.setSlop(1000); 
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("nonexisting phrase with repetitions does not exist in any doc", 0, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.add(new Term("nonexist", "phrase"));
    query.add(new Term("nonexist", "exist"));
    query.add(new Term("nonexist", "exist"));
    query.add(new Term("nonexist", "exist"));
    query.setSlop(1000); 
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("nonexisting phrase with repetitions does not exist in any doc", 0, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testPalyndrome2() throws Exception {
    query.setSlop(0); 
    query.add(new Term("field", "two"));
    query.add(new Term("field", "three"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("phrase found with exact phrase scorer", 1, hits.length);
    float score0 = hits[0].score;
    QueryUtils.check(query,searcher);
    query.setSlop(2); 
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("just sloppy enough", 1, hits.length);
    float score1 = hits[0].score;
    assertEquals("exact scorer and sloppy scorer score the same when slop does not matter",score0, score1, SCORE_COMP_THRESH);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.setSlop(2); 
    query.add(new Term("palindrome", "two"));
    query.add(new Term("palindrome", "three"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("just sloppy enough", 1, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.setSlop(2); 
    query.add(new Term("palindrome", "three"));
    query.add(new Term("palindrome", "two"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("just sloppy enough", 1, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testPalyndrome3() throws Exception {
    query.setSlop(0); 
    query.add(new Term("field", "one"));
    query.add(new Term("field", "two"));
    query.add(new Term("field", "three"));
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("phrase found with exact phrase scorer", 1, hits.length);
    float score0 = hits[0].score;
    QueryUtils.check(query,searcher);
    query.setSlop(4); 
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("just sloppy enough", 1, hits.length);
    float score1 = hits[0].score;
    assertEquals("exact scorer and sloppy scorer score the same when slop does not matter",score0, score1, SCORE_COMP_THRESH);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.setSlop(4); 
    query.add(new Term("palindrome", "one"));
    query.add(new Term("palindrome", "two"));
    query.add(new Term("palindrome", "three"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("just sloppy enough", 1, hits.length);
    QueryUtils.check(query,searcher);
    query = new PhraseQuery();
    query.setSlop(4); 
    query.add(new Term("palindrome", "three"));
    query.add(new Term("palindrome", "two"));
    query.add(new Term("palindrome", "one"));
    hits = searcher.search(query, null, 1000).scoreDocs;
    assertEquals("just sloppy enough", 1, hits.length);
    QueryUtils.check(query,searcher);
  }
  public void testEmptyPhraseQuery() throws Throwable {
    final BooleanQuery q2 = new BooleanQuery();
    q2.add(new PhraseQuery(), BooleanClause.Occur.MUST);
    q2.toString();
  }
}
