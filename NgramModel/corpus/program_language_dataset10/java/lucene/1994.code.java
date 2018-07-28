package org.apache.lucene.search.function;
import java.util.HashMap;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryUtils;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;
import static org.junit.Assert.*;
public class TestFieldScoreQuery extends FunctionTestSetup {
  public TestFieldScoreQuery() {
    super(true);
  }
  @Test
  public void testRankByte () throws Exception {
    doTestRank(INT_FIELD,FieldScoreQuery.Type.BYTE);
  }
  @Test
  public void testRankShort () throws Exception {
    doTestRank(INT_FIELD,FieldScoreQuery.Type.SHORT);
  }
  @Test
  public void testRankInt () throws Exception {
    doTestRank(INT_FIELD,FieldScoreQuery.Type.INT);
  }
  @Test
  public void testRankFloat () throws Exception {
    doTestRank(INT_FIELD,FieldScoreQuery.Type.FLOAT);
    doTestRank(FLOAT_FIELD,FieldScoreQuery.Type.FLOAT);
  }
  private void doTestRank (String field, FieldScoreQuery.Type tp) throws Exception {
    IndexSearcher s = new IndexSearcher(dir, true);
    Query q = new FieldScoreQuery(field,tp);
    log("test: "+q);
    QueryUtils.check(q,s);
    ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
    assertEquals("All docs should be matched!",N_DOCS,h.length);
    String prevID = "ID"+(N_DOCS+1); 
    for (int i=0; i<h.length; i++) {
      String resID = s.doc(h[i].doc).get(ID_FIELD);
      log(i+".   score="+h[i].score+"  -  "+resID);
      log(s.explain(q,h[i].doc));
      assertTrue("res id "+resID+" should be < prev res id "+prevID, resID.compareTo(prevID)<0);
      prevID = resID;
    }
  }
  @Test
  public void testExactScoreByte () throws Exception {
    doTestExactScore(INT_FIELD,FieldScoreQuery.Type.BYTE);
  }
  @Test
  public void testExactScoreShort () throws  Exception {
    doTestExactScore(INT_FIELD,FieldScoreQuery.Type.SHORT);
  }
  @Test
  public void testExactScoreInt () throws  Exception {
    doTestExactScore(INT_FIELD,FieldScoreQuery.Type.INT);
  }
  @Test
  public void testExactScoreFloat () throws  Exception {
    doTestExactScore(INT_FIELD,FieldScoreQuery.Type.FLOAT);
    doTestExactScore(FLOAT_FIELD,FieldScoreQuery.Type.FLOAT);
  }
  private void doTestExactScore (String field, FieldScoreQuery.Type tp) throws Exception {
    IndexSearcher s = new IndexSearcher(dir, true);
    Query q = new FieldScoreQuery(field,tp);
    TopDocs td = s.search(q,null,1000);
    assertEquals("All docs should be matched!",N_DOCS,td.totalHits);
    ScoreDoc sd[] = td.scoreDocs;
    for (ScoreDoc aSd : sd) {
      float score = aSd.score;
      log(s.explain(q, aSd.doc));
      String id = s.getIndexReader().document(aSd.doc).get(ID_FIELD);
      float expectedScore = expectedFieldScore(id); 
      assertEquals("score of " + id + " shuould be " + expectedScore + " != " + score, expectedScore, score, TEST_SCORE_TOLERANCE_DELTA);
    }
  }
  @Test
  public void testCachingByte () throws  Exception {
    doTestCaching(INT_FIELD,FieldScoreQuery.Type.BYTE);
  }
  @Test
  public void testCachingShort () throws  Exception {
    doTestCaching(INT_FIELD,FieldScoreQuery.Type.SHORT);
  }
  @Test
  public void testCachingInt () throws Exception {
    doTestCaching(INT_FIELD,FieldScoreQuery.Type.INT);
  }
  @Test
  public void testCachingFloat () throws  Exception {
    doTestCaching(INT_FIELD,FieldScoreQuery.Type.FLOAT);
    doTestCaching(FLOAT_FIELD,FieldScoreQuery.Type.FLOAT);
  }
  private void doTestCaching (String field, FieldScoreQuery.Type tp) throws Exception {
    HashMap<FieldScoreQuery.Type,Object> expectedArrayTypes = new HashMap<FieldScoreQuery.Type,Object>();
    expectedArrayTypes.put(FieldScoreQuery.Type.BYTE, new byte[0]);
    expectedArrayTypes.put(FieldScoreQuery.Type.SHORT, new short[0]);
    expectedArrayTypes.put(FieldScoreQuery.Type.INT, new int[0]);
    expectedArrayTypes.put(FieldScoreQuery.Type.FLOAT, new float[0]);
    IndexSearcher s = new IndexSearcher(dir, true);
    Object[] innerArray = new Object[s.getIndexReader().getSequentialSubReaders().length];
    boolean warned = false; 
    for (int i=0; i<10; i++) {
      FieldScoreQuery q = new FieldScoreQuery(field,tp);
      ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
      assertEquals("All docs should be matched!",N_DOCS,h.length);
      IndexReader[] readers = s.getIndexReader().getSequentialSubReaders();
      for (int j = 0; j < readers.length; j++) {
        IndexReader reader = readers[j];
        try {
          if (i == 0) {
            innerArray[j] = q.valSrc.getValues(reader).getInnerArray();
            log(i + ".  compare: " + innerArray[j].getClass() + " to "
                + expectedArrayTypes.get(tp).getClass());
            assertEquals(
                "field values should be cached in the correct array type!",
                innerArray[j].getClass(), expectedArrayTypes.get(tp).getClass());
          } else {
            log(i + ".  compare: " + innerArray[j] + " to "
                + q.valSrc.getValues(reader).getInnerArray());
            assertSame("field values should be cached and reused!", innerArray[j],
                q.valSrc.getValues(reader).getInnerArray());
          }
        } catch (UnsupportedOperationException e) {
          if (!warned) {
            System.err.println("WARNING: " + testName()
                + " cannot fully test values of " + q);
            warned = true;
          }
        }
      }
    }
    s = new IndexSearcher(dir, true);
    FieldScoreQuery q = new FieldScoreQuery(field,tp);
    ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
    assertEquals("All docs should be matched!",N_DOCS,h.length);
    IndexReader[] readers = s.getIndexReader().getSequentialSubReaders();
    for (int j = 0; j < readers.length; j++) {
      IndexReader reader = readers[j];
      try {
        log("compare: " + innerArray + " to "
            + q.valSrc.getValues(reader).getInnerArray());
        assertNotSame(
            "cached field values should not be reused if reader as changed!",
            innerArray, q.valSrc.getValues(reader).getInnerArray());
      } catch (UnsupportedOperationException e) {
        if (!warned) {
          System.err.println("WARNING: " + testName()
              + " cannot fully test values of " + q);
          warned = true;
        }
      }
    }
  }
  private String testName() {
    return getClass().getName()+"."+ getName();
  }
}
