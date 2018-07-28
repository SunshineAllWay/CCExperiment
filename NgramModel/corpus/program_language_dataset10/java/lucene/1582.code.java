package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.util.PriorityQueue;
final class SegmentMergeQueue extends PriorityQueue<SegmentMergeInfo> {
  SegmentMergeQueue(int size) {
    initialize(size);
  }
  @Override
  protected final boolean lessThan(SegmentMergeInfo stiA, SegmentMergeInfo stiB) {
    int comparison = stiA.term.compareTo(stiB.term);
    if (comparison == 0)
      return stiA.base < stiB.base; 
    else
      return comparison < 0;
  }
  final void close() throws IOException {
    while (top() != null)
      pop().close();
  }
}
