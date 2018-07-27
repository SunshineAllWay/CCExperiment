package org.apache.lucene.queryParser.core.nodes;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class FuzzyQueryNode extends FieldQueryNode {
  private static final long serialVersionUID = -1794537213032589441L;
  private float similarity;
  private int prefixLength;
  public FuzzyQueryNode(CharSequence field, CharSequence term,
      float minSimilarity, int begin, int end) {
    super(field, term, begin, end);
    this.similarity = minSimilarity;
    setLeaf(true);
  }
  public void setPrefixLength(int prefixLength) {
    this.prefixLength = prefixLength;
  }
  public int getPrefixLength() {
    return this.prefixLength;
  }
  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    if (isDefaultField(this.field)) {
      return getTermEscaped(escaper) + "~" + this.similarity;
    } else {
      return this.field + ":" + getTermEscaped(escaper) + "~" + this.similarity;
    }
  }
  @Override
  public String toString() {
    return "<fuzzy field='" + this.field + "' similarity='" + this.similarity
        + "' term='" + this.text + "'/>";
  }
  public void setSimilarity(float similarity) {
    this.similarity = similarity;
  }
  @Override
  public FuzzyQueryNode cloneTree() throws CloneNotSupportedException {
    FuzzyQueryNode clone = (FuzzyQueryNode) super.cloneTree();
    clone.similarity = this.similarity;
    return clone;
  }
  public float getSimilarity() {
    return this.similarity;
  }
}
