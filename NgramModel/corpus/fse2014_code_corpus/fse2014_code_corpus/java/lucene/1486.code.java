package org.apache.lucene.document;
public enum FieldSelectorResult {
  LOAD,
  LAZY_LOAD,
  NO_LOAD,
  LOAD_AND_BREAK,
  SIZE,
  SIZE_AND_BREAK
}
