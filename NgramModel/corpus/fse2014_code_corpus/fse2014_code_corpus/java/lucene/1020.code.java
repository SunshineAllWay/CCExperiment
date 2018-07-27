package org.apache.lucene.store.instantiated;
import java.io.Serializable;
class FieldSetting implements Serializable {
  String fieldName;
  boolean storeTermVector = false;
  boolean storeOffsetWithTermVector = false;
  boolean storePositionWithTermVector = false;
  boolean storePayloads = false;
  boolean stored = false;
  boolean indexed = false;
  boolean tokenized = false;
  FieldSetting() {
  }
  FieldSetting(String fieldName) {
    this.fieldName = fieldName;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    final FieldSetting that = (FieldSetting) o;
    return fieldName.equals(that.fieldName);
  }
  @Override
  public int hashCode() {
    return fieldName.hashCode();
  }
}
