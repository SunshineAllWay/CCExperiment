package org.apache.lucene.queryParser.core.nodes;
import java.util.List;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class BooleanQueryNode extends QueryNodeImpl {
  private static final long serialVersionUID = -2206623652088638072L;
  public BooleanQueryNode(List<QueryNode> clauses) {
    setLeaf(false);
    allocate();
    set(clauses);
  }
  @Override
  public String toString() {
    if (getChildren() == null || getChildren().size() == 0)
      return "<boolean operation='default'/>";
    StringBuilder sb = new StringBuilder();
    sb.append("<boolean operation='default'>");
    for (QueryNode child : getChildren()) {
      sb.append("\n");
      sb.append(child.toString());
    }
    sb.append("\n</boolean>");
    return sb.toString();
  }
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChildren() == null || getChildren().size() == 0)
      return "";
    StringBuilder sb = new StringBuilder();
    String filler = "";
    for (QueryNode child : getChildren()) {
      sb.append(filler).append(child.toQueryString(escapeSyntaxParser));
      filler = " ";
    }
    if ((getParent() != null && getParent() instanceof GroupQueryNode)
        || isRoot())
      return sb.toString();
    else
      return "( " + sb.toString() + " )";
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    BooleanQueryNode clone = (BooleanQueryNode) super.cloneTree();
    return clone;
  }
}
