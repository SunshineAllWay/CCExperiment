package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.LuceneTestCase;
public class TestScoreCachingWrappingScorer extends LuceneTestCase {
  private static final class SimpleScorer extends Scorer {
    private int idx = 0;
    private int doc = -1;
    public SimpleScorer() {
      super(null);
    }
    @Override public float score() throws IOException {
      return idx == scores.length ? Float.NaN : scores[idx++];
    }
    @Override public int docID() { return doc; }
    @Override public int nextDoc() throws IOException {
      return ++doc < scores.length ? doc : NO_MORE_DOCS;
    }
    @Override public int advance(int target) throws IOException {
      doc = target;
      return doc < scores.length ? doc : NO_MORE_DOCS;
    }
  }
  private static final class ScoreCachingCollector extends Collector {
    private int idx = 0;
    private Scorer scorer;
    float[] mscores;
    public ScoreCachingCollector(int numToCollect) {
      mscores = new float[numToCollect];
    }
    @Override public void collect(int doc) throws IOException {
      if (idx == mscores.length) {
        return; 
      }
      mscores[idx] = scorer.score();
      mscores[idx] = scorer.score();
      mscores[idx] = scorer.score();
      ++idx;
    }
    @Override public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
    }
    @Override public void setScorer(Scorer scorer) throws IOException {
      this.scorer = new ScoreCachingWrappingScorer(scorer);
    }
    @Override public boolean acceptsDocsOutOfOrder() {
      return true;
    }
  }
  private static final float[] scores = new float[] { 0.7767749f, 1.7839992f,
      8.9925785f, 7.9608946f, 0.07948637f, 2.6356435f, 7.4950366f, 7.1490803f,
      8.108544f, 4.961808f, 2.2423935f, 7.285586f, 4.6699767f };
  public void testGetScores() throws Exception {
    Scorer s = new SimpleScorer();
    ScoreCachingCollector scc = new ScoreCachingCollector(scores.length);
    scc.setScorer(s);
    int doc;
    while ((doc = s.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
      scc.collect(doc);
    }
    for (int i = 0; i < scores.length; i++) {
      assertEquals(scores[i], scc.mscores[i], 0f);
    }
  }
}
