package org.apache.solr.search;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetIterator;
import org.apache.lucene.search.DocIdSetIterator;
public class BitDocSet extends DocSetBase {
  final OpenBitSet bits;
  int size;    
  public BitDocSet() {
    bits = new OpenBitSet();
  }
  public BitDocSet(OpenBitSet bits) {
    this.bits = bits;
    size=-1;
  }
  public BitDocSet(OpenBitSet bits, int size) {
    this.bits = bits;
    this.size = size;
  }
  public DocIterator iterator() {
    return new DocIterator() {
      private final OpenBitSetIterator iter = new OpenBitSetIterator(bits);
      private int pos = iter.nextDoc();
      public boolean hasNext() {
        return pos != DocIdSetIterator.NO_MORE_DOCS;
      }
      public Integer next() {
        return nextDoc();
      }
      public void remove() {
        bits.clear(pos);
      }
      public int nextDoc() {
        int old=pos;
        pos=iter.nextDoc();
        return old;
      }
      public float score() {
        return 0.0f;
      }
    };
  }
  public OpenBitSet getBits() {
    return bits;
  }
  public void add(int doc) {
    bits.set(doc);
    size=-1;  
  }
  public void addUnique(int doc) {
    bits.set(doc);
    size=-1;  
  }
  public int size() {
    if (size!=-1) return size;
    return size=(int)bits.cardinality();
  }
  public void invalidateSize() {
    size=-1;
  }
  public boolean exists(int doc) {
    return bits.get(doc);
  }
  @Override
  public int intersectionSize(DocSet other) {
    if (other instanceof BitDocSet) {
      return (int)OpenBitSet.intersectionCount(this.bits, ((BitDocSet)other).bits);
    } else {
      return other.intersectionSize(this);
    }
  }
  @Override
  public int unionSize(DocSet other) {
    if (other instanceof BitDocSet) {
      return (int)OpenBitSet.unionCount(this.bits, ((BitDocSet)other).bits);
    } else {
      return other.unionSize(this);
    }
  }
  @Override
  public int andNotSize(DocSet other) {
    if (other instanceof BitDocSet) {
      return (int)OpenBitSet.andNotCount(this.bits, ((BitDocSet)other).bits);
    } else {
      return super.andNotSize(other);
    }
  }
  @Override
   public DocSet andNot(DocSet other) {
    OpenBitSet newbits = (OpenBitSet)(bits.clone());
     if (other instanceof BitDocSet) {
       newbits.andNot(((BitDocSet)other).bits);
     } else {
       DocIterator iter = other.iterator();
       while (iter.hasNext()) newbits.clear(iter.nextDoc());
     }
     return new BitDocSet(newbits);
  }
  @Override
   public DocSet union(DocSet other) {
     OpenBitSet newbits = (OpenBitSet)(bits.clone());
     if (other instanceof BitDocSet) {
       newbits.union(((BitDocSet)other).bits);
     } else {
       DocIterator iter = other.iterator();
       while (iter.hasNext()) newbits.set(iter.nextDoc());
     }
     return new BitDocSet(newbits);
  }
  public long memSize() {
    return (bits.getBits().length << 3) + 16;
  }
}
