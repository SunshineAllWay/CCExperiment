package org.apache.lucene.queryParser.spans;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.builders.StandardQueryBuilder;
import org.apache.lucene.search.spans.SpanQuery;
public class SpansQueryTreeBuilder extends QueryTreeBuilder implements
    StandardQueryBuilder {
  public SpansQueryTreeBuilder() {
    setBuilder(BooleanQueryNode.class, new SpanOrQueryNodeBuilder());
    setBuilder(FieldQueryNode.class, new SpanTermQueryNodeBuilder());
  }
  @Override
  public SpanQuery build(QueryNode queryTree) throws QueryNodeException {
    return (SpanQuery) super.build(queryTree);
  }
}
