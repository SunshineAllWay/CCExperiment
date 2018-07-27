package org.apache.lucene.search;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.DocIdBitSet;
import java.util.BitSet;
import java.io.IOException;
public class SingleDocTestFilter extends Filter {
  private int doc;
  public SingleDocTestFilter(int doc) {
    this.doc = doc;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    BitSet bits = new BitSet(reader.maxDoc());
    bits.set(doc);
    return new DocIdBitSet(bits);
  }
}
