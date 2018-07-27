package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
public class TestPositiveScoresOnlyCollector extends LuceneTestCase {
  private static final class SimpleScorer extends Scorer {
    private int idx = -1;
    public SimpleScorer() {
      super(null);
    }
    @Override public float score() throws IOException {
      return idx == scores.length ? Float.NaN : scores[idx];
    }
    @Override public int docID() { return idx; }
    @Override public int nextDoc() throws IOException {
      return ++idx != scores.length ? idx : NO_MORE_DOCS;
    }
    @Override public int advance(int target) throws IOException {
      idx = target;
      return idx < scores.length ? idx : NO_MORE_DOCS;
    }
  }
  private static final float[] scores = new float[] { 0.7767749f, -1.7839992f,
      8.9925785f, 7.9608946f, -0.07948637f, 2.6356435f, 7.4950366f, 7.1490803f,
      -8.108544f, 4.961808f, 2.2423935f, -7.285586f, 4.6699767f };
  public void testNegativeScores() throws Exception {
    int numPositiveScores = 0;
    for (int i = 0; i < scores.length; i++) {
      if (scores[i] > 0) {
        ++numPositiveScores;
      }
    }
    Scorer s = new SimpleScorer();
    TopDocsCollector<ScoreDoc> tdc = TopScoreDocCollector.create(scores.length, true);
    Collector c = new PositiveScoresOnlyCollector(tdc);
    c.setScorer(s);
    while (s.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
      c.collect(0);
    }
    TopDocs td = tdc.topDocs();
    ScoreDoc[] sd = td.scoreDocs;
    assertEquals(numPositiveScores, td.totalHits);
    for (int i = 0; i < sd.length; i++) {
      assertTrue("only positive scores should return: " + sd[i].score, sd[i].score > 0);
    }
  }
}
