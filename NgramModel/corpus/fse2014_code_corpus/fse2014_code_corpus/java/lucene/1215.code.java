package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.core.util.UnescapedCharSequence;
import org.apache.lucene.queryParser.standard.config.LowercaseExpandedTermsAttribute;
import org.apache.lucene.queryParser.standard.nodes.WildcardQueryNode;
public class LowercaseExpandedTermsQueryNodeProcessor extends
    QueryNodeProcessorImpl {
  public LowercaseExpandedTermsQueryNodeProcessor() {
  }
  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    if (getQueryConfigHandler().hasAttribute(
        LowercaseExpandedTermsAttribute.class)) {
      if (getQueryConfigHandler().getAttribute(
          LowercaseExpandedTermsAttribute.class).isLowercaseExpandedTerms()) {
        return super.process(queryTree);
      }
    }
    return queryTree;
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof WildcardQueryNode || node instanceof FuzzyQueryNode
        || node instanceof ParametricQueryNode) {
      FieldQueryNode fieldNode = (FieldQueryNode) node;
      fieldNode.setText(UnescapedCharSequence.toLowerCase(fieldNode.getText()));
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
