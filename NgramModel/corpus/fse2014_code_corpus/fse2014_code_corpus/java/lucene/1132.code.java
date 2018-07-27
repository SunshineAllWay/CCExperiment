package org.apache.lucene.queryParser.core.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.BoostQueryNode;
import org.apache.lucene.queryParser.core.nodes.DeletedQueryNode;
import org.apache.lucene.queryParser.core.nodes.MatchNoDocsQueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.TokenizedPhraseQueryNode;
public class NoChildOptimizationQueryNodeProcessor extends
    QueryNodeProcessorImpl {
  public NoChildOptimizationQueryNodeProcessor() {
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof BooleanQueryNode || node instanceof BoostQueryNode
        || node instanceof TokenizedPhraseQueryNode
        || node instanceof ModifierQueryNode) {
      List<QueryNode> children = node.getChildren();
      if (children != null && children.size() > 0) {
        for (QueryNode child : children) {
          if (!(child instanceof DeletedQueryNode)) {
            return node;
          }
        }
      }
      return new MatchNoDocsQueryNode();
    }
    return node;
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    return children;
  }
}
