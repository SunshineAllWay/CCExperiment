package org.apache.lucene.queryParser.core.nodes;
public interface FieldableNode extends QueryNode {
  CharSequence getField();
  void setField(CharSequence fieldName);
}
