package org.apache.lucene.queryParser.core.nodes;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.lucene.messages.NLS;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
public abstract class QueryNodeImpl implements QueryNode, Cloneable {
  private static final long serialVersionUID = 5569870883474845989L;
  public static final String PLAINTEXT_FIELD_NAME = "_plain";
  private boolean isLeaf = true;
  private Hashtable<CharSequence, Object> tags = new Hashtable<CharSequence, Object>();
  private List<QueryNode> clauses = null;
  protected void allocate() {
    if (this.clauses == null) {
      this.clauses = new ArrayList<QueryNode>();
    } else {
      this.clauses.clear();
    }
  }
  public final void add(QueryNode child) {
    if (isLeaf() || this.clauses == null || child == null) {
      throw new IllegalArgumentException(NLS
          .getLocalizedMessage(QueryParserMessages.NODE_ACTION_NOT_SUPPORTED));
    }
    this.clauses.add(child);
    ((QueryNodeImpl) child).setParent(this);
  }
  public final void add(List<QueryNode> children) {
    if (isLeaf() || this.clauses == null) {
      throw new IllegalArgumentException(NLS
          .getLocalizedMessage(QueryParserMessages.NODE_ACTION_NOT_SUPPORTED));
    }
    for (QueryNode child : children) {
      add(child);
    }
  }
  public boolean isLeaf() {
    return this.isLeaf;
  }
  public final void set(List<QueryNode> children) {
    if (isLeaf() || this.clauses == null) {
      ResourceBundle bundle = ResourceBundle
          .getBundle("org.apache.lucene.queryParser.messages.QueryParserMessages");
      String message = bundle.getObject("Q0008E.NODE_ACTION_NOT_SUPPORTED")
          .toString();
      throw new IllegalArgumentException(message);
    }
    for (QueryNode child : children) {
      ((QueryNodeImpl) child).setParent(null);
    }
    allocate();
    for (QueryNode child : children) {
      add(child);
    }
  }
  public QueryNode cloneTree() throws CloneNotSupportedException {
    QueryNodeImpl clone = (QueryNodeImpl) super.clone();
    clone.isLeaf = this.isLeaf;
    clone.tags = new Hashtable<CharSequence, Object>();
    if (this.clauses != null) {
      List<QueryNode> localClauses = new ArrayList<QueryNode>();
      for (QueryNode clause : this.clauses) {
        localClauses.add(clause.cloneTree());
      }
      clone.clauses = localClauses;
    }
    return clone;
  }
  @Override
  public Object clone() throws CloneNotSupportedException {
    return cloneTree();
  }
  protected void setLeaf(boolean isLeaf) {
    this.isLeaf = isLeaf;
  }
  public final List<QueryNode> getChildren() {
    if (isLeaf() || this.clauses == null) {
      return null;
    }
    return this.clauses;
  }
  public void setTag(CharSequence tagName, Object value) {
    this.tags.put(tagName.toString().toLowerCase(), value);
  }
  public void unsetTag(CharSequence tagName) {
    this.tags.remove(tagName.toString().toLowerCase());
  }
  public boolean containsTag(CharSequence tagName) {
    return this.tags.containsKey(tagName.toString().toLowerCase());
  }
  public Object getTag(CharSequence tagName) {
    return this.tags.get(tagName.toString().toLowerCase());
  }
  private QueryNode parent = null;
  private void setParent(QueryNode parent) {
    this.parent = parent;
  }
  public QueryNode getParent() {
    return this.parent;
  }
  protected boolean isRoot() {
    return getParent() == null;
  }
  protected boolean toQueryStringIgnoreFields = false;
  protected boolean isDefaultField(CharSequence fld) {
    if (this.toQueryStringIgnoreFields)
      return true;
    if (fld == null)
      return true;
    if (QueryNodeImpl.PLAINTEXT_FIELD_NAME.equals(fld.toString()))
      return true;
    return false;
  }
  @Override
  public String toString() {
    return super.toString();
  }
  @SuppressWarnings( { "unchecked" })
  public Map<CharSequence, Object> getTags() {
    return (Map<CharSequence, Object>) this.tags.clone();
  }
} 
