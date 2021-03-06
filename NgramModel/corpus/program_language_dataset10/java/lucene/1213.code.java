package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.config.FuzzyAttribute;
import org.apache.lucene.search.FuzzyQuery;
public class FuzzyQueryNodeProcessor extends QueryNodeProcessorImpl {
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof FuzzyQueryNode) {
      FuzzyQueryNode fuzzyNode = (FuzzyQueryNode) node;
      QueryConfigHandler config = getQueryConfigHandler();
      if (config != null && config.hasAttribute(FuzzyAttribute.class)) {
        FuzzyAttribute fuzzyAttr = config.getAttribute(FuzzyAttribute.class);
        fuzzyNode.setPrefixLength(fuzzyAttr.getPrefixLength());
        if (fuzzyNode.getSimilarity() < 0) {
          fuzzyNode.setSimilarity(fuzzyAttr.getFuzzyMinSimilarity());
        }
      } else if (fuzzyNode.getSimilarity() < 0) {
        throw new IllegalArgumentException("No "
            + FuzzyAttribute.class.getName() + " set in the config");
      }
    }
    return node;
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    return children;
  }
}
