package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import java.text.DecimalFormat;
import java.io.IOException;
public class TestDisjunctionMaxQuery extends LuceneTestCase{
    public static final float SCORE_COMP_THRESH = 0.0000f;
    private static class TestSimilarity extends DefaultSimilarity {
        public TestSimilarity() {
        }
        @Override
        public float tf(float freq) {
            if (freq > 0.0f) return 1.0f;
            else return 0.0f;
        }
        @Override
        public float lengthNorm(String fieldName, int numTerms) {
            return 1.0f;
        }
        @Override
        public float idf(int docFreq, int numDocs) {
            return 1.0f;
        }
    }
    public Similarity sim = new TestSimilarity();
    public Directory index;
    public IndexReader r;
    public IndexSearcher s;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        index = new RAMDirectory();
        IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setSimilarity(sim));
        {
            Document d1 = new Document();
            d1.add(new Field("id", "d1", Field.Store.YES, Field.Index.NOT_ANALYZED));
            d1.add(new Field("hed", "elephant", Field.Store.YES, Field.Index.ANALYZED));
            d1.add(new Field("dek", "elephant", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(d1);
        }
        {
            Document d2 = new Document();
            d2.add(new Field("id", "d2", Field.Store.YES, Field.Index.NOT_ANALYZED));
            d2.add(new Field("hed", "elephant", Field.Store.YES, Field.Index.ANALYZED));
            d2.add(new Field("dek", "albino", Field.Store.YES, Field.Index.ANALYZED));
            d2.add(new Field("dek", "elephant", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(d2);
        }
        {
            Document d3 = new Document();
            d3.add(new Field("id", "d3", Field.Store.YES, Field.Index.NOT_ANALYZED));
            d3.add(new Field("hed", "albino", Field.Store.YES, Field.Index.ANALYZED));
            d3.add(new Field("hed", "elephant", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(d3);
        }
        {
            Document d4 = new Document();
            d4.add(new Field("id", "d4", Field.Store.YES, Field.Index.NOT_ANALYZED));
            d4.add(new Field("hed", "albino", Field.Store.YES, Field.Index.ANALYZED));
            d4.add(new Field("hed", "elephant", Field.Store.YES, Field.Index.ANALYZED));
            d4.add(new Field("dek", "albino", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(d4);
        }
        writer.close();
        r = IndexReader.open(index, true);
        s = new IndexSearcher(r);
        s.setSimilarity(sim);
    }
  public void testSkipToFirsttimeMiss() throws IOException {
    final DisjunctionMaxQuery dq = new DisjunctionMaxQuery(0.0f);
    dq.add(tq("id","d1"));
    dq.add(tq("dek","DOES_NOT_EXIST"));
    QueryUtils.check(dq,s);
    final Weight dw = dq.weight(s);
    final Scorer ds = dw.scorer(r, true, false);
    final boolean skipOk = ds.advance(3) != DocIdSetIterator.NO_MORE_DOCS;
    if (skipOk) {
      fail("firsttime skipTo found a match? ... " + r.document(ds.docID()).get("id"));
    }
  }
  public void testSkipToFirsttimeHit() throws IOException {
    final DisjunctionMaxQuery dq = new DisjunctionMaxQuery(0.0f);
    dq.add(tq("dek","albino"));
    dq.add(tq("dek","DOES_NOT_EXIST"));
    QueryUtils.check(dq,s);
    final Weight dw = dq.weight(s);
    final Scorer ds = dw.scorer(r, true, false);
    assertTrue("firsttime skipTo found no match", ds.advance(3) != DocIdSetIterator.NO_MORE_DOCS);
    assertEquals("found wrong docid", "d4", r.document(ds.docID()).get("id"));
  }
  public void testSimpleEqualScores1() throws Exception {
    DisjunctionMaxQuery q = new DisjunctionMaxQuery(0.0f);
    q.add(tq("hed","albino"));
    q.add(tq("hed","elephant"));
    QueryUtils.check(q,s);
    ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
    try {
      assertEquals("all docs should match " + q.toString(),
          4, h.length);
      float score = h[0].score;
      for (int i = 1; i < h.length; i++) {
        assertEquals("score #" + i + " is not the same",
            score, h[i].score, SCORE_COMP_THRESH);
      }
    } catch (Error e) {
      printHits("testSimpleEqualScores1",h,s);
      throw e;
    }
  }
    public void testSimpleEqualScores2() throws Exception {
        DisjunctionMaxQuery q = new DisjunctionMaxQuery(0.0f);
        q.add(tq("dek","albino"));
        q.add(tq("dek","elephant"));
        QueryUtils.check(q,s);
        ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
        try {
            assertEquals("3 docs should match " + q.toString(),
                         3, h.length);
            float score = h[0].score;
            for (int i = 1; i < h.length; i++) {
                assertEquals("score #" + i + " is not the same",
                             score, h[i].score, SCORE_COMP_THRESH);
            }
        } catch (Error e) {
            printHits("testSimpleEqualScores2",h, s);
            throw e;
        }
    }
    public void testSimpleEqualScores3() throws Exception {
        DisjunctionMaxQuery q = new DisjunctionMaxQuery(0.0f);
        q.add(tq("hed","albino"));
        q.add(tq("hed","elephant"));
        q.add(tq("dek","albino"));
        q.add(tq("dek","elephant"));
        QueryUtils.check(q,s);
        ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
        try {
            assertEquals("all docs should match " + q.toString(),
                         4, h.length);
            float score = h[0].score;
            for (int i = 1; i < h.length; i++) {
                assertEquals("score #" + i + " is not the same",
                             score, h[i].score, SCORE_COMP_THRESH);
            }
        } catch (Error e) {
            printHits("testSimpleEqualScores3",h, s);
            throw e;
        }
    }
    public void testSimpleTiebreaker() throws Exception {
        DisjunctionMaxQuery q = new DisjunctionMaxQuery(0.01f);
        q.add(tq("dek","albino"));
        q.add(tq("dek","elephant"));
        QueryUtils.check(q,s);
        ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
        try {
            assertEquals("3 docs should match " + q.toString(),
                         3, h.length);
            assertEquals("wrong first",  "d2", s.doc(h[0].doc).get("id"));
            float score0 = h[0].score;
            float score1 = h[1].score;
            float score2 = h[2].score;
            assertTrue("d2 does not have better score then others: " +
                       score0 + " >? " + score1,
                       score0 > score1);
            assertEquals("d4 and d1 don't have equal scores",
                         score1, score2, SCORE_COMP_THRESH);
        } catch (Error e) {
            printHits("testSimpleTiebreaker",h, s);
            throw e;
        }
    }
    public void testBooleanRequiredEqualScores() throws Exception {
        BooleanQuery q = new BooleanQuery();
        {
            DisjunctionMaxQuery q1 = new DisjunctionMaxQuery(0.0f);
            q1.add(tq("hed","albino"));
            q1.add(tq("dek","albino"));
            q.add(q1,BooleanClause.Occur.MUST);
            QueryUtils.check(q1,s);
        }
        {
            DisjunctionMaxQuery q2 = new DisjunctionMaxQuery(0.0f);
            q2.add(tq("hed","elephant"));
            q2.add(tq("dek","elephant"));
            q.add(q2, BooleanClause.Occur.MUST);
           QueryUtils.check(q2,s);
        }
        QueryUtils.check(q,s);
        ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
        try {
            assertEquals("3 docs should match " + q.toString(),
                         3, h.length);
            float score = h[0].score;
            for (int i = 1; i < h.length; i++) {
                assertEquals("score #" + i + " is not the same",
                             score, h[i].score, SCORE_COMP_THRESH);
            }
        } catch (Error e) {
            printHits("testBooleanRequiredEqualScores1",h, s);
            throw e;
        }
    }
    public void testBooleanOptionalNoTiebreaker() throws Exception {
        BooleanQuery q = new BooleanQuery();
        {
            DisjunctionMaxQuery q1 = new DisjunctionMaxQuery(0.0f);
            q1.add(tq("hed","albino"));
            q1.add(tq("dek","albino"));
            q.add(q1, BooleanClause.Occur.SHOULD);
        }
        {
            DisjunctionMaxQuery q2 = new DisjunctionMaxQuery(0.0f);
            q2.add(tq("hed","elephant"));
            q2.add(tq("dek","elephant"));
            q.add(q2, BooleanClause.Occur.SHOULD);
        }
        QueryUtils.check(q,s);
        ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
        try {
            assertEquals("4 docs should match " + q.toString(),
                         4, h.length);
            float score = h[0].score;
            for (int i = 1; i < h.length-1; i++) { 
                assertEquals("score #" + i + " is not the same",
                             score, h[i].score, SCORE_COMP_THRESH);
            }
            assertEquals("wrong last", "d1", s.doc(h[h.length-1].doc).get("id"));
            float score1 = h[h.length-1].score;
            assertTrue("d1 does not have worse score then others: " +
                       score + " >? " + score1,
                       score > score1);
        } catch (Error e) {
            printHits("testBooleanOptionalNoTiebreaker",h, s);
            throw e;
        }
    }
    public void testBooleanOptionalWithTiebreaker() throws Exception {
        BooleanQuery q = new BooleanQuery();
        {
            DisjunctionMaxQuery q1 = new DisjunctionMaxQuery(0.01f);
            q1.add(tq("hed","albino"));
            q1.add(tq("dek","albino"));
            q.add(q1, BooleanClause.Occur.SHOULD);
        }
        {
            DisjunctionMaxQuery q2 = new DisjunctionMaxQuery(0.01f);
            q2.add(tq("hed","elephant"));
            q2.add(tq("dek","elephant"));
            q.add(q2, BooleanClause.Occur.SHOULD);
        }
        QueryUtils.check(q,s);
        ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
        try {
            assertEquals("4 docs should match " + q.toString(),
                         4, h.length);
            float score0 = h[0].score;
            float score1 = h[1].score;
            float score2 = h[2].score;
            float score3 = h[3].score;
            String doc0 = s.doc(h[0].doc).get("id");
            String doc1 = s.doc(h[1].doc).get("id");
            String doc2 = s.doc(h[2].doc).get("id");
            String doc3 = s.doc(h[3].doc).get("id");            
            assertTrue("doc0 should be d2 or d4: " + doc0,
                       doc0.equals("d2") || doc0.equals("d4"));
            assertTrue("doc1 should be d2 or d4: " + doc0,
                       doc1.equals("d2") || doc1.equals("d4"));
            assertEquals("score0 and score1 should match",
                         score0, score1, SCORE_COMP_THRESH);
            assertEquals("wrong third", "d3", doc2);
            assertTrue("d3 does not have worse score then d2 and d4: " +
                       score1 + " >? " + score2,
                       score1 > score2);
            assertEquals("wrong fourth", "d1", doc3);
            assertTrue("d1 does not have worse score then d3: " +
                       score2 + " >? " + score3,
                       score2 > score3);
        } catch (Error e) {
            printHits("testBooleanOptionalWithTiebreaker",h, s);
            throw e;
        }
    }
    public void testBooleanOptionalWithTiebreakerAndBoost() throws Exception {
        BooleanQuery q = new BooleanQuery();
        {
            DisjunctionMaxQuery q1 = new DisjunctionMaxQuery(0.01f);
            q1.add(tq("hed","albino", 1.5f));
            q1.add(tq("dek","albino"));
            q.add(q1, BooleanClause.Occur.SHOULD);
        }
        {
            DisjunctionMaxQuery q2 = new DisjunctionMaxQuery(0.01f);
            q2.add(tq("hed","elephant", 1.5f));
            q2.add(tq("dek","elephant"));
            q.add(q2, BooleanClause.Occur.SHOULD);
        }
        QueryUtils.check(q,s);
        ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
        try {
            assertEquals("4 docs should match " + q.toString(),
                         4, h.length);
            float score0 = h[0].score;
            float score1 = h[1].score;
            float score2 = h[2].score;
            float score3 = h[3].score;
            String doc0 = s.doc(h[0].doc).get("id");
            String doc1 = s.doc(h[1].doc).get("id");
            String doc2 = s.doc(h[2].doc).get("id");
            String doc3 = s.doc(h[3].doc).get("id");            
            assertEquals("doc0 should be d4: ", "d4", doc0);
            assertEquals("doc1 should be d3: ", "d3", doc1);
            assertEquals("doc2 should be d2: ", "d2", doc2);
            assertEquals("doc3 should be d1: ", "d1", doc3);
            assertTrue("d4 does not have a better score then d3: " +
                       score0 + " >? " + score1,
                       score0 > score1);
            assertTrue("d3 does not have a better score then d2: " +
                       score1 + " >? " + score2,
                       score1 > score2);
            assertTrue("d3 does not have a better score then d1: " +
                       score2 + " >? " + score3,
                       score2 > score3);
        } catch (Error e) {
            printHits("testBooleanOptionalWithTiebreakerAndBoost",h, s);
            throw e;
        }
    }
    protected Query tq(String f, String t) {
        return new TermQuery(new Term(f, t));
    }
    protected Query tq(String f, String t, float b) {
        Query q = tq(f,t);
        q.setBoost(b);
        return q;
    }
    protected void printHits(String test, ScoreDoc[] h, Searcher searcher) throws Exception {
        System.err.println("------- " + test + " -------");
        DecimalFormat f = new DecimalFormat("0.000000000");
        for (int i = 0; i < h.length; i++) {
            Document d = searcher.doc(h[i].doc);
            float score = h[i].score;
            System.err.println("#" + i + ": " + f.format(score) + " - " +
                               d.get("id"));
        }
    }
}
