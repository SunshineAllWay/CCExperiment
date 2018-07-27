package org.apache.lucene.index;
import org.apache.lucene.util.AttributeSource;
public final class FieldInvertState {
  int position;
  int length;
  int numOverlap;
  int offset;
  float boost;
  AttributeSource attributeSource;
  public FieldInvertState() {
  }
  public FieldInvertState(int position, int length, int numOverlap, int offset, float boost) {
    this.position = position;
    this.length = length;
    this.numOverlap = numOverlap;
    this.offset = offset;
    this.boost = boost;
  }
  void reset(float docBoost) {
    position = 0;
    length = 0;
    numOverlap = 0;
    offset = 0;
    boost = docBoost;
    attributeSource = null;
  }
  public int getPosition() {
    return position;
  }
  public int getLength() {
    return length;
  }
  public int getNumOverlap() {
    return numOverlap;
  }
  public int getOffset() {
    return offset;
  }
  public float getBoost() {
    return boost;
  }
  public AttributeSource getAttributeSource() {
    return attributeSource;
  }
}
