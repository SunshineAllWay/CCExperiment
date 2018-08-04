package org.apache.lucene.search;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.DocIdBitSet;
import java.util.BitSet;
public class TestFilteredQuery extends LuceneTestCase {
  private IndexSearcher searcher;
  private RAMDirectory directory;
  private Query query;
  private Filter filter;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter (directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add (new Field("field", "one two three four five", Field.Store.YES, Field.Index.ANALYZED));
    doc.add (new Field("sorter", "b", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument (doc);
    doc = new Document();
    doc.add (new Field("field", "one two three four", Field.Store.YES, Field.Index.ANALYZED));
    doc.add (new Field("sorter", "d", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument (doc);
    doc = new Document();
    doc.add (new Field("field", "one two three y", Field.Store.YES, Field.Index.ANALYZED));
    doc.add (new Field("sorter", "a", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument (doc);
    doc = new Document();
    doc.add (new Field("field", "one two x", Field.Store.YES, Field.Index.ANALYZED));
    doc.add (new Field("sorter", "c", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument (doc);
    writer.optimize ();
    writer.close ();
    searcher = new IndexSearcher (directory, true);
    query = new TermQuery (new Term ("field", "three"));
    filter = newStaticFilterB();
  }
  private static Filter newStaticFilterB() {
    return new Filter() {
      @Override
      public DocIdSet getDocIdSet (IndexReader reader) {
        BitSet bitset = new BitSet(5);
        bitset.set (1);
        bitset.set (3);
        return new DocIdBitSet(bitset);
      }
    };
  }
  @Override
  protected void tearDown() throws Exception {
    searcher.close();
    directory.close();
    super.tearDown();
  }
  public void testFilteredQuery()
  throws Exception {
    Query filteredquery = new FilteredQuery (query, filter);
    ScoreDoc[] hits = searcher.search (filteredquery, null, 1000).scoreDocs;
    assertEquals (1, hits.length);
    assertEquals (1, hits[0].doc);
    QueryUtils.check(filteredquery,searcher);
    hits = searcher.search (filteredquery, null, 1000, new Sort(new SortField("sorter", SortField.STRING))).scoreDocs;
    assertEquals (1, hits.length);
    assertEquals (1, hits[0].doc);
    filteredquery = new FilteredQuery (new TermQuery (new Term ("field", "one")), filter);
    hits = searcher.search (filteredquery, null, 1000).scoreDocs;
    assertEquals (2, hits.length);
    QueryUtils.check(filteredquery,searcher);
    filteredquery = new FilteredQuery (new TermQuery (new Term ("field", "x")), filter);
    hits = searcher.search (filteredquery, null, 1000).scoreDocs;
    assertEquals (1, hits.length);
    assertEquals (3, hits[0].doc);
    QueryUtils.check(filteredquery,searcher);
    filteredquery = new FilteredQuery (new TermQuery (new Term ("field", "y")), filter);
    hits = searcher.search (filteredquery, null, 1000).scoreDocs;
    assertEquals (0, hits.length);
    QueryUtils.check(filteredquery,searcher);
    Filter f = newStaticFilterA();
    float boost = 2.5f;
    BooleanQuery bq1 = new BooleanQuery();
    TermQuery tq = new TermQuery (new Term ("field", "one"));
    tq.setBoost(boost);
    bq1.add(tq, Occur.MUST);
    bq1.add(new TermQuery (new Term ("field", "five")), Occur.MUST);
    BooleanQuery bq2 = new BooleanQuery();
    tq = new TermQuery (new Term ("field", "one"));
    filteredquery = new FilteredQuery(tq, f);
    filteredquery.setBoost(boost);
    bq2.add(filteredquery, Occur.MUST);
    bq2.add(new TermQuery (new Term ("field", "five")), Occur.MUST);
    assertScoreEquals(bq1, bq2);
    assertEquals(boost, filteredquery.getBoost(), 0);
    assertEquals(1.0f, tq.getBoost(), 0); 
  }
  private static Filter newStaticFilterA() {
    return new Filter() {
      @Override
      public DocIdSet getDocIdSet (IndexReader reader) {
        BitSet bitset = new BitSet(5);
        bitset.set(0, 5);
        return new DocIdBitSet(bitset);
      }
    };
  }
  public void assertScoreEquals(Query q1, Query q2) throws Exception {
    ScoreDoc[] hits1 = searcher.search (q1, null, 1000).scoreDocs;
    ScoreDoc[] hits2 = searcher.search (q2, null, 1000).scoreDocs;
    assertEquals(hits1.length, hits2.length);
    for (int i = 0; i < hits1.length; i++) {
      assertEquals(hits1[i].score, hits2[i].score, 0.0000001f);
    }
  }
  public void testRangeQuery() throws Exception {
    TermRangeQuery rq = new TermRangeQuery(
        "sorter", "b", "d", true, true);
    Query filteredquery = new FilteredQuery(rq, filter);
    ScoreDoc[] hits = searcher.search(filteredquery, null, 1000).scoreDocs;
    assertEquals(2, hits.length);
    QueryUtils.check(filteredquery,searcher);
  }
  public void testBoolean() throws Exception {
    BooleanQuery bq = new BooleanQuery();
    Query query = new FilteredQuery(new MatchAllDocsQuery(),
        new SingleDocTestFilter(0));
    bq.add(query, BooleanClause.Occur.MUST);
    query = new FilteredQuery(new MatchAllDocsQuery(),
        new SingleDocTestFilter(1));
    bq.add(query, BooleanClause.Occur.MUST);
    ScoreDoc[] hits = searcher.search(bq, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    QueryUtils.check(query,searcher);    
  }
  public void testBoolean2() throws Exception {
    BooleanQuery bq = new BooleanQuery();
    Query query = new FilteredQuery(bq,
        new SingleDocTestFilter(0));
    bq.add(new TermQuery(new Term("field", "one")), BooleanClause.Occur.SHOULD);
    bq.add(new TermQuery(new Term("field", "two")), BooleanClause.Occur.SHOULD);
    ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
    assertEquals(1, hits.length);
    QueryUtils.check(query,searcher);    
  }
}