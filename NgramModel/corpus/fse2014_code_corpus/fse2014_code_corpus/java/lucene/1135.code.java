package org.apache.lucene.queryParser.core.processors;
import java.util.LinkedList;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
public class QueryNodeProcessorPipeline implements QueryNodeProcessor {
  private LinkedList<QueryNodeProcessor> processors = new LinkedList<QueryNodeProcessor>();
  private QueryConfigHandler queryConfig;
  public QueryNodeProcessorPipeline() {
  }
  public QueryNodeProcessorPipeline(QueryConfigHandler queryConfigHandler) {
    this.queryConfig = queryConfigHandler;
  }
  public QueryConfigHandler getQueryConfigHandler() {
    return this.queryConfig;
  }
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    for (QueryNodeProcessor processor : this.processors) {
      queryTree = processor.process(queryTree);
    }
    return queryTree;
  }
  public void addProcessor(QueryNodeProcessor processor) {
    this.processors.add(processor);
    processor.setQueryConfigHandler(this.queryConfig);
  }
  public void setQueryConfigHandler(QueryConfigHandler queryConfigHandler) {
    this.queryConfig = queryConfigHandler;
    for (QueryNodeProcessor processor : this.processors) {
      processor.setQueryConfigHandler(this.queryConfig);
    }
  }
}
