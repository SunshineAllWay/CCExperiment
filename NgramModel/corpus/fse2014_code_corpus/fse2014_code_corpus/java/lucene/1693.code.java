package org.apache.lucene.search;
import java.io.IOException;
public class ScoreCachingWrappingScorer extends Scorer {
  private Scorer scorer;
  private int curDoc = -1;
  private float curScore;
  public ScoreCachingWrappingScorer(Scorer scorer) {
    super(scorer.getSimilarity());
    this.scorer = scorer;
  }
  @Override
  protected boolean score(Collector collector, int max, int firstDocID) throws IOException {
    return scorer.score(collector, max, firstDocID);
  }
  @Override
  public Similarity getSimilarity() {
    return scorer.getSimilarity();
  }
  @Override
  public float score() throws IOException {
    int doc = scorer.docID();
    if (doc != curDoc) {
      curScore = scorer.score();
      curDoc = doc;
    }
    return curScore;
  }
  @Override
  public int docID() {
    return scorer.docID();
  }
  @Override
  public int nextDoc() throws IOException {
    return scorer.nextDoc();
  }
  @Override
  public void score(Collector collector) throws IOException {
    scorer.score(collector);
  }
  @Override
  public int advance(int target) throws IOException {
    return scorer.advance(target);
  }
}
