package org.apache.lucene.search.function;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
public class TestCustomScoreQuery extends FunctionTestSetup {
  public TestCustomScoreQuery() {
    super(true);
  }
  @Test
  public void testCustomScoreByte() throws Exception, ParseException {
    doTestCustomScore(INT_FIELD, FieldScoreQuery.Type.BYTE, 1.0);
    doTestCustomScore(INT_FIELD, FieldScoreQuery.Type.BYTE, 2.0);
  }
  @Test
  public void testCustomScoreShort() throws Exception, ParseException {
    doTestCustomScore(INT_FIELD, FieldScoreQuery.Type.SHORT, 1.0);
    doTestCustomScore(INT_FIELD, FieldScoreQuery.Type.SHORT, 3.0);
  }
  @Test
  public void testCustomScoreInt() throws Exception, ParseException {
    doTestCustomScore(INT_FIELD, FieldScoreQuery.Type.INT, 1.0);
    doTestCustomScore(INT_FIELD, FieldScoreQuery.Type.INT, 4.0);
  }
  @Test
  public void testCustomScoreFloat() throws Exception, ParseException {
    doTestCustomScore(INT_FIELD, FieldScoreQuery.Type.FLOAT, 1.0);
    doTestCustomScore(INT_FIELD, FieldScoreQuery.Type.FLOAT, 5.0);
    doTestCustomScore(FLOAT_FIELD, FieldScoreQuery.Type.FLOAT, 1.0);
    doTestCustomScore(FLOAT_FIELD, FieldScoreQuery.Type.FLOAT, 6.0);
  }
  private static class CustomAddQuery extends CustomScoreQuery {
    CustomAddQuery(Query q, ValueSourceQuery qValSrc) {
      super(q, qValSrc);
    }
    @Override
    public String name() {
      return "customAdd";
    }
    @Override
    protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) {
      return new CustomScoreProvider(reader) {
        @Override
        public float customScore(int doc, float subQueryScore, float valSrcScore) {
          return subQueryScore + valSrcScore;
        }
        @Override
        public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation valSrcExpl) {
          float valSrcScore = valSrcExpl == null ? 0 : valSrcExpl.getValue();
          Explanation exp = new Explanation(valSrcScore + subQueryExpl.getValue(), "custom score: sum of:");
          exp.addDetail(subQueryExpl);
          if (valSrcExpl != null) {
            exp.addDetail(valSrcExpl);
          }
          return exp;
        }
      };
    }
  }
  private static class CustomMulAddQuery extends CustomScoreQuery {
    CustomMulAddQuery(Query q, ValueSourceQuery qValSrc1, ValueSourceQuery qValSrc2) {
      super(q, new ValueSourceQuery[]{qValSrc1, qValSrc2});
    }
    @Override
    public String name() {
      return "customMulAdd";
    }
    @Override
    protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) {
      return new CustomScoreProvider(reader) {
        @Override
        public float customScore(int doc, float subQueryScore, float valSrcScores[]) {
          if (valSrcScores.length == 0) {
            return subQueryScore;
          }
          if (valSrcScores.length == 1) {
            return subQueryScore + valSrcScores[0];
          }
          return (subQueryScore + valSrcScores[0]) * valSrcScores[1]; 
        }
        @Override
        public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation valSrcExpls[]) {
          if (valSrcExpls.length == 0) {
            return subQueryExpl;
          }
          Explanation exp = new Explanation(valSrcExpls[0].getValue() + subQueryExpl.getValue(), "sum of:");
          exp.addDetail(subQueryExpl);
          exp.addDetail(valSrcExpls[0]);
          if (valSrcExpls.length == 1) {
            exp.setDescription("CustomMulAdd, sum of:");
            return exp;
          }
          Explanation exp2 = new Explanation(valSrcExpls[1].getValue() * exp.getValue(), "custom score: product of:");
          exp2.addDetail(valSrcExpls[1]);
          exp2.addDetail(exp);
          return exp2;
        }
      };
    }
  }
  private final class CustomExternalQuery extends CustomScoreQuery {
    @Override
    protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) throws IOException {
      final int[] values = FieldCache.DEFAULT.getInts(reader, INT_FIELD);
      return new CustomScoreProvider(reader) {
        @Override
        public float customScore(int doc, float subScore, float valSrcScore) throws IOException {
          assertTrue(doc <= reader.maxDoc());
          return values[doc];
        }
      };
    }
    public CustomExternalQuery(Query q) {
      super(q);
    }
  }
  @Test
  public void testCustomExternalQuery() throws Exception {
    QueryParser qp = new QueryParser(TEST_VERSION_CURRENT, TEXT_FIELD,anlzr); 
    String qtxt = "first aid text"; 
    Query q1 = qp.parse(qtxt); 
    final Query q = new CustomExternalQuery(q1);
    log(q);
    IndexSearcher s = new IndexSearcher(dir);
    TopDocs hits = s.search(q, 1000);
    assertEquals(N_DOCS, hits.totalHits);
    for(int i=0;i<N_DOCS;i++) {
      final int doc = hits.scoreDocs[i].doc;
      final float score = hits.scoreDocs[i].score;
      assertEquals("doc=" + doc, (float) 1+(4*doc) % N_DOCS, score, 0.0001);
    }
    s.close();
  }
  @Test
  public void testRewrite() throws Exception {
    final IndexSearcher s = new IndexSearcher(dir, true);
    Query q = new TermQuery(new Term(TEXT_FIELD, "first"));
    CustomScoreQuery original = new CustomScoreQuery(q);
    CustomScoreQuery rewritten = (CustomScoreQuery) original.rewrite(s.getIndexReader());
    assertTrue("rewritten query should be identical, as TermQuery does not rewrite", original == rewritten);
    assertTrue("no hits for query", s.search(rewritten,1).totalHits > 0);
    assertEquals(s.search(q,1).totalHits, s.search(rewritten,1).totalHits);
    q = new TermRangeQuery(TEXT_FIELD, null, null, true, true); 
    original = new CustomScoreQuery(q);
    rewritten = (CustomScoreQuery) original.rewrite(s.getIndexReader());
    assertTrue("rewritten query should not be identical, as TermRangeQuery rewrites", original != rewritten);
    assertTrue("no hits for query", s.search(rewritten,1).totalHits > 0);
    assertEquals(s.search(q,1).totalHits, s.search(original,1).totalHits);
    assertEquals(s.search(q,1).totalHits, s.search(rewritten,1).totalHits);
    s.close();
  }
  private void doTestCustomScore(String field, FieldScoreQuery.Type tp, double dboost) throws Exception, ParseException {
    float boost = (float) dboost;
    IndexSearcher s = new IndexSearcher(dir, true);
    FieldScoreQuery qValSrc = new FieldScoreQuery(field, tp); 
    QueryParser qp = new QueryParser(TEST_VERSION_CURRENT, TEXT_FIELD, anlzr);
    String qtxt = "first aid text"; 
    Query q1 = qp.parse(qtxt);
    log(q1);
    Query q2CustomNeutral = new CustomScoreQuery(q1);
    q2CustomNeutral.setBoost(boost);
    log(q2CustomNeutral);
    CustomScoreQuery q3CustomMul = new CustomScoreQuery(q1, qValSrc);
    q3CustomMul.setStrict(true);
    q3CustomMul.setBoost(boost);
    log(q3CustomMul);
    CustomScoreQuery q4CustomAdd = new CustomAddQuery(q1, qValSrc);
    q4CustomAdd.setStrict(true);
    q4CustomAdd.setBoost(boost);
    log(q4CustomAdd);
    CustomScoreQuery q5CustomMulAdd = new CustomMulAddQuery(q1, qValSrc, qValSrc);
    q5CustomMulAdd.setStrict(true);
    q5CustomMulAdd.setBoost(boost);
    log(q5CustomMulAdd);
    TopDocs td1 = s.search(q1, null, 1000);
    TopDocs td2CustomNeutral = s.search(q2CustomNeutral, null, 1000);
    TopDocs td3CustomMul = s.search(q3CustomMul, null, 1000);
    TopDocs td4CustomAdd = s.search(q4CustomAdd, null, 1000);
    TopDocs td5CustomMulAdd = s.search(q5CustomMulAdd, null, 1000);
    Map<Integer,Float> h1               = topDocsToMap(td1);
    Map<Integer,Float> h2CustomNeutral  = topDocsToMap(td2CustomNeutral);
    Map<Integer,Float> h3CustomMul      = topDocsToMap(td3CustomMul);
    Map<Integer,Float> h4CustomAdd      = topDocsToMap(td4CustomAdd);
    Map<Integer,Float> h5CustomMulAdd   = topDocsToMap(td5CustomMulAdd);
    verifyResults(boost, s, 
        h1, h2CustomNeutral, h3CustomMul, h4CustomAdd, h5CustomMulAdd,
        q1, q2CustomNeutral, q3CustomMul, q4CustomAdd, q5CustomMulAdd);
  }
  private void verifyResults(float boost, IndexSearcher s, 
      Map<Integer,Float> h1, Map<Integer,Float> h2customNeutral, Map<Integer,Float> h3CustomMul, Map<Integer,Float> h4CustomAdd, Map<Integer,Float> h5CustomMulAdd,
      Query q1, Query q2, Query q3, Query q4, Query q5) throws Exception {
    log("#hits = "+h1.size());
    assertEquals("queries should have same #hits",h1.size(),h2customNeutral.size());
    assertEquals("queries should have same #hits",h1.size(),h3CustomMul.size());
    assertEquals("queries should have same #hits",h1.size(),h4CustomAdd.size());
    assertEquals("queries should have same #hits",h1.size(),h5CustomMulAdd.size());
    QueryUtils.check(q1,s);
    QueryUtils.check(q2,s);
    QueryUtils.check(q3,s);
    QueryUtils.check(q4,s);
    QueryUtils.check(q5,s);
    for (final Integer doc : h1.keySet()) {
      log("doc = "+doc);
      float fieldScore = expectedFieldScore(s.getIndexReader().document(doc).get(ID_FIELD));
      log("fieldScore = " + fieldScore);
      assertTrue("fieldScore should not be 0", fieldScore > 0);
      float score1 = h1.get(doc);
      logResult("score1=", s, q1, doc, score1);
      float score2 = h2customNeutral.get(doc);
      logResult("score2=", s, q2, doc, score2);
      assertEquals("same score (just boosted) for neutral", boost * score1, score2, TEST_SCORE_TOLERANCE_DELTA);
      float score3 = h3CustomMul.get(doc);
      logResult("score3=", s, q3, doc, score3);
      assertEquals("new score for custom mul", boost * fieldScore * score1, score3, TEST_SCORE_TOLERANCE_DELTA);
      float score4 = h4CustomAdd.get(doc);
      logResult("score4=", s, q4, doc, score4);
      assertEquals("new score for custom add", boost * (fieldScore + score1), score4, TEST_SCORE_TOLERANCE_DELTA);
      float score5 = h5CustomMulAdd.get(doc);
      logResult("score5=", s, q5, doc, score5);
      assertEquals("new score for custom mul add", boost * fieldScore * (score1 + fieldScore), score5, TEST_SCORE_TOLERANCE_DELTA);
    }
  }
  private void logResult(String msg, Searcher s, Query q, int doc, float score1) throws IOException {
    log(msg+" "+score1);
    log("Explain by: "+q);
    log(s.explain(q,doc));
  }
  private Map<Integer,Float> topDocsToMap(TopDocs td) {
    Map<Integer,Float> h = new HashMap<Integer,Float>();
    for (int i=0; i<td.totalHits; i++) {
      h.put(td.scoreDocs[i].doc, td.scoreDocs[i].score);
    }
    return h;
  }
}
