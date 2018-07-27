package org.apache.lucene.search.spell;
import org.apache.lucene.util.PriorityQueue;
final class SuggestWordQueue extends PriorityQueue<SuggestWord> {
  SuggestWordQueue (int size) {
    initialize(size);
  }
  @Override
  protected final boolean lessThan (SuggestWord wa, SuggestWord wb) {
    int val = wa.compareTo(wb);
    return val < 0;
  }
}
