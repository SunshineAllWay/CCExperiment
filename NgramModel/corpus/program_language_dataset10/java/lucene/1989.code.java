package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.RAMDirectory;
import java.io.IOException;
public class TestWildcard
    extends LuceneTestCase {
  public void testEquals() {
    WildcardQuery wq1 = new WildcardQuery(new Term("field", "b*a"));
    WildcardQuery wq2 = new WildcardQuery(new Term("field", "b*a"));
    WildcardQuery wq3 = new WildcardQuery(new Term("field", "b*a"));
    assertEquals(wq1, wq2);
    assertEquals(wq2, wq1);
    assertEquals(wq2, wq3);
    assertEquals(wq1, wq3);
    assertFalse(wq1.equals(null));
    FuzzyQuery fq = new FuzzyQuery(new Term("field", "b*a"));
    assertFalse(wq1.equals(fq));
    assertFalse(fq.equals(wq1));
  }
  public void testTermWithoutWildcard() throws IOException {
      RAMDirectory indexStore = getIndexStore("field", new String[]{"nowildcard", "nowildcardx"});
      IndexSearcher searcher = new IndexSearcher(indexStore, true);
      MultiTermQuery wq = new WildcardQuery(new Term("field", "nowildcard"));
      assertMatches(searcher, wq, 1);
      wq.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
      wq.setBoost(0.1F);
      Query q = searcher.rewrite(wq);
      assertTrue(q instanceof TermQuery);
      assertEquals(q.getBoost(), wq.getBoost());
      wq.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_FILTER_REWRITE);
      wq.setBoost(0.2F);
      q = searcher.rewrite(wq);
      assertTrue(q instanceof ConstantScoreQuery);
      assertEquals(q.getBoost(), wq.getBoost());
      wq.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
      wq.setBoost(0.3F);
      q = searcher.rewrite(wq);
      assertTrue(q instanceof ConstantScoreQuery);
      assertEquals(q.getBoost(), wq.getBoost());
      wq.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE);
      wq.setBoost(0.4F);
      q = searcher.rewrite(wq);
      assertTrue(q instanceof ConstantScoreQuery);
      assertEquals(q.getBoost(), wq.getBoost());
  }
  public void testEmptyTerm() throws IOException {
    RAMDirectory indexStore = getIndexStore("field", new String[]{"nowildcard", "nowildcardx"});
    IndexSearcher searcher = new IndexSearcher(indexStore, true);
    MultiTermQuery wq = new WildcardQuery(new Term("field", ""));
    wq.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
    assertMatches(searcher, wq, 0);
    Query q = searcher.rewrite(wq);
    assertTrue(q instanceof BooleanQuery);
    assertEquals(0, ((BooleanQuery) q).clauses().size());
  }
  public void testPrefixTerm() throws IOException {
    RAMDirectory indexStore = getIndexStore("field", new String[]{"prefix", "prefixx"});
    IndexSearcher searcher = new IndexSearcher(indexStore, true);
    MultiTermQuery wq = new WildcardQuery(new Term("field", "prefix*"));
    assertMatches(searcher, wq, 2);
    MultiTermQuery expected = new PrefixQuery(new Term("field", "prefix"));
    wq.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
    wq.setBoost(0.1F);
    expected.setRewriteMethod(wq.getRewriteMethod());
    expected.setBoost(wq.getBoost());
    assertEquals(searcher.rewrite(expected), searcher.rewrite(wq));
    wq.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_FILTER_REWRITE);
    wq.setBoost(0.2F);
    expected.setRewriteMethod(wq.getRewriteMethod());
    expected.setBoost(wq.getBoost());
    assertEquals(searcher.rewrite(expected), searcher.rewrite(wq));
    wq.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    wq.setBoost(0.3F);
    expected.setRewriteMethod(wq.getRewriteMethod());
    expected.setBoost(wq.getBoost());
    assertEquals(searcher.rewrite(expected), searcher.rewrite(wq));
    wq.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE);
    wq.setBoost(0.4F);
    expected.setRewriteMethod(wq.getRewriteMethod());
    expected.setBoost(wq.getBoost());
    assertEquals(searcher.rewrite(expected), searcher.rewrite(wq));
  }
  public void testAsterisk()
      throws IOException {
    RAMDirectory indexStore = getIndexStore("body", new String[]
    {"metal", "metals"});
    IndexSearcher searcher = new IndexSearcher(indexStore, true);
    Query query1 = new TermQuery(new Term("body", "metal"));
    Query query2 = new WildcardQuery(new Term("body", "metal*"));
    Query query3 = new WildcardQuery(new Term("body", "m*tal"));
    Query query4 = new WildcardQuery(new Term("body", "m*tal*"));
    Query query5 = new WildcardQuery(new Term("body", "m*tals"));
    BooleanQuery query6 = new BooleanQuery();
    query6.add(query5, BooleanClause.Occur.SHOULD);
    BooleanQuery query7 = new BooleanQuery();
    query7.add(query3, BooleanClause.Occur.SHOULD);
    query7.add(query5, BooleanClause.Occur.SHOULD);
    Query query8 = new WildcardQuery(new Term("body", "M*tal*"));
    assertMatches(searcher, query1, 1);
    assertMatches(searcher, query2, 2);
    assertMatches(searcher, query3, 1);
    assertMatches(searcher, query4, 2);
    assertMatches(searcher, query5, 1);
    assertMatches(searcher, query6, 1);
    assertMatches(searcher, query7, 2);
    assertMatches(searcher, query8, 0);
    assertMatches(searcher, new WildcardQuery(new Term("body", "*tall")), 0);
    assertMatches(searcher, new WildcardQuery(new Term("body", "*tal")), 1);
    assertMatches(searcher, new WildcardQuery(new Term("body", "*tal*")), 2);
  }
  public void testQuestionmark()
      throws IOException {
    RAMDirectory indexStore = getIndexStore("body", new String[]
    {"metal", "metals", "mXtals", "mXtXls"});
    IndexSearcher searcher = new IndexSearcher(indexStore, true);
    Query query1 = new WildcardQuery(new Term("body", "m?tal"));
    Query query2 = new WildcardQuery(new Term("body", "metal?"));
    Query query3 = new WildcardQuery(new Term("body", "metals?"));
    Query query4 = new WildcardQuery(new Term("body", "m?t?ls"));
    Query query5 = new WildcardQuery(new Term("body", "M?t?ls"));
    Query query6 = new WildcardQuery(new Term("body", "meta??"));
    assertMatches(searcher, query1, 1); 
    assertMatches(searcher, query2, 1);
    assertMatches(searcher, query3, 0);
    assertMatches(searcher, query4, 3);
    assertMatches(searcher, query5, 0);
    assertMatches(searcher, query6, 1); 
  }
  private RAMDirectory getIndexStore(String field, String[] contents)
      throws IOException {
    RAMDirectory indexStore = new RAMDirectory();
    IndexWriter writer = new IndexWriter(indexStore, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < contents.length; ++i) {
      Document doc = new Document();
      doc.add(new Field(field, contents[i], Field.Store.YES, Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.optimize();
    writer.close();
    return indexStore;
  }
  private void assertMatches(IndexSearcher searcher, Query q, int expectedMatches)
      throws IOException {
    ScoreDoc[] result = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(expectedMatches, result.length);
  }
  public void testParsingAndSearching() throws Exception {
    String field = "content";
    QueryParser qp = new QueryParser(TEST_VERSION_CURRENT, field, new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    qp.setAllowLeadingWildcard(true);
    String docs[] = {
        "\\ abcdefg1",
        "\\79 hijklmn1",
        "\\\\ opqrstu1",
    };
    String matchAll[] = {
        "*", "*1", "**1", "*?", "*?1", "?*1", "**", "***", "\\\\*"
    };
    String matchNone[] = {
        "a*h", "a?h", "*a*h", "?a", "a?",
    };
    String matchOneDocPrefix[][] = {
        {"a*", "ab*", "abc*", }, 
        {"h*", "hi*", "hij*", "\\\\7*"}, 
        {"o*", "op*", "opq*", "\\\\\\\\*"}, 
    };
    String matchOneDocWild[][] = {
        {"*a*", "*ab*", "*abc**", "ab*e*", "*g?", "*f?1", "abc**"}, 
        {"*h*", "*hi*", "*hij**", "hi*k*", "*n?", "*m?1", "hij**"}, 
        {"*o*", "*op*", "*opq**", "op*q*", "*u?", "*t?1", "opq**"}, 
    };
    RAMDirectory dir = new RAMDirectory();
    IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < docs.length; i++) {
      Document doc = new Document();
      doc.add(new Field(field,docs[i],Store.NO,Index.ANALYZED));
      iw.addDocument(doc);
    }
    iw.close();
    IndexSearcher searcher = new IndexSearcher(dir, true);
    for (int i = 0; i < matchAll.length; i++) {
      String qtxt = matchAll[i];
      Query q = qp.parse(qtxt);
      if (VERBOSE) System.out.println("matchAll: qtxt="+qtxt+" q="+q+" "+q.getClass().getName());
      ScoreDoc[] hits = searcher.search(q, null, 1000).scoreDocs;
      assertEquals(docs.length,hits.length);
    }
    for (int i = 0; i < matchNone.length; i++) {
      String qtxt = matchNone[i];
      Query q = qp.parse(qtxt);
      if (VERBOSE) System.out.println("matchNone: qtxt="+qtxt+" q="+q+" "+q.getClass().getName());
      ScoreDoc[] hits = searcher.search(q, null, 1000).scoreDocs;
      assertEquals(0,hits.length);
    }
    for (int i = 0; i < matchOneDocPrefix.length; i++) {
      for (int j = 0; j < matchOneDocPrefix[i].length; j++) {
        String qtxt = matchOneDocPrefix[i][j];
        Query q = qp.parse(qtxt);
        if (VERBOSE) System.out.println("match 1 prefix: doc="+docs[i]+" qtxt="+qtxt+" q="+q+" "+q.getClass().getName());
        assertEquals(PrefixQuery.class, q.getClass());
        ScoreDoc[] hits = searcher.search(q, null, 1000).scoreDocs;
        assertEquals(1,hits.length);
        assertEquals(i,hits[0].doc);
      }
    }
    for (int i = 0; i < matchOneDocPrefix.length; i++) {
      for (int j = 0; j < matchOneDocWild[i].length; j++) {
        String qtxt = matchOneDocWild[i][j];
        Query q = qp.parse(qtxt);
        if (VERBOSE) System.out.println("match 1 wild: doc="+docs[i]+" qtxt="+qtxt+" q="+q+" "+q.getClass().getName());
        assertEquals(WildcardQuery.class, q.getClass());
        ScoreDoc[] hits = searcher.search(q, null, 1000).scoreDocs;
        assertEquals(1,hits.length);
        assertEquals(i,hits[0].doc);
      }
    }
    searcher.close();
  }
}
