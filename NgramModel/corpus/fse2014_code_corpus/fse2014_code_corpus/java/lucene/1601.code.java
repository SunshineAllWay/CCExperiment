package org.apache.lucene.index;
public interface TermFreqVector {
  public String getField();
  public int size();
  public String[] getTerms();
  public int[] getTermFrequencies();
  public int indexOf(String term);
  public int[] indexesOf(String[] terms, int start, int len);
}
