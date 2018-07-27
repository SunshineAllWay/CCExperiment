package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.*;
final class ExactPhraseScorer extends PhraseScorer {
  ExactPhraseScorer(Weight weight, TermPositions[] tps, int[] offsets,
      Similarity similarity, byte[] norms) {
    super(weight, tps, offsets, similarity, norms);
  }
  @Override
  protected final float phraseFreq() throws IOException {
    pq.clear();
    for (PhrasePositions pp = first; pp != null; pp = pp.next) {
      pp.firstPosition();
      pq.add(pp);				  
    }
    pqToList();					  
    int freq = 0;
    do {					  
      while (first.position < last.position) {	  
	    do {
	      if (!first.nextPosition())
	        return freq;
	    } while (first.position < last.position);
	      firstToLast();
      }
      freq++;					  
    } while (last.nextPosition());
    return freq;
  }
}
