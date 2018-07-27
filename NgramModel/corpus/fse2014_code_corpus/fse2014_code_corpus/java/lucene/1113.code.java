package org.apache.lucene.queryParser.core.nodes;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class MatchAllDocsQueryNode extends QueryNodeImpl {
  private static final long serialVersionUID = -7050381275423477809L;
  public MatchAllDocsQueryNode() {
  }
  @Override
  public String toString() {
    return "<matchAllDocs field='*' term='*'/>";
  }
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    return "*:*";
  }
  @Override
  public MatchAllDocsQueryNode cloneTree() throws CloneNotSupportedException {
    MatchAllDocsQueryNode clone = (MatchAllDocsQueryNode) super.cloneTree();
    return clone;
  }
}
