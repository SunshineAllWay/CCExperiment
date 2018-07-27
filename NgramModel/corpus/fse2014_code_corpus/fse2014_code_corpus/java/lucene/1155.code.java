package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode.CompareOperator;
import org.apache.lucene.queryParser.standard.config.MultiTermRewriteMethodAttribute;
import org.apache.lucene.queryParser.standard.nodes.RangeQueryNode;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.TermRangeQuery;
public class RangeQueryNodeBuilder implements StandardQueryBuilder {
  public RangeQueryNodeBuilder() {
  }
  public TermRangeQuery build(QueryNode queryNode) throws QueryNodeException {
    RangeQueryNode rangeNode = (RangeQueryNode) queryNode;
    ParametricQueryNode upper = rangeNode.getUpperBound();
    ParametricQueryNode lower = rangeNode.getLowerBound();
    boolean lowerInclusive = false;
    boolean upperInclusive = false;
    if (upper.getOperator() == CompareOperator.LE) {
      upperInclusive = true;
    }
    if (lower.getOperator() == CompareOperator.GE) {
      lowerInclusive = true;
    }
    String field = rangeNode.getField().toString();
    TermRangeQuery rangeQuery = new TermRangeQuery(field, lower
        .getTextAsString(), upper.getTextAsString(), lowerInclusive,
        upperInclusive, rangeNode.getCollator());
    MultiTermQuery.RewriteMethod method = (MultiTermQuery.RewriteMethod)queryNode.getTag(MultiTermRewriteMethodAttribute.TAG_ID);
    if (method != null) {
      rangeQuery.setRewriteMethod(method);
    }
    return rangeQuery;
  }
}
