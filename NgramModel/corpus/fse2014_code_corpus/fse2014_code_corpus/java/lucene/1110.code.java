package org.apache.lucene.queryParser.core.nodes;
import java.util.Locale;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax.Type;
public class FieldQueryNode extends QueryNodeImpl implements TextableQueryNode,
    FieldableNode {
  private static final long serialVersionUID = 3634521145130758265L;
  protected CharSequence field;
  protected CharSequence text;
  protected int begin;
  protected int end;
  protected int positionIncrement;
  public FieldQueryNode(CharSequence field, CharSequence text, int begin,
      int end) {
    this.field = field;
    this.text = text;
    this.begin = begin;
    this.end = end;
    this.setLeaf(true);
  }
  protected CharSequence getTermEscaped(EscapeQuerySyntax escaper) {
    return escaper.escape(this.text, Locale.getDefault(), Type.NORMAL);
  }
  protected CharSequence getTermEscapeQuoted(EscapeQuerySyntax escaper) {
    return escaper.escape(this.text, Locale.getDefault(), Type.STRING);
  }
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    if (isDefaultField(this.field)) {
      return getTermEscaped(escaper);
    } else {
      return this.field + ":" + getTermEscaped(escaper);
    }
  }
  @Override
  public String toString() {
    return "<field start='" + this.begin + "' end='" + this.end + "' field='"
        + this.field + "' text='" + this.text + "'/>";
  }
  public String getTextAsString() {
    if (this.text == null)
      return null;
    else
      return this.text.toString();
  }
  public String getFieldAsString() {
    if (this.field == null)
      return null;
    else
      return this.field.toString();
  }
  public int getBegin() {
    return this.begin;
  }
  public void setBegin(int begin) {
    this.begin = begin;
  }
  public int getEnd() {
    return this.end;
  }
  public void setEnd(int end) {
    this.end = end;
  }
  public CharSequence getField() {
    return this.field;
  }
  public void setField(CharSequence field) {
    this.field = field;
  }
  public int getPositionIncrement() {
    return this.positionIncrement;
  }
  public void setPositionIncrement(int pi) {
    this.positionIncrement = pi;
  }
  public CharSequence getText() {
    return this.text;
  }
  public void setText(CharSequence text) {
    this.text = text;
  }
  @Override
  public FieldQueryNode cloneTree() throws CloneNotSupportedException {
    FieldQueryNode fqn = (FieldQueryNode) super.cloneTree();
    fqn.begin = this.begin;
    fqn.end = this.end;
    fqn.field = this.field;
    fqn.text = this.text;
    fqn.positionIncrement = this.positionIncrement;
    fqn.toQueryStringIgnoreFields = this.toQueryStringIgnoreFields;
    return fqn;
  }
}
