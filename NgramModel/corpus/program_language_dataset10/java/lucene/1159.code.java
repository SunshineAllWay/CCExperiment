package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.BoostQueryNode;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryParser.core.nodes.GroupQueryNode;
import org.apache.lucene.queryParser.core.nodes.MatchAllDocsQueryNode;
import org.apache.lucene.queryParser.core.nodes.MatchNoDocsQueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.SlopQueryNode;
import org.apache.lucene.queryParser.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryParser.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.queryParser.standard.nodes.PrefixWildcardQueryNode;
import org.apache.lucene.queryParser.standard.nodes.RangeQueryNode;
import org.apache.lucene.queryParser.standard.nodes.StandardBooleanQueryNode;
import org.apache.lucene.queryParser.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryParser.standard.processors.StandardQueryNodeProcessorPipeline;
import org.apache.lucene.search.Query;
public class StandardQueryTreeBuilder extends QueryTreeBuilder implements
    StandardQueryBuilder {
  public StandardQueryTreeBuilder() {
    setBuilder(GroupQueryNode.class, new GroupQueryNodeBuilder());
    setBuilder(FieldQueryNode.class, new FieldQueryNodeBuilder());
    setBuilder(BooleanQueryNode.class, new BooleanQueryNodeBuilder());
    setBuilder(FuzzyQueryNode.class, new FuzzyQueryNodeBuilder());
    setBuilder(BoostQueryNode.class, new BoostQueryNodeBuilder());
    setBuilder(ModifierQueryNode.class, new ModifierQueryNodeBuilder());
    setBuilder(WildcardQueryNode.class, new WildcardQueryNodeBuilder());
    setBuilder(TokenizedPhraseQueryNode.class, new PhraseQueryNodeBuilder());
    setBuilder(MatchNoDocsQueryNode.class, new MatchNoDocsQueryNodeBuilder());
    setBuilder(PrefixWildcardQueryNode.class,
        new PrefixWildcardQueryNodeBuilder());
    setBuilder(RangeQueryNode.class, new RangeQueryNodeBuilder());
    setBuilder(SlopQueryNode.class, new SlopQueryNodeBuilder());
    setBuilder(StandardBooleanQueryNode.class,
        new StandardBooleanQueryNodeBuilder());
    setBuilder(MultiPhraseQueryNode.class, new MultiPhraseQueryNodeBuilder());
    setBuilder(MatchAllDocsQueryNode.class, new MatchAllDocsQueryNodeBuilder());
  }
  @Override
  public Query build(QueryNode queryNode) throws QueryNodeException {
    return (Query) super.build(queryNode);
  }
}
