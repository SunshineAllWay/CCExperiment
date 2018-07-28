package org.apache.lucene.queryParser.standard.builders;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
public class PhraseQueryNodeBuilder implements StandardQueryBuilder {
  public PhraseQueryNodeBuilder() {
  }
  public PhraseQuery build(QueryNode queryNode) throws QueryNodeException {
    TokenizedPhraseQueryNode phraseNode = (TokenizedPhraseQueryNode) queryNode;
    PhraseQuery phraseQuery = new PhraseQuery();
    List<QueryNode> children = phraseNode.getChildren();
    if (children != null) {
      for (QueryNode child : children) {
        TermQuery termQuery = (TermQuery) child
            .getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
        FieldQueryNode termNode = (FieldQueryNode) child;
        phraseQuery.add(termQuery.getTerm(), termNode.getPositionIncrement());
      }
    }
    return phraseQuery;
  }
}
