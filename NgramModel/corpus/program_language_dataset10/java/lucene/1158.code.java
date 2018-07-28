package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryBuilder;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.search.Query;
public interface StandardQueryBuilder extends QueryBuilder {
  public Query build(QueryNode queryNode) throws QueryNodeException;
}
