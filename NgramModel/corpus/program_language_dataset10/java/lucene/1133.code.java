package org.apache.lucene.queryParser.core.processors;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
public interface QueryNodeProcessor {
  public QueryNode process(QueryNode queryTree) throws QueryNodeException;
  public void setQueryConfigHandler(QueryConfigHandler queryConfigHandler);
  public QueryConfigHandler getQueryConfigHandler();
}
