package org.apache.lucene.document;
public class LoadFirstFieldSelector implements FieldSelector {
  public FieldSelectorResult accept(String fieldName) {
    return FieldSelectorResult.LOAD_AND_BREAK;
  }
}