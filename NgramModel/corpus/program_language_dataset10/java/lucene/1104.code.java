package org.apache.lucene.queryParser.core.nodes;
import java.util.List;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class AndQueryNode extends BooleanQueryNode {
  private static final long serialVersionUID = 118496077529151825L;
  public AndQueryNode(List<QueryNode> clauses) {
    super(clauses);
    if ((clauses == null) || (clauses.size() == 0)) {
      throw new IllegalArgumentException(
          "AND query must have at least one clause");
    }
  }
  @Override
  public String toString() {
    if (getChildren() == null || getChildren().size() == 0)
      return "<boolean operation='and'/>";
    StringBuilder sb = new StringBuilder();
    sb.append("<boolean operation='and'>");
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
    for (QueryNode child : getChildren()) {
      sb.append(filler).append(child.toQueryString(escapeSyntaxParser));
      filler = " AND ";
    }
    if ((getParent() != null && getParent() instanceof GroupQueryNode)
        || isRoot())
      return sb.toString();
    else
      return "( " + sb.toString() + " )";
  }
}
