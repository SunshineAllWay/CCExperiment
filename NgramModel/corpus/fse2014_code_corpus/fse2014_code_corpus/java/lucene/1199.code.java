package org.apache.lucene.queryParser.standard.nodes;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class WildcardQueryNode extends FieldQueryNode {
  private static final long serialVersionUID = 0L;
  public WildcardQueryNode(CharSequence field, CharSequence text, int begin,
      int end) {
    super(field, text, begin, end);
  }
  public WildcardQueryNode(FieldQueryNode fqn) {
    this(fqn.getField(), fqn.getText(), fqn.getBegin(), fqn.getEnd());
  }
  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    if (isDefaultField(this.field)) {
      return getTermEscaped(escaper);
    } else {
      return this.field + ":" + getTermEscaped(escaper);
    }
  }
  @Override
  public String toString() {
    return "<wildcard field='" + this.field + "' term='" + this.text + "'/>";
  }
  @Override
  public WildcardQueryNode cloneTree() throws CloneNotSupportedException {
    WildcardQueryNode clone = (WildcardQueryNode) super.cloneTree();
    return clone;
  }
}
