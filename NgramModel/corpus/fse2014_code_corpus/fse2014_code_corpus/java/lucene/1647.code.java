package org.apache.lucene.search;
import java.io.IOException;
class DisjunctionMaxScorer extends Scorer {
  private final Scorer[] subScorers;
  private int numScorers;
  private final float tieBreakerMultiplier;
  private int doc = -1;
  public DisjunctionMaxScorer(float tieBreakerMultiplier,
      Similarity similarity, Scorer[] subScorers, int numScorers) throws IOException {
    super(similarity);
    this.tieBreakerMultiplier = tieBreakerMultiplier;
    this.subScorers = subScorers;
    this.numScorers = numScorers;
    heapify();
  }
  @Override
  public int nextDoc() throws IOException {
    if (numScorers == 0) return doc = NO_MORE_DOCS;
    while (subScorers[0].docID() == doc) {
      if (subScorers[0].nextDoc() != NO_MORE_DOCS) {
        heapAdjust(0);
      } else {
        heapRemoveRoot();
        if (numScorers == 0) {
          return doc = NO_MORE_DOCS;
        }
      }
    }
    return doc = subScorers[0].docID();
  }
  @Override
  public int docID() {
    return doc;
  }
  @Override
  public float score() throws IOException {
    int doc = subScorers[0].docID();
    float[] sum = { subScorers[0].score() }, max = { sum[0] };
    int size = numScorers;
    scoreAll(1, size, doc, sum, max);
    scoreAll(2, size, doc, sum, max);
    return max[0] + (sum[0] - max[0]) * tieBreakerMultiplier;
  }
  private void scoreAll(int root, int size, int doc, float[] sum, float[] max) throws IOException {
    if (root < size && subScorers[root].docID() == doc) {
      float sub = subScorers[root].score();
      sum[0] += sub;
      max[0] = Math.max(max[0], sub);
      scoreAll((root<<1)+1, size, doc, sum, max);
      scoreAll((root<<1)+2, size, doc, sum, max);
    }
  }
  @Override
  public int advance(int target) throws IOException {
    if (numScorers == 0) return doc = NO_MORE_DOCS;
    while (subScorers[0].docID() < target) {
      if (subScorers[0].advance(target) != NO_MORE_DOCS) {
        heapAdjust(0);
      } else {
        heapRemoveRoot();
        if (numScorers == 0) {
          return doc = NO_MORE_DOCS;
        }
      }
    }
    return doc = subScorers[0].docID();
  }
  private void heapify() {
    for (int i = (numScorers >> 1) - 1; i >= 0; i--) {
      heapAdjust(i);
    }
  }
  private void heapAdjust(int root) {
    Scorer scorer = subScorers[root];
    int doc = scorer.docID();
    int i = root;
    while (i <= (numScorers >> 1) - 1) {
      int lchild = (i << 1) + 1;
      Scorer lscorer = subScorers[lchild];
      int ldoc = lscorer.docID();
      int rdoc = Integer.MAX_VALUE, rchild = (i << 1) + 2;
      Scorer rscorer = null;
      if (rchild < numScorers) {
        rscorer = subScorers[rchild];
        rdoc = rscorer.docID();
      }
      if (ldoc < doc) {
        if (rdoc < ldoc) {
          subScorers[i] = rscorer;
          subScorers[rchild] = scorer;
          i = rchild;
        } else {
          subScorers[i] = lscorer;
          subScorers[lchild] = scorer;
          i = lchild;
        }
      } else if (rdoc < doc) {
        subScorers[i] = rscorer;
        subScorers[rchild] = scorer;
        i = rchild;
      } else {
        return;
      }
    }
  }
  private void heapRemoveRoot() {
    if (numScorers == 1) {
      subScorers[0] = null;
      numScorers = 0;
    } else {
      subScorers[0] = subScorers[numScorers - 1];
      subScorers[numScorers - 1] = null;
      --numScorers;
      heapAdjust(0);
    }
  }
}
