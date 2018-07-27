package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.TermPositions;
abstract class PhraseScorer extends Scorer {
  private Weight weight;
  protected byte[] norms;
  protected float value;
  private boolean firstTime = true;
  private boolean more = true;
  protected PhraseQueue pq;
  protected PhrasePositions first, last;
  private float freq; 
  PhraseScorer(Weight weight, TermPositions[] tps, int[] offsets,
      Similarity similarity, byte[] norms) {
    super(similarity);
    this.norms = norms;
    this.weight = weight;
    this.value = weight.getValue();
    for (int i = 0; i < tps.length; i++) {
      PhrasePositions pp = new PhrasePositions(tps[i], offsets[i]);
      if (last != null) {			  
        last.next = pp;
      } else {
        first = pp;
      }
      last = pp;
    }
    pq = new PhraseQueue(tps.length);             
    first.doc = -1;
  }
  @Override
  public int docID() { return first.doc; }
  @Override
  public int nextDoc() throws IOException {
    if (firstTime) {
      init();
      firstTime = false;
    } else if (more) {
      more = last.next();                         
    }
    if (!doNext()) {
      first.doc = NO_MORE_DOCS;
    }
    return first.doc;
  }
  private boolean doNext() throws IOException {
    while (more) {
      while (more && first.doc < last.doc) {      
        more = first.skipTo(last.doc);            
        firstToLast();                            
      }
      if (more) {
        freq = phraseFreq();                      
        if (freq == 0.0f)                         
          more = last.next();                     
        else
          return true;                            
      }
    }
    return false;                                 
  }
  @Override
  public float score() throws IOException {
    float raw = getSimilarity().tf(freq) * value; 
    return norms == null ? raw : raw * getSimilarity().decodeNormValue(norms[first.doc]); 
  }
  @Override
  public int advance(int target) throws IOException {
    firstTime = false;
    for (PhrasePositions pp = first; more && pp != null; pp = pp.next) {
      more = pp.skipTo(target);
    }
    if (more) {
      sort();                                     
    }
    if (!doNext()) {
      first.doc = NO_MORE_DOCS;
    }
    return first.doc;
  }
  public final float currentFreq() { return freq; }
  protected abstract float phraseFreq() throws IOException;
  private void init() throws IOException {
    for (PhrasePositions pp = first; more && pp != null; pp = pp.next) {
      more = pp.next();
    }
    if (more) {
      sort();
    }
  }
  private void sort() {
    pq.clear();
    for (PhrasePositions pp = first; pp != null; pp = pp.next) {
      pq.add(pp);
    }
    pqToList();
  }
  protected final void pqToList() {
    last = first = null;
    while (pq.top() != null) {
      PhrasePositions pp = pq.pop();
      if (last != null) {			  
        last.next = pp;
      } else
        first = pp;
      last = pp;
      pp.next = null;
    }
  }
  protected final void firstToLast() {
    last.next = first;			  
    last = first;
    first = first.next;
    last.next = null;
  }
  @Override
  public String toString() { return "scorer(" + weight + ")"; }
}
