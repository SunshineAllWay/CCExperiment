package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.DocIdBitSet;
public abstract class Filter implements java.io.Serializable {
  public abstract DocIdSet getDocIdSet(IndexReader reader) throws IOException;
}
