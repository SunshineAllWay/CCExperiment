package org.apache.lucene.queryParser.core.nodes;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryParser.core.processors.RemoveDeletedQueryNodesProcessor;
public class DeletedQueryNode extends QueryNodeImpl {
  private static final long serialVersionUID = -9151675506000425293L;
  public DeletedQueryNode() {
  }
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    return "[DELETEDCHILD]";
  }
  @Override
  public String toString() {
    return "<deleted/>";
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    DeletedQueryNode clone = (DeletedQueryNode) super.cloneTree();
    return clone;
  }
}
