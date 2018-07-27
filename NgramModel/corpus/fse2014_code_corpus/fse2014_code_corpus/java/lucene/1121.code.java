package org.apache.lucene.queryParser.core.nodes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax.Type;
public class PathQueryNode extends QueryNodeImpl {
  private static final long serialVersionUID = -8325921322405804789L;
  public static class QueryText implements Cloneable {
    CharSequence value = null;
    int begin;
    int end;
    public QueryText(CharSequence value, int begin, int end) {
      super();
      this.value = value;
      this.begin = begin;
      this.end = end;
    }
    @Override
    public QueryText clone() throws CloneNotSupportedException {
      QueryText clone = (QueryText) super.clone();
      clone.value = this.value;
      clone.begin = this.begin;
      clone.end = this.end;
      return clone;
    }
    public CharSequence getValue() {
      return value;
    }
    public int getBegin() {
      return begin;
    }
    public int getEnd() {
      return end;
    }
    @Override
    public String toString() {
      return value + ", " + begin + ", " + end;
    }
  }
  private List<QueryText> values = null;
  public PathQueryNode(List<QueryText> pathElements) {
    this.values = pathElements;
    if (pathElements.size() <= 1) {
      throw new RuntimeException(
          "PathQuerynode requires more 2 or more path elements.");
    }
  }
  public List<QueryText> getPathElements() {
    return values;
  }
  public void setPathElements(List<QueryText> elements) {
    this.values = elements;
  }
  public QueryText getPathElement(int index) {
    return values.get(index);
  }
  public CharSequence getFirstPathElement() {
    return values.get(0).value;
  }
  public List<QueryText> getPathElements(int startIndex) {
    List<PathQueryNode.QueryText> rValues = new ArrayList<PathQueryNode.QueryText>();
    for (int i = startIndex; i < this.values.size(); i++) {
      try {
        rValues.add(this.values.get(i).clone());
      } catch (CloneNotSupportedException e) {
      }
    }
    return rValues;
  }
  private CharSequence getPathString() {
    StringBuilder path = new StringBuilder();
    for (QueryText pathelement : values) {
      path.append("/").append(pathelement.value);
    }
    return path.toString();
  }
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    StringBuilder path = new StringBuilder();
    path.append("/").append(getFirstPathElement());
    for (QueryText pathelement : getPathElements(1)) {
      CharSequence value = escaper.escape(pathelement.value, Locale
          .getDefault(), Type.STRING);
      path.append("/\"").append(value).append("\"");
    }
    return path.toString();
  }
  @Override
  public String toString() {
    QueryText text = this.values.get(0);
    return "<path start='" + text.begin + "' end='" + text.end + "' path='"
        + getPathString() + "'/>";
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    PathQueryNode clone = (PathQueryNode) super.cloneTree();
    if (this.values != null) {
      List<QueryText> localValues = new ArrayList<QueryText>();
      for (QueryText value : this.values) {
        localValues.add(value.clone());
      }
      clone.values = localValues;
    }
    return clone;
  }
}
