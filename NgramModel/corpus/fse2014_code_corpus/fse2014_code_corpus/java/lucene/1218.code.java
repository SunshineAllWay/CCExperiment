package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.nodes.ParametricRangeQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.config.MultiTermRewriteMethodAttribute;
import org.apache.lucene.queryParser.standard.nodes.WildcardQueryNode;
import org.apache.lucene.search.MultiTermQuery;
public class MultiTermRewriteMethodProcessor extends QueryNodeProcessorImpl {
  @Override
  protected QueryNode postProcessNode(QueryNode node) {
    if (node instanceof WildcardQueryNode
        || node instanceof ParametricRangeQueryNode) {
      if (!getQueryConfigHandler().hasAttribute(
          MultiTermRewriteMethodAttribute.class)) {
        throw new IllegalArgumentException(
            "MultiTermRewriteMethodAttribute should be set on the QueryConfigHandler");
      }
      MultiTermQuery.RewriteMethod rewriteMethod = getQueryConfigHandler()
          .getAttribute(MultiTermRewriteMethodAttribute.class)
          .getMultiTermRewriteMethod();
      node.setTag(MultiTermRewriteMethodAttribute.TAG_ID, rewriteMethod);
    }
    return node;
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) {
    return node;
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children) {
    return children;
  }
}
