package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.MatchAllDocsQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.nodes.WildcardQueryNode;
import org.apache.lucene.search.MatchAllDocsQuery;
public class MatchAllDocsQueryNodeProcessor extends QueryNodeProcessorImpl {
  public MatchAllDocsQueryNodeProcessor() {
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof FieldQueryNode) {
      FieldQueryNode fqn = (FieldQueryNode) node;
      if (fqn.getField().toString().equals("*")
          && fqn.getText().toString().equals("*")) {
        return new MatchAllDocsQueryNode();
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
