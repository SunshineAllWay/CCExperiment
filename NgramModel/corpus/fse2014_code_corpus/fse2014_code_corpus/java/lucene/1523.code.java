package org.apache.lucene.index;
final class FieldInfo {
  String name;
  boolean isIndexed;
  int number;
  boolean storeTermVector;
  boolean storeOffsetWithTermVector;
  boolean storePositionWithTermVector;
  boolean omitNorms; 
  boolean omitTermFreqAndPositions;
  boolean storePayloads; 
  FieldInfo(String na, boolean tk, int nu, boolean storeTermVector, 
            boolean storePositionWithTermVector,  boolean storeOffsetWithTermVector, 
            boolean omitNorms, boolean storePayloads, boolean omitTermFreqAndPositions) {
    name = na;
    isIndexed = tk;
    number = nu;
    if (isIndexed) {
      this.storeTermVector = storeTermVector;
      this.storeOffsetWithTermVector = storeOffsetWithTermVector;
      this.storePositionWithTermVector = storePositionWithTermVector;
      this.storePayloads = storePayloads;
      this.omitNorms = omitNorms;
      this.omitTermFreqAndPositions = omitTermFreqAndPositions;
    } else { 
      this.storeTermVector = false;
      this.storeOffsetWithTermVector = false;
      this.storePositionWithTermVector = false;
      this.storePayloads = false;
      this.omitNorms = true;
      this.omitTermFreqAndPositions = false;
    }
  }
  @Override
  public Object clone() {
    return new FieldInfo(name, isIndexed, number, storeTermVector, storePositionWithTermVector,
                         storeOffsetWithTermVector, omitNorms, storePayloads, omitTermFreqAndPositions);
  }
  void update(boolean isIndexed, boolean storeTermVector, boolean storePositionWithTermVector, 
              boolean storeOffsetWithTermVector, boolean omitNorms, boolean storePayloads, boolean omitTermFreqAndPositions) {
    if (this.isIndexed != isIndexed) {
      this.isIndexed = true;                      
    }
    if (isIndexed) { 
      if (this.storeTermVector != storeTermVector) {
        this.storeTermVector = true;                
      }
      if (this.storePositionWithTermVector != storePositionWithTermVector) {
        this.storePositionWithTermVector = true;                
      }
      if (this.storeOffsetWithTermVector != storeOffsetWithTermVector) {
        this.storeOffsetWithTermVector = true;                
      }
      if (this.storePayloads != storePayloads) {
        this.storePayloads = true;
      }
      if (this.omitNorms != omitNorms) {
        this.omitNorms = false;                
      }
      if (this.omitTermFreqAndPositions != omitTermFreqAndPositions) {
        this.omitTermFreqAndPositions = true;                
      }
    }
  }
}
