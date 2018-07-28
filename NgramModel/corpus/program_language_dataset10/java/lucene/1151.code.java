package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.search.Query;
public class ModifierQueryNodeBuilder implements StandardQueryBuilder {
  public ModifierQueryNodeBuilder() {
  }
  public Query build(QueryNode queryNode) throws QueryNodeException {
    ModifierQueryNode modifierNode = (ModifierQueryNode) queryNode;
    return (Query) (modifierNode).getChild().getTag(
        QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
  }
}
