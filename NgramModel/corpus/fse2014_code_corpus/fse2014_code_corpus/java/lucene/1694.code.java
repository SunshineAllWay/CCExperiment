package org.apache.lucene.search;
public class ScoreDoc implements java.io.Serializable {
  public float score;
  public int doc;
  public ScoreDoc(int doc, float score) {
    this.doc = doc;
    this.score = score;
  }
  @Override
  public String toString() {
    return "doc=" + doc + " score=" + score;
  }
}
