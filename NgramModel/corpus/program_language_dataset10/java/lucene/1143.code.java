package org.apache.lucene.queryParser.standard.builders;
import java.util.List;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.nodes.AnyQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
public class AnyQueryNodeBuilder implements StandardQueryBuilder {
  public AnyQueryNodeBuilder() {
  }
  public BooleanQuery build(QueryNode queryNode) throws QueryNodeException {
    AnyQueryNode andNode = (AnyQueryNode) queryNode;
    BooleanQuery bQuery = new BooleanQuery();
    List<QueryNode> children = andNode.getChildren();
    if (children != null) {
      for (QueryNode child : children) {
        Object obj = child.getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
        if (obj != null) {
          Query query = (Query) obj;
          try {
            bQuery.add(query, BooleanClause.Occur.SHOULD);
          } catch (TooManyClauses ex) {
            throw new QueryNodeException(new MessageImpl(
            QueryParserMessages.EMPTY_MESSAGE), ex);
          }
        }
      }
    }
    bQuery.setMinimumNumberShouldMatch(andNode.getMinimumMatchingElements());
    return bQuery;
  }
}
