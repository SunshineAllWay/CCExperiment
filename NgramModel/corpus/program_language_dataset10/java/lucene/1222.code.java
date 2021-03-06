package org.apache.lucene.queryParser.standard.processors;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.processors.NoChildOptimizationQueryNodeProcessor;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorPipeline;
import org.apache.lucene.queryParser.core.processors.RemoveDeletedQueryNodesProcessor;
import org.apache.lucene.queryParser.standard.builders.StandardQueryTreeBuilder;
import org.apache.lucene.queryParser.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryParser.standard.parser.StandardSyntaxParser;
import org.apache.lucene.search.Query;
public class StandardQueryNodeProcessorPipeline extends
    QueryNodeProcessorPipeline {
  public StandardQueryNodeProcessorPipeline(QueryConfigHandler queryConfig) {
    super(queryConfig);
    addProcessor(new WildcardQueryNodeProcessor());    
    addProcessor(new MultiFieldQueryNodeProcessor());
    addProcessor(new FuzzyQueryNodeProcessor());
    addProcessor(new MatchAllDocsQueryNodeProcessor());
    addProcessor(new LowercaseExpandedTermsQueryNodeProcessor());
    addProcessor(new ParametricRangeQueryNodeProcessor());
    addProcessor(new AllowLeadingWildcardProcessor());    
    addProcessor(new AnalyzerQueryNodeProcessor());
    addProcessor(new PhraseSlopQueryNodeProcessor());
    addProcessor(new GroupQueryNodeProcessor());
    addProcessor(new NoChildOptimizationQueryNodeProcessor());
    addProcessor(new RemoveDeletedQueryNodesProcessor());
    addProcessor(new RemoveEmptyNonLeafQueryNodeProcessor());
    addProcessor(new BooleanSingleChildOptimizationQueryNodeProcessor());
    addProcessor(new DefaultPhraseSlopQueryNodeProcessor());
    addProcessor(new BoostQueryNodeProcessor());    
    addProcessor(new MultiTermRewriteMethodProcessor());
  }
}
