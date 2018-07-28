package org.apache.lucene.search;
import java.io.IOException;
public abstract class Scorer extends DocIdSetIterator {
  private final Similarity similarity;
  protected Scorer(Similarity similarity) {
    this.similarity = similarity;
  }
  public Similarity getSimilarity() {
    return this.similarity;
  }
  public void score(Collector collector) throws IOException {
    collector.setScorer(this);
    int doc;
    while ((doc = nextDoc()) != NO_MORE_DOCS) {
      collector.collect(doc);
    }
  }
  protected boolean score(Collector collector, int max, int firstDocID) throws IOException {
    collector.setScorer(this);
    int doc = firstDocID;
    while (doc < max) {
      collector.collect(doc);
      doc = nextDoc();
    }
    return doc != NO_MORE_DOCS;
  }
  public abstract float score() throws IOException;
}
