package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.TermDocs;
final class TermScorer extends Scorer {
  private Weight weight;
  private TermDocs termDocs;
  private byte[] norms;
  private float weightValue;
  private int doc = -1;
  private final int[] docs = new int[32];         
  private final int[] freqs = new int[32];        
  private int pointer;
  private int pointerMax;
  private static final int SCORE_CACHE_SIZE = 32;
  private float[] scoreCache = new float[SCORE_CACHE_SIZE];
  TermScorer(Weight weight, TermDocs td, Similarity similarity, byte[] norms) {
    super(similarity);
    this.weight = weight;
    this.termDocs = td;
    this.norms = norms;
    this.weightValue = weight.getValue();
    for (int i = 0; i < SCORE_CACHE_SIZE; i++)
      scoreCache[i] = getSimilarity().tf(i) * weightValue;
  }
  @Override
  public void score(Collector c) throws IOException {
    score(c, Integer.MAX_VALUE, nextDoc());
  }
  @Override
  protected boolean score(Collector c, int end, int firstDocID) throws IOException {
    c.setScorer(this);
    while (doc < end) {                           
      c.collect(doc);                      
      if (++pointer >= pointerMax) {
        pointerMax = termDocs.read(docs, freqs);  
        if (pointerMax != 0) {
          pointer = 0;
        } else {
          termDocs.close();                       
          doc = Integer.MAX_VALUE;                
          return false;
        }
      } 
      doc = docs[pointer];
    }
    return true;
  }
  @Override
  public int docID() { return doc; }
  @Override
  public int nextDoc() throws IOException {
    pointer++;
    if (pointer >= pointerMax) {
      pointerMax = termDocs.read(docs, freqs);    
      if (pointerMax != 0) {
        pointer = 0;
      } else {
        termDocs.close();                         
        return doc = NO_MORE_DOCS;
      }
    } 
    doc = docs[pointer];
    return doc;
  }
  @Override
  public float score() {
    assert doc != -1;
    int f = freqs[pointer];
    float raw =                                   
      f < SCORE_CACHE_SIZE                        
      ? scoreCache[f]                             
      : getSimilarity().tf(f)*weightValue;        
    return norms == null ? raw : raw * getSimilarity().decodeNormValue(norms[doc]); 
  }
  @Override
  public int advance(int target) throws IOException {
    for (pointer++; pointer < pointerMax; pointer++) {
      if (docs[pointer] >= target) {
        return doc = docs[pointer];
      }
    }
    boolean result = termDocs.skipTo(target);
    if (result) {
      pointerMax = 1;
      pointer = 0;
      docs[pointer] = doc = termDocs.doc();
      freqs[pointer] = termDocs.freq();
    } else {
      doc = NO_MORE_DOCS;
    }
    return doc;
  }
  @Override
  public String toString() { return "scorer(" + weight + ")"; }
}
