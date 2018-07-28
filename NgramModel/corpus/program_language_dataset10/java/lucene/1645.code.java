package org.apache.lucene.search;
import org.apache.lucene.index.FieldInvertState;
public class DefaultSimilarity extends Similarity {
  @Override
  public float computeNorm(String field, FieldInvertState state) {
    final int numTerms;
    if (discountOverlaps)
      numTerms = state.getLength() - state.getNumOverlap();
    else
      numTerms = state.getLength();
    return (state.getBoost() * lengthNorm(field, numTerms));
  }
  @Override
  public float lengthNorm(String fieldName, int numTerms) {
    return (float)(1.0 / Math.sqrt(numTerms));
  }
  @Override
  public float queryNorm(float sumOfSquaredWeights) {
    return (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
  }
  @Override
  public float tf(float freq) {
    return (float)Math.sqrt(freq);
  }
  @Override
  public float sloppyFreq(int distance) {
    return 1.0f / (distance + 1);
  }
  @Override
  public float idf(int docFreq, int numDocs) {
    return (float)(Math.log(numDocs/(double)(docFreq+1)) + 1.0);
  }
  @Override
  public float coord(int overlap, int maxOverlap) {
    return overlap / (float)maxOverlap;
  }
  protected boolean discountOverlaps = true;
  public void setDiscountOverlaps(boolean v) {
    discountOverlaps = v;
  }
  public boolean getDiscountOverlaps() {
    return discountOverlaps;
  }
}
