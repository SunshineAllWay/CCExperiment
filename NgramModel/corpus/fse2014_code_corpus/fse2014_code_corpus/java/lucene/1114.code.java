package org.apache.lucene.queryParser.core.nodes;
public class MatchNoDocsQueryNode extends DeletedQueryNode {
  private static final long serialVersionUID = 8081805751679581497L;
  public MatchNoDocsQueryNode() {
  }
  @Override
  public String toString() {
    return "<matchNoDocsQueryNode/>";
  }
}
