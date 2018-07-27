package org.apache.lucene.queryParser.core.nodes;
import java.util.List;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class TokenizedPhraseQueryNode extends QueryNodeImpl implements
    FieldableNode {
  private static final long serialVersionUID = -7185108320787917541L;
  public TokenizedPhraseQueryNode() {
    setLeaf(false);
    allocate();
  }
  @Override
  public String toString() {
    if (getChildren() == null || getChildren().size() == 0)
      return "<tokenizedphrase/>";
    StringBuilder sb = new StringBuilder();
    sb.append("<tokenizedtphrase>");
    for (QueryNode child : getChildren()) {
      sb.append("\n");
      sb.append(child.toString());
    }
    sb.append("\n</tokenizedphrase>");
    return sb.toString();
  }
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChildren() == null || getChildren().size() == 0)
      return "";
    StringBuilder sb = new StringBuilder();
    String filler = "";
    for (QueryNode child : getChildren()) {
      sb.append(filler).append(child.toQueryString(escapeSyntaxParser));
      filler = ",";
    }
    return "[TP[" + sb.toString() + "]]";
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    TokenizedPhraseQueryNode clone = (TokenizedPhraseQueryNode) super
        .cloneTree();
    return clone;
  }
  public CharSequence getField() {
    List<QueryNode> children = getChildren();
    if (children == null || children.size() == 0) {
      return null;
    } else {
      return ((FieldableNode) children.get(0)).getField();
    }
  }
  public void setField(CharSequence fieldName) {
    List<QueryNode> children = getChildren();
    if (children != null) {
      for (QueryNode child : getChildren()) {
        if (child instanceof FieldableNode) {
          ((FieldableNode) child).setField(fieldName);
        }
      }
    }
  }
} 
