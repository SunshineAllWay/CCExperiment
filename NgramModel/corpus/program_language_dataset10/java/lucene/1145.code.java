package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.BoostQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.search.Query;
public class BoostQueryNodeBuilder implements StandardQueryBuilder {
  public BoostQueryNodeBuilder() {
  }
  public Query build(QueryNode queryNode) throws QueryNodeException {
    BoostQueryNode boostNode = (BoostQueryNode) queryNode;
    QueryNode child = boostNode.getChild();
    if (child == null) {
      return null;
    }
    Query query = (Query) child
        .getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
    query.setBoost(boostNode.getValue());
    return query;
  }
}
