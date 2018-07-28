package org.apache.lucene.queryParser.core.builders;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
public interface QueryBuilder {
  Object build(QueryNode queryNode) throws QueryNodeException;
}
