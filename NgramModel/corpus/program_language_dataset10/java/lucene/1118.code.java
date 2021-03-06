package org.apache.lucene.queryParser.core.nodes;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class OrQueryNode extends BooleanQueryNode {
  private static final long serialVersionUID = -3692323307688017852L;
  public OrQueryNode(List<QueryNode> clauses) {
    super(clauses);
    if ((clauses == null) || (clauses.size() == 0)) {
      throw new IllegalArgumentException(
          "OR query must have at least one clause");
    }
  }
  @Override
  public String toString() {
    if (getChildren() == null || getChildren().size() == 0)
      return "<boolean operation='or'/>";
    StringBuilder sb = new StringBuilder();
    sb.append("<boolean operation='or'>");
    for (QueryNode child : getChildren()) {
      sb.append("\n");
      sb.append(child.toString());
    }
    sb.append("\n</boolean>");
    return sb.toString();
  }
  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChildren() == null || getChildren().size() == 0)
      return "";
    StringBuilder sb = new StringBuilder();
    String filler = "";
    for (Iterator<QueryNode> it = getChildren().iterator(); it.hasNext();) {
      sb.append(filler).append(it.next().toQueryString(escapeSyntaxParser));
      filler = " OR ";
    }
    if ((getParent() != null && getParent() instanceof GroupQueryNode)
        || isRoot())
      return sb.toString();
    else
      return "( " + sb.toString() + " )";
  }
}
