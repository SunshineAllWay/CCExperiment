package org.apache.lucene.queryParser.core.nodes;
public interface TextableQueryNode {
  CharSequence getText();
  void setText(CharSequence text);
}
