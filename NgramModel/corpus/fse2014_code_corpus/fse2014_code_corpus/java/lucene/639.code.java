package org.apache.lucene.analysis.compound.hyphenation;
public class Hyphenation {
  private int[] hyphenPoints;
  Hyphenation(int[] points) {
    hyphenPoints = points;
  }
  public int length() {
    return hyphenPoints.length;
  }
  public int[] getHyphenationPoints() {
    return hyphenPoints;
  }
}
