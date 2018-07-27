package org.apache.lucene.queryParser.core.processors;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
public abstract class QueryNodeProcessorImpl implements QueryNodeProcessor {
  private ArrayList<ChildrenList> childrenListPool = new ArrayList<ChildrenList>();
  private QueryConfigHandler queryConfig;
  public QueryNodeProcessorImpl() {
  }
  public QueryNodeProcessorImpl(QueryConfigHandler queryConfigHandler) {
    this.queryConfig = queryConfigHandler;
  }
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    return processIteration(queryTree);
  }
  private QueryNode processIteration(QueryNode queryTree)
      throws QueryNodeException {
    queryTree = preProcessNode(queryTree);
    processChildren(queryTree);
    queryTree = postProcessNode(queryTree);
    return queryTree;
  }
  protected void processChildren(QueryNode queryTree) throws QueryNodeException {
    List<QueryNode> children = queryTree.getChildren();
    ChildrenList newChildren;
    if (children != null && children.size() > 0) {
      newChildren = allocateChildrenList();
      try {
        for (QueryNode child : children) {
          child = processIteration(child);
          if (child == null) {
            throw new NullPointerException();
          }
          newChildren.add(child);
        }
        List<QueryNode> orderedChildrenList = setChildrenOrder(newChildren);
        queryTree.set(orderedChildrenList);
      } finally {
        newChildren.beingUsed = false;
      }
    }
  }
  private ChildrenList allocateChildrenList() {
    ChildrenList list = null;
    for (ChildrenList auxList : this.childrenListPool) {
      if (!auxList.beingUsed) {
        list = auxList;
        list.clear();
        break;
      }
    }
    if (list == null) {
      list = new ChildrenList();
      this.childrenListPool.add(list);
    }
    list.beingUsed = true;
    return list;
  }
  public void setQueryConfigHandler(QueryConfigHandler queryConfigHandler) {
    this.queryConfig = queryConfigHandler;
  }
  public QueryConfigHandler getQueryConfigHandler() {
    return this.queryConfig;
  }
  abstract protected QueryNode preProcessNode(QueryNode node)
      throws QueryNodeException;
  abstract protected QueryNode postProcessNode(QueryNode node)
      throws QueryNodeException;
  abstract protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException;
  private static class ChildrenList extends ArrayList<QueryNode> {
    private static final long serialVersionUID = -2613518456949297135L;
    boolean beingUsed;
  }
}
