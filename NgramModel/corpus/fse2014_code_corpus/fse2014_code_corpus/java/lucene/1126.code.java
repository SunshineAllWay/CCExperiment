package org.apache.lucene.queryParser.core.nodes;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class QuotedFieldQueryNode extends FieldQueryNode {
  private static final long serialVersionUID = -6675157780051428987L;
  public QuotedFieldQueryNode(CharSequence field, CharSequence text, int begin,
      int end) {
    super(field, text, begin, end);
  }
  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    if (isDefaultField(this.field)) {
      return "\"" + getTermEscapeQuoted(escaper) + "\"";
    } else {
      return this.field + ":" + "\"" + getTermEscapeQuoted(escaper) + "\"";
    }
  }
  @Override
  public String toString() {
    return "<quotedfield start='" + this.begin + "' end='" + this.end
        + "' field='" + this.field + "' term='" + this.text + "'/>";
  }
  @Override
  public QuotedFieldQueryNode cloneTree() throws CloneNotSupportedException {
    QuotedFieldQueryNode clone = (QuotedFieldQueryNode) super.cloneTree();
    return clone;
  }
}
