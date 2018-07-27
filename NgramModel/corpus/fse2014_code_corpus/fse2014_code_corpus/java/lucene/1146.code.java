package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.search.TermQuery;
public class FieldQueryNodeBuilder implements StandardQueryBuilder {
  public FieldQueryNodeBuilder() {
  }
  public TermQuery build(QueryNode queryNode) throws QueryNodeException {
    FieldQueryNode fieldNode = (FieldQueryNode) queryNode;
    return new TermQuery(new Term(fieldNode.getFieldAsString(), fieldNode
        .getTextAsString()));
  }
}
