package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.nodes.MatchNoDocsQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.parser.EscapeQuerySyntaxImpl;
import org.apache.lucene.search.BooleanQuery;
public class MatchNoDocsQueryNodeBuilder implements StandardQueryBuilder {
  public MatchNoDocsQueryNodeBuilder() {
  }
  public BooleanQuery build(QueryNode queryNode) throws QueryNodeException {
    if (!(queryNode instanceof MatchNoDocsQueryNode)) {
      throw new QueryNodeException(new MessageImpl(
          QueryParserMessages.LUCENE_QUERY_CONVERSION_ERROR, queryNode
              .toQueryString(new EscapeQuerySyntaxImpl()), queryNode.getClass()
              .getName()));
    }
    return new BooleanQuery();
  }
}
