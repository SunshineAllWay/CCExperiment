package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
public abstract class TopScoreDocCollector extends TopDocsCollector<ScoreDoc> {
  private static class InOrderTopScoreDocCollector extends TopScoreDocCollector {
    private InOrderTopScoreDocCollector(int numHits) {
      super(numHits);
    }
    @Override
    public void collect(int doc) throws IOException {
      float score = scorer.score();
      assert score != Float.NEGATIVE_INFINITY;
      assert !Float.isNaN(score);
      totalHits++;
      if (score <= pqTop.score) {
        return;
      }
      pqTop.doc = doc + docBase;
      pqTop.score = score;
      pqTop = pq.updateTop();
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return false;
    }
  }
  private static class OutOfOrderTopScoreDocCollector extends TopScoreDocCollector {
    private OutOfOrderTopScoreDocCollector(int numHits) {
      super(numHits);
    }
    @Override
    public void collect(int doc) throws IOException {
      float score = scorer.score();
      assert !Float.isNaN(score);
      totalHits++;
      doc += docBase;
      if (score < pqTop.score || (score == pqTop.score && doc > pqTop.doc)) {
        return;
      }
      pqTop.doc = doc;
      pqTop.score = score;
      pqTop = pq.updateTop();
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }
  }
  public static TopScoreDocCollector create(int numHits, boolean docsScoredInOrder) {
    if (docsScoredInOrder) {
      return new InOrderTopScoreDocCollector(numHits);
    } else {
      return new OutOfOrderTopScoreDocCollector(numHits);
    }
  }
  ScoreDoc pqTop;
  int docBase = 0;
  Scorer scorer;
  private TopScoreDocCollector(int numHits) {
    super(new HitQueue(numHits, true));
    pqTop = pq.top();
  }
  @Override
  protected TopDocs newTopDocs(ScoreDoc[] results, int start) {
    if (results == null) {
      return EMPTY_TOPDOCS;
    }
    float maxScore = Float.NaN;
    if (start == 0) {
      maxScore = results[0].score;
    } else {
      for (int i = pq.size(); i > 1; i--) { pq.pop(); }
      maxScore = pq.pop().score;
    }
    return new TopDocs(totalHits, results, maxScore);
  }
  @Override
  public void setNextReader(IndexReader reader, int base) {
    docBase = base;
  }
  @Override
  public void setScorer(Scorer scorer) throws IOException {
    this.scorer = scorer;
  }
}
