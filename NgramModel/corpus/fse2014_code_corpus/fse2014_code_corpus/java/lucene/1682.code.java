package org.apache.lucene.search;
import org.apache.lucene.util.PriorityQueue;
final class PhraseQueue extends PriorityQueue<PhrasePositions> {
  PhraseQueue(int size) {
    initialize(size);
  }
  @Override
  protected final boolean lessThan(PhrasePositions pp1, PhrasePositions pp2) {
    if (pp1.doc == pp2.doc) 
      if (pp1.position == pp2.position)
        return pp1.offset < pp2.offset;
      else
        return pp1.position < pp2.position;
    else
      return pp1.doc < pp2.doc;
  }
}
