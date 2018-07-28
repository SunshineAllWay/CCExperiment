package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.config.MultiTermRewriteMethodAttribute;
import org.apache.lucene.queryParser.standard.nodes.WildcardQueryNode;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.WildcardQuery;
public class WildcardQueryNodeBuilder implements StandardQueryBuilder {
  public WildcardQueryNodeBuilder() {
  }
  public WildcardQuery build(QueryNode queryNode) throws QueryNodeException {
    WildcardQueryNode wildcardNode = (WildcardQueryNode) queryNode;
    WildcardQuery q = new WildcardQuery(new Term(wildcardNode.getFieldAsString(),
                                                 wildcardNode.getTextAsString()));
    MultiTermQuery.RewriteMethod method = (MultiTermQuery.RewriteMethod)queryNode.getTag(MultiTermRewriteMethodAttribute.TAG_ID);
    if (method != null) {
      q.setRewriteMethod(method);
    }
    return q;
  }
}
