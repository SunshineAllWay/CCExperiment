package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.GroupQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.search.Query;
public class GroupQueryNodeBuilder implements StandardQueryBuilder {
  public GroupQueryNodeBuilder() {
  }
  public Query build(QueryNode queryNode) throws QueryNodeException {
    GroupQueryNode groupNode = (GroupQueryNode) queryNode;
    return (Query) (groupNode).getChild().getTag(
        QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
  }
}
