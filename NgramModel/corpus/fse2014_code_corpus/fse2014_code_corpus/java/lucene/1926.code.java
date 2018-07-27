package org.apache.lucene.search;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.DocIdBitSet;
import java.util.BitSet;
public class MockFilter extends Filter {
  private boolean wasCalled;
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) {
    wasCalled = true;
    return new DocIdBitSet(new BitSet());
  }
  public void clear() {
    wasCalled = false;
  }
  public boolean wasCalled() {
    return wasCalled;
  }
}
