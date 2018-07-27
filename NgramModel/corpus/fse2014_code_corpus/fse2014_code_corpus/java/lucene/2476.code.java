package org.apache.solr.search;
import java.util.Iterator;
public interface DocIterator extends Iterator<Integer> {
  public int nextDoc();
  public float score();
}
