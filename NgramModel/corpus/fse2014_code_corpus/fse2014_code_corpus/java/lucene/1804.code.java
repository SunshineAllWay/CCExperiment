package org.apache.lucene.util;
import java.io.IOException;
import org.apache.lucene.search.DocIdSetIterator;
public class OpenBitSetDISI extends OpenBitSet {
  public OpenBitSetDISI(DocIdSetIterator disi, int maxSize) throws IOException {
    super(maxSize);
    inPlaceOr(disi);
  }
  public OpenBitSetDISI(int maxSize) {
    super(maxSize);
  }
  public void inPlaceOr(DocIdSetIterator disi) throws IOException {
    int doc;
    long size = size();
    while ((doc = disi.nextDoc()) < size) {
      fastSet(doc);
    }
  }
  public void inPlaceAnd(DocIdSetIterator disi) throws IOException {
    int bitSetDoc = nextSetBit(0);
    int disiDoc;
    while (bitSetDoc != -1 && (disiDoc = disi.advance(bitSetDoc)) != DocIdSetIterator.NO_MORE_DOCS) {
      clear(bitSetDoc, disiDoc);
      bitSetDoc = nextSetBit(disiDoc + 1);
    }
    if (bitSetDoc != -1) {
      clear(bitSetDoc, size());
    }
  }
  public void inPlaceNot(DocIdSetIterator disi) throws IOException {
    int doc;
    long size = size();
    while ((doc = disi.nextDoc()) < size) {
      fastClear(doc);
    }
  }
  public void inPlaceXor(DocIdSetIterator disi) throws IOException {
    int doc;
    long size = size();
    while ((doc = disi.nextDoc()) < size) {
      fastFlip(doc);
    }
  }
}
