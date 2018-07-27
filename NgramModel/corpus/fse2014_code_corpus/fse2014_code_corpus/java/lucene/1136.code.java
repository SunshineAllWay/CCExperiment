package org.apache.lucene.queryParser.core.processors;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.DeletedQueryNode;
import org.apache.lucene.queryParser.core.nodes.MatchNoDocsQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
public class RemoveDeletedQueryNodesProcessor extends QueryNodeProcessorImpl {
  public RemoveDeletedQueryNodesProcessor() {
  }
  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    queryTree = super.process(queryTree);
    if (queryTree instanceof DeletedQueryNode
        && !(queryTree instanceof MatchNoDocsQueryNode)) {
      return new MatchNoDocsQueryNode();
    }
    return queryTree;
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (!node.isLeaf()) {
      List<QueryNode> children = node.getChildren();
      boolean removeBoolean = false;
      if (children == null || children.size() == 0) {
        removeBoolean = true;
      } else {
        removeBoolean = true;
        for (Iterator<QueryNode> it = children.iterator(); it.hasNext();) {
          if (!(it.next() instanceof DeletedQueryNode)) {
            removeBoolean = false;
            break;
          }
        }
      }
      if (removeBoolean) {
        return new DeletedQueryNode();
      }
    }
    return node;
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    for (int i = 0; i < children.size(); i++) {
      if (children.get(i) instanceof DeletedQueryNode) {
        children.remove(i--);
      }
    }
    return children;
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }
}
