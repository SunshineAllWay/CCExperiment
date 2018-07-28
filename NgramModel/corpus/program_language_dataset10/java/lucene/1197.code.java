package org.apache.lucene.queryParser.standard.nodes;
import java.text.Collator;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricRangeQueryNode;
import org.apache.lucene.queryParser.standard.config.RangeCollatorAttribute;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
public class RangeQueryNode extends ParametricRangeQueryNode {
  private static final long serialVersionUID = 7400866652044314657L;
  private Collator collator;
  public RangeQueryNode(ParametricQueryNode lower, ParametricQueryNode upper, Collator collator) {
    super(lower, upper);
    this.collator = collator;
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("<range>\n\t");
    sb.append(this.getUpperBound()).append("\n\t");
    sb.append(this.getLowerBound()).append("\n");
    sb.append("</range>\n");
    return sb.toString();
  }
  public Collator getCollator() {
    return this.collator;
  }
}
