package org.apache.lucene.queryParser.standard.nodes;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
public class PrefixWildcardQueryNode extends WildcardQueryNode {
  private static final long serialVersionUID = 6851557641826407515L;
  public PrefixWildcardQueryNode(CharSequence field, CharSequence text,
      int begin, int end) {
    super(field, text, begin, end);
  }
  public PrefixWildcardQueryNode(FieldQueryNode fqn) {
    this(fqn.getField(), fqn.getText(), fqn.getBegin(), fqn.getEnd());
  }
  @Override
  public String toString() {
    return "<prefixWildcard field='" + this.field + "' term='" + this.text
        + "'/>";
  }
  @Override
  public PrefixWildcardQueryNode cloneTree() throws CloneNotSupportedException {
    PrefixWildcardQueryNode clone = (PrefixWildcardQueryNode) super.cloneTree();
    return clone;
  }
}
