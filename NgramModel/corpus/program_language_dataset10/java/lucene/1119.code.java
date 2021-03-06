package org.apache.lucene.queryParser.core.nodes;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class ParametricQueryNode extends FieldQueryNode {
  private static final long serialVersionUID = -5770038129741218116L;
  private CompareOperator operator;
  public enum CompareOperator {
    LE { 
      @Override
      public String toString() { return "<="; }
    },
    LT {
      @Override
      public String toString() { return "<";  }
    },
    GE {
      @Override
      public String toString() { return ">="; }
    },
    GT {
      @Override
      public String toString() { return ">";  }
    },
    EQ {
      @Override
      public String toString() { return "=";  }
    },
    NE {
      @Override
      public String toString() { return "!="; }
    };
  }
  public ParametricQueryNode(CharSequence field, CompareOperator comp,
      CharSequence value, int begin, int end) {
    super(field, value, begin, end);
    this.operator = comp;
    setLeaf(true);
  }
  public CharSequence getOperand() {
    return getText();
  }
  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    return this.field + "" + this.operator.toString() + "\"" + this.text + "\"";
  }
  @Override
  public String toString() {
    return "<parametric field='" + this.field + "' operator='"
        + this.operator.toString() + "' text='" + this.text + "'/>";
  }
  @Override
  public ParametricQueryNode cloneTree() throws CloneNotSupportedException {
    ParametricQueryNode clone = (ParametricQueryNode) super.cloneTree();
    clone.operator = this.operator;
    return clone;
  }
  public CompareOperator getOperator() {
    return this.operator;
  }
}
