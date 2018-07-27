package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.BoostQueryNode;
import org.apache.lucene.queryParser.core.nodes.FieldableNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.config.BoostAttribute;
public class BoostQueryNodeProcessor extends QueryNodeProcessorImpl {
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof FieldableNode && 
        (node.getParent() == null || !(node.getParent() instanceof FieldableNode))) {
      FieldableNode fieldNode = (FieldableNode) node;
      QueryConfigHandler config = getQueryConfigHandler();
      if (config != null) {
        FieldConfig fieldConfig = config.getFieldConfig(fieldNode.getField());
        if (fieldConfig != null && fieldConfig.hasAttribute(BoostAttribute.class)) {
          BoostAttribute boostAttr = fieldConfig.getAttribute(BoostAttribute.class);
          return new BoostQueryNode(node, boostAttr.getBoost());
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
