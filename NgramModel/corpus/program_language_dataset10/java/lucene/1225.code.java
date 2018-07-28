package org.apache.lucene.queryParser.spans;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.builders.StandardQueryBuilder;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
public class SpanOrQueryNodeBuilder implements StandardQueryBuilder {
  public SpanOrQuery build(QueryNode node) throws QueryNodeException {
    BooleanQueryNode booleanNode = (BooleanQueryNode) node;
    List<QueryNode> children = booleanNode.getChildren();
    SpanQuery[] spanQueries = new SpanQuery[children.size()];
    int i = 0;
    for (QueryNode child : children) {
      spanQueries[i++] = (SpanQuery) child
          .getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
    }
    return new SpanOrQuery(spanQueries);
  }
}
