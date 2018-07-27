package org.apache.lucene.queryParser.core.nodes;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeError;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class ModifierQueryNode extends QueryNodeImpl {
  private static final long serialVersionUID = -391209837953928169L;
  public enum Modifier {
    MOD_NONE, MOD_NOT, MOD_REQ;
    @Override
    public String toString() {
      switch (this) {
      case MOD_NONE:
        return "MOD_NONE";
      case MOD_NOT:
        return "MOD_NOT";
      case MOD_REQ:
        return "MOD_REQ";
      }
      return "MOD_DEFAULT";
    }
    public String toDigitString() {
      switch (this) {
      case MOD_NONE:
        return "";
      case MOD_NOT:
        return "-";
      case MOD_REQ:
        return "+";
      }
      return "";
    }
    public String toLargeString() {
      switch (this) {
      case MOD_NONE:
        return "";
      case MOD_NOT:
        return "NOT ";
      case MOD_REQ:
        return "+";
      }
      return "";
    }
  }
  private Modifier modifier = Modifier.MOD_NONE;
  public ModifierQueryNode(QueryNode query, Modifier mod) {
    if (query == null) {
      throw new QueryNodeError(new MessageImpl(
          QueryParserMessages.PARAMETER_VALUE_NOT_SUPPORTED, "query", "null"));
    }
    allocate();
    setLeaf(false);
    add(query);
    this.modifier = mod;
  }
  public QueryNode getChild() {
    return getChildren().get(0);
  }
  public Modifier getModifier() {
    return this.modifier;
  }
  @Override
  public String toString() {
    return "<modifier operation='" + this.modifier.toString() + "'>" + "\n"
        + getChild().toString() + "\n</modifier>";
  }
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChild() == null)
      return "";
    String leftParenthensis = "";
    String rightParenthensis = "";
    if (getChild() != null && getChild() instanceof ModifierQueryNode) {
      leftParenthensis = "(";
      rightParenthensis = ")";
    }
    if (getChild() instanceof BooleanQueryNode) {
      return this.modifier.toLargeString() + leftParenthensis
          + getChild().toQueryString(escapeSyntaxParser) + rightParenthensis;
    } else {
      return this.modifier.toDigitString() + leftParenthensis
          + getChild().toQueryString(escapeSyntaxParser) + rightParenthensis;
    }
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    ModifierQueryNode clone = (ModifierQueryNode) super.cloneTree();
    clone.modifier = this.modifier;
    return clone;
  }
  public void setChild(QueryNode child) {
    List<QueryNode> list = new ArrayList<QueryNode>();
    list.add(child);
    this.set(list);
  }
}
