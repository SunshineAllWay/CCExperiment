package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.search.FuzzyQuery;
public class FuzzyQueryNodeBuilder implements StandardQueryBuilder {
  public FuzzyQueryNodeBuilder() {
  }
  public FuzzyQuery build(QueryNode queryNode) throws QueryNodeException {
    FuzzyQueryNode fuzzyNode = (FuzzyQueryNode) queryNode;
    return new FuzzyQuery(new Term(fuzzyNode.getFieldAsString(), fuzzyNode
        .getTextAsString()), fuzzyNode.getSimilarity(), fuzzyNode
        .getPrefixLength());
  }
}
