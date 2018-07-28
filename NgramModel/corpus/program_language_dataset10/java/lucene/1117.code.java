package org.apache.lucene.queryParser.core.nodes;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class OpaqueQueryNode extends QueryNodeImpl {
  private static final long serialVersionUID = 0L;
  private CharSequence schema = null;
  private CharSequence value = null;
  public OpaqueQueryNode(CharSequence schema, CharSequence value) {
    this.setLeaf(true);
    this.schema = schema;
    this.value = value;
  }
  @Override
  public String toString() {
    return "<opaque schema='" + this.schema + "' value='" + this.value + "'/>";
  }
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    return "@" + this.schema + ":'" + this.value + "'";
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    OpaqueQueryNode clone = (OpaqueQueryNode) super.cloneTree();
    clone.schema = this.schema;
    clone.value = this.value;
    return clone;
  }
  public CharSequence getSchema() {
    return this.schema;
  }
  public CharSequence getValue() {
    return this.value;
  }
}
