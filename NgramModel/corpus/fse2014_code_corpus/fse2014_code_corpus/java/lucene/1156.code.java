package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.SlopQueryNode;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
public class SlopQueryNodeBuilder implements StandardQueryBuilder {
  public SlopQueryNodeBuilder() {
  }
  public Query build(QueryNode queryNode) throws QueryNodeException {
    SlopQueryNode phraseSlopNode = (SlopQueryNode) queryNode;
    Query query = (Query) phraseSlopNode.getChild().getTag(
        QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
    if (query instanceof PhraseQuery) {
      ((PhraseQuery) query).setSlop(phraseSlopNode.getValue());
    } else {
      ((MultiPhraseQuery) query).setSlop(phraseSlopNode.getValue());
    }
    return query;
  }
}
