package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.core.util.UnescapedCharSequence;
import org.apache.lucene.queryParser.standard.config.AllowLeadingWildcardAttribute;
import org.apache.lucene.queryParser.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryParser.standard.parser.EscapeQuerySyntaxImpl;
public class AllowLeadingWildcardProcessor extends QueryNodeProcessorImpl {
  public AllowLeadingWildcardProcessor() {
  }
  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    if (getQueryConfigHandler().hasAttribute(AllowLeadingWildcardAttribute.class)) {
      AllowLeadingWildcardAttribute alwAttr= getQueryConfigHandler().getAttribute(AllowLeadingWildcardAttribute.class);
      if (!alwAttr.isAllowLeadingWildcard()) {
        return super.process(queryTree);
      }
    }
    return queryTree;
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof WildcardQueryNode) {
      WildcardQueryNode wildcardNode = (WildcardQueryNode) node;
      if (wildcardNode.getText().length() > 0) {
        if (UnescapedCharSequence.wasEscaped(wildcardNode.getText(), 0))
          return node;
        switch (wildcardNode.getText().charAt(0)) {    
          case '*':
          case '?':
            throw new QueryNodeException(new MessageImpl(
                QueryParserMessages.LEADING_WILDCARD_NOT_ALLOWED, node
                    .toQueryString(new EscapeQuerySyntaxImpl())));    
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
