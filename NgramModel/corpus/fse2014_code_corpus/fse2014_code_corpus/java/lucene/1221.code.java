package org.apache.lucene.queryParser.standard.processors;
import java.util.LinkedList;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.GroupQueryNode;
import org.apache.lucene.queryParser.core.nodes.MatchNoDocsQueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
public class RemoveEmptyNonLeafQueryNodeProcessor extends
    QueryNodeProcessorImpl {
  private LinkedList<QueryNode> childrenBuffer = new LinkedList<QueryNode>();
  public RemoveEmptyNonLeafQueryNodeProcessor() {
  }
  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    queryTree = super.process(queryTree);
    if (!queryTree.isLeaf()) {
      List<QueryNode> children = queryTree.getChildren();
      if (children == null || children.size() == 0) {
        return new MatchNoDocsQueryNode();
      }
    }
    return queryTree;
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    try {
      for (QueryNode child : children) {
        if (!child.isLeaf()) {
          List<QueryNode> grandChildren = child.getChildren();
          if (grandChildren != null && grandChildren.size() > 0) {
            this.childrenBuffer.add(child);
          }
        } else {
          this.childrenBuffer.add(child);
        }
      }
      children.clear();
      children.addAll(this.childrenBuffer);
    } finally {
      this.childrenBuffer.clear();
    }
    return children;
  }
}
