package org.apache.solr.search;
public interface DocList extends DocSet {
  public int offset();
  public int size();
  public int matches();
  public DocList subset(int offset, int len);
  public DocIterator iterator();
  public boolean hasScores();
  public float maxScore();
}
