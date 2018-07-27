package org.apache.solr.search;
import org.apache.solr.common.SolrException;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.index.IndexReader;
import java.io.IOException;
public interface DocSet  {
  public void add(int doc);
  public void addUnique(int doc);
  public int size();
  public boolean exists(int docid);
  public DocIterator iterator();
  @Deprecated
  public OpenBitSet getBits();
  public long memSize();
  public DocSet intersection(DocSet other);
  public int intersectionSize(DocSet other);
  public DocSet union(DocSet other);
  public int unionSize(DocSet other);
  public DocSet andNot(DocSet other);
  public int andNotSize(DocSet other);
  public Filter getTopFilter();
}
abstract class DocSetBase implements DocSet {
  public boolean equals(Object obj) {
    if (!(obj instanceof DocSet)) return false;
    DocSet other = (DocSet)obj;
    if (this.size() != other.size()) return false;
    if (this instanceof DocList && other instanceof DocList) {
      DocIterator i1=this.iterator();
      DocIterator i2=other.iterator();
      while(i1.hasNext() && i2.hasNext()) {
        if (i1.nextDoc() != i2.nextDoc()) return false;
      }
      return true;
    }
    return this.getBits().equals(other.getBits());
  }
  public void add(int doc) {
    throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Unsupported Operation");
  }
  public void addUnique(int doc) {
    throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Unsupported Operation");
  }
  public OpenBitSet getBits() {
    OpenBitSet bits = new OpenBitSet();
    for (DocIterator iter = iterator(); iter.hasNext();) {
      bits.set(iter.nextDoc());
    }
    return bits;
  };
  public DocSet intersection(DocSet other) {
    if (!(other instanceof BitDocSet)) {
      return other.intersection(this);
    }
    OpenBitSet newbits = (OpenBitSet)(this.getBits().clone());
    newbits.and(other.getBits());
    return new BitDocSet(newbits);
  }
  public DocSet union(DocSet other) {
    OpenBitSet newbits = (OpenBitSet)(this.getBits().clone());
    newbits.or(other.getBits());
    return new BitDocSet(newbits);
  }
  public int intersectionSize(DocSet other) {
    if (!(other instanceof BitDocSet)) {
      return other.intersectionSize(this);
    }
    return intersection(other).size();
  }
  public int unionSize(DocSet other) {
    return this.size() + other.size() - this.intersectionSize(other);
  }
  public DocSet andNot(DocSet other) {
    OpenBitSet newbits = (OpenBitSet)(this.getBits().clone());
    newbits.andNot(other.getBits());
    return new BitDocSet(newbits);
  }
  public int andNotSize(DocSet other) {
    return this.size() - this.intersectionSize(other);
  }
  public Filter getTopFilter() {
    final OpenBitSet bs = getBits();
    return new Filter() {
      @Override
      public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
        int offset = 0;
        SolrIndexReader r = (SolrIndexReader)reader;
        while (r.getParent() != null) {
          offset += r.getBase();
          r = r.getParent();
        }
        if (r==reader) return bs;
        final int base = offset;
        final int maxDoc = reader.maxDoc();
        final int max = base + maxDoc;   
        return new DocIdSet() {
          public DocIdSetIterator iterator() throws IOException {
            return new DocIdSetIterator() {
              int pos=base-1;
              int adjustedDoc=-1;
              @Override
              public int docID() {
                return adjustedDoc;
              }
              @Override
              public int nextDoc() throws IOException {
                pos = bs.nextSetBit(pos+1);
                return adjustedDoc = (pos>=0 && pos<max) ? pos-base : NO_MORE_DOCS;
              }
              @Override
              public int advance(int target) throws IOException {
                if (target==NO_MORE_DOCS) return adjustedDoc=NO_MORE_DOCS;
                pos = bs.nextSetBit(target+base);
                return adjustedDoc = (pos>=0 && pos<max) ? pos-base : NO_MORE_DOCS;
              }
            };
          }
          @Override
          public boolean isCacheable() {
            return true;
          }
        };
      }
    };
  }
}
