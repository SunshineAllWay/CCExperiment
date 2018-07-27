package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.SlopQueryNode;
import org.apache.lucene.queryParser.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.config.DefaultPhraseSlopAttribute;
import org.apache.lucene.queryParser.standard.nodes.MultiPhraseQueryNode;
public class DefaultPhraseSlopQueryNodeProcessor extends QueryNodeProcessorImpl {
  private boolean processChildren = true;
  private int defaultPhraseSlop;
  public DefaultPhraseSlopQueryNodeProcessor() {
  }
  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    QueryConfigHandler queryConfig = getQueryConfigHandler();
    if (queryConfig != null) {
      if (queryConfig.hasAttribute(DefaultPhraseSlopAttribute.class)) {
        this.defaultPhraseSlop = queryConfig.getAttribute(
            DefaultPhraseSlopAttribute.class).getDefaultPhraseSlop();
        return super.process(queryTree);
      }
    }
    return queryTree;
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof TokenizedPhraseQueryNode
        || node instanceof MultiPhraseQueryNode) {
      return new SlopQueryNode(node, this.defaultPhraseSlop);
    }
    return node;
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof SlopQueryNode) {
      this.processChildren = false;
    }
    return node;
  }
  @Override
  protected void processChildren(QueryNode queryTree) throws QueryNodeException {
    if (this.processChildren) {
      super.processChildren(queryTree);
    } else {
      this.processChildren = true;
    }
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    return children;
  }
}
