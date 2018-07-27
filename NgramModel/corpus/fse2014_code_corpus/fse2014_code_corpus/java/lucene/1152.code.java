package org.apache.lucene.queryParser.standard.builders;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.TermQuery;
public class MultiPhraseQueryNodeBuilder implements StandardQueryBuilder {
  public MultiPhraseQueryNodeBuilder() {
  }
  public MultiPhraseQuery build(QueryNode queryNode) throws QueryNodeException {
    MultiPhraseQueryNode phraseNode = (MultiPhraseQueryNode) queryNode;
    MultiPhraseQuery phraseQuery = new MultiPhraseQuery();
    List<QueryNode> children = phraseNode.getChildren();
    if (children != null) {
      TreeMap<Integer, List<Term>> positionTermMap = new TreeMap<Integer, List<Term>>();
      for (QueryNode child : children) {
        FieldQueryNode termNode = (FieldQueryNode) child;
        TermQuery termQuery = (TermQuery) termNode
            .getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
        List<Term> termList = positionTermMap.get(termNode
            .getPositionIncrement());
        if (termList == null) {
          termList = new LinkedList<Term>();
          positionTermMap.put(termNode.getPositionIncrement(), termList);
        }
        termList.add(termQuery.getTerm());
      }
      for (int positionIncrement : positionTermMap.keySet()) {
        List<Term> termList = positionTermMap.get(positionIncrement);
        phraseQuery.add(termList.toArray(new Term[termList.size()]),
            positionIncrement);
      }
    }
    return phraseQuery;
  }
}
