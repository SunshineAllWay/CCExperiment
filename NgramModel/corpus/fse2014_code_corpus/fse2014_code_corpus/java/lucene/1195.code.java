package org.apache.lucene.queryParser.standard.nodes;
import java.util.List;
import org.apache.lucene.queryParser.core.nodes.FieldableNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNodeImpl;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
public class MultiPhraseQueryNode extends QueryNodeImpl implements
    FieldableNode {
  private static final long serialVersionUID = -2138501723963320158L;
  public MultiPhraseQueryNode() {
    setLeaf(false);
    allocate();
  }
  @Override
  public String toString() {
    if (getChildren() == null || getChildren().size() == 0)
      return "<multiPhrase/>";
    StringBuilder sb = new StringBuilder();
    sb.append("<multiPhrase>");
    for (QueryNode child : getChildren()) {
      sb.append("\n");
      sb.append(child.toString());
    }
    sb.append("\n</multiPhrase>");
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
    return "[MTP[" + sb.toString() + "]]";
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    MultiPhraseQueryNode clone = (MultiPhraseQueryNode) super.cloneTree();
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
      for (QueryNode child : children) {
        if (child instanceof FieldableNode) {
          ((FieldableNode) child).setField(fieldName);
        }
      }
    }
  }
} 
