package org.apache.lucene.queryParser.core.nodes;
import java.util.List;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeError;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class BoostQueryNode extends QueryNodeImpl {
  private static final long serialVersionUID = -3929082630855807593L;
  private float value = 0;
  public BoostQueryNode(QueryNode query, float value) throws QueryNodeException {
    if (query == null) {
      throw new QueryNodeError(new MessageImpl(
          QueryParserMessages.NODE_ACTION_NOT_SUPPORTED, "query", "null"));
    }
    this.value = value;
    setLeaf(false);
    allocate();
    add(query);
  }
  public QueryNode getChild() {
    List<QueryNode> children = getChildren();
    if (children == null || children.size() == 0) {
      return null;
    }
    return children.get(0);
  }
  public float getValue() {
    return this.value;
  }
  private CharSequence getValueString() {
    Float f = Float.valueOf(this.value);
    if (f == f.longValue())
      return "" + f.longValue();
    else
      return "" + f;
  }
  @Override
  public String toString() {
    return "<boost value='" + getValueString() + "'>" + "\n"
        + getChild().toString() + "\n</boost>";
  }
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChild() == null)
      return "";
    return getChild().toQueryString(escapeSyntaxParser) + "^"
        + getValueString();
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    BoostQueryNode clone = (BoostQueryNode) super.cloneTree();
    clone.value = this.value;
    return clone;
  }
}
