package org.apache.lucene.index;
public interface TermPositionVector extends TermFreqVector {
    public int[] getTermPositions(int index);
    public TermVectorOffsetInfo [] getOffsets(int index);
}