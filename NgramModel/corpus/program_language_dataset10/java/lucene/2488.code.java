package org.apache.solr.search;
import org.apache.lucene.util.BitUtil;
public final class HashDocSet extends DocSetBase {
  static float DEFAULT_INVERSE_LOAD_FACTOR = 1.0f /0.75f;
  private final static int EMPTY=-1;
  private final int[] table;
  private final int size;
  private final int mask;
  public HashDocSet(int[] docs, int offset, int len) {
    this(docs, offset, len, DEFAULT_INVERSE_LOAD_FACTOR);
  }
  public HashDocSet(int[] docs, int offset, int len, float inverseLoadFactor) {
    int tsize = Math.max(BitUtil.nextHighestPowerOfTwo(len), 1);
    if (tsize < len * inverseLoadFactor) {
      tsize <<= 1;
    }
    mask=tsize-1;
    table = new int[tsize];
    for (int i=tsize-1; i>=0; i--) table[i]=EMPTY;
    int end = offset + len;
    for (int i=offset; i<end; i++) {
      put(docs[i]);
    }
    size = len;
  }
  void put(int doc) {
    int s = doc & mask;
    while (table[s]!=EMPTY) {
      s = (s + ((doc>>7)|1)) & mask;
    }
    table[s]=doc;
  }
  public boolean exists(int doc) {
    int s = doc & mask;
    for(;;) {
      int v = table[s];
      if (v==EMPTY) return false;
      if (v==doc) return true;
      s = (s + ((doc>>7)|1)) & mask;
    }
  }
  public int size() {
    return size;
  }
  public DocIterator iterator() {
    return new DocIterator() {
      int pos=0;
      int doc;
      { goNext(); }
      public boolean hasNext() {
        return pos < table.length;
      }
      public Integer next() {
        return nextDoc();
      }
      public void remove() {
      }
      void goNext() {
        while (pos<table.length && table[pos]==EMPTY) pos++;
      }
      public int nextDoc() {
        int doc = table[pos];
        pos++;
        goNext();
        return doc;
      }
      public float score() {
        return 0.0f;
      }
    };
  }
  public long memSize() {
    return (table.length<<2) + 20;
  }
  @Override
  public DocSet intersection(DocSet other) {
   if (other instanceof HashDocSet) {
     final HashDocSet a = size()<=other.size() ? this : (HashDocSet)other;
     final HashDocSet b = size()<=other.size() ? (HashDocSet)other : this;
     int[] result = new int[a.size()];
     int resultCount=0;
     for (int i=0; i<a.table.length; i++) {
       int id=a.table[i];
       if (id >= 0 && b.exists(id)) {
         result[resultCount++]=id;
       }
     }
     return new HashDocSet(result,0,resultCount);
   } else {
     int[] result = new int[size()];
     int resultCount=0;
     for (int i=0; i<table.length; i++) {
       int id=table[i];
       if (id >= 0 && other.exists(id)) {
         result[resultCount++]=id;
       }
     }
     return new HashDocSet(result,0,resultCount);
   }
  }
  @Override
  public int intersectionSize(DocSet other) {
   if (other instanceof HashDocSet) {
     final HashDocSet a = size()<=other.size() ? this : (HashDocSet)other;
     final HashDocSet b = size()<=other.size() ? (HashDocSet)other : this;
     int resultCount=0;
     for (int i=0; i<a.table.length; i++) {
       int id=a.table[i];
       if (id >= 0 && b.exists(id)) {
         resultCount++;
       }
     }
     return resultCount;
   } else {
     int resultCount=0;
     for (int i=0; i<table.length; i++) {
       int id=table[i];
       if (id >= 0 && other.exists(id)) {
         resultCount++;
       }
     }
     return resultCount;
   }
  }
  @Override
  public DocSet andNot(DocSet other) {
    int[] result = new int[size()];
    int resultCount=0;
    for (int i=0; i<table.length; i++) {
      int id=table[i];
      if (id >= 0 && !other.exists(id)) {
        result[resultCount++]=id;
      }
    }
    return new HashDocSet(result,0,resultCount);
  }
  @Override
  public DocSet union(DocSet other) {
   if (other instanceof HashDocSet) {
     final HashDocSet a = size()<=other.size() ? this : (HashDocSet)other;
     final HashDocSet b = size()<=other.size() ? (HashDocSet)other : this;
     int[] result = new int[a.size()+b.size()];
     int resultCount=0;
     for (int i=0; i<b.table.length; i++) {
       int id=b.table[i];
       if (id>=0) result[resultCount++]=id;
     }
     for (int i=0; i<a.table.length; i++) {
       int id=a.table[i];
       if (id>=0 && !b.exists(id)) result[resultCount++]=id;
     }
     return new HashDocSet(result,0,resultCount);
   } else {
     return other.union(this);
   }
  }
}
