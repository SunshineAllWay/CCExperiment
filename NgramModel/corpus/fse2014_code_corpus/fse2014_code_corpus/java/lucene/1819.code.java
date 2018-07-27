package org.apache.lucene.util;
public enum Version {
  LUCENE_20,
  LUCENE_21,
  LUCENE_22,
  LUCENE_23,
  LUCENE_24,
  LUCENE_29,
  LUCENE_30,
  LUCENE_31,
  @Deprecated
  LUCENE_CURRENT;
  public boolean onOrAfter(Version other) {
    return compareTo(other) >= 0;
  }
}