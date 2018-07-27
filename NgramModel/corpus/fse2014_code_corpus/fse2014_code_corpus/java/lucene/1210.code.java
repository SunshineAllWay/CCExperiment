package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode.Modifier;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.nodes.BooleanModifierNode;
public class BooleanSingleChildOptimizationQueryNodeProcessor extends
    QueryNodeProcessorImpl {
  public BooleanSingleChildOptimizationQueryNodeProcessor() {
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof BooleanQueryNode) {
      List<QueryNode> children = node.getChildren();
      if (children != null && children.size() == 1) {
        QueryNode child = children.get(0);
        if (child instanceof ModifierQueryNode) {
          ModifierQueryNode modNode = (ModifierQueryNode) child;
          if (modNode instanceof BooleanModifierNode
              || modNode.getModifier() == Modifier.MOD_NONE) {
            return child;
          }
        } else {
          return child;
        }
      }
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
