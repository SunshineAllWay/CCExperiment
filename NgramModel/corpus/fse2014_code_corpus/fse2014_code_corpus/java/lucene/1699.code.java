package org.apache.lucene.search;
import org.apache.lucene.index.FieldInvertState;
public class SimilarityDelegator extends Similarity {
  private Similarity delegee;
  public SimilarityDelegator(Similarity delegee) {
    this.delegee = delegee;
  }
  @Override
  public float computeNorm(String fieldName, FieldInvertState state) {
    return delegee.computeNorm(fieldName, state);
  }
  @Override
  public float lengthNorm(String fieldName, int numTerms) {
    return delegee.lengthNorm(fieldName, numTerms);
  }
  @Override
  public float queryNorm(float sumOfSquaredWeights) {
    return delegee.queryNorm(sumOfSquaredWeights);
  }
  @Override
  public float tf(float freq) {
    return delegee.tf(freq);
  }
  @Override
  public float sloppyFreq(int distance) {
    return delegee.sloppyFreq(distance);
  }
  @Override
  public float idf(int docFreq, int numDocs) {
    return delegee.idf(docFreq, numDocs);
  }
  @Override
  public float coord(int overlap, int maxOverlap) {
    return delegee.coord(overlap, maxOverlap);
  }
  @Override
  public float scorePayload(int docId, String fieldName, int start, int end, byte [] payload, int offset, int length) {
    return delegee.scorePayload(docId, fieldName, start, end, payload, offset, length);
  }
}
