package org.apache.lucene.queryParser.standard.builders;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.config.MultiTermRewriteMethodAttribute;
import org.apache.lucene.queryParser.standard.nodes.PrefixWildcardQueryNode;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
public class PrefixWildcardQueryNodeBuilder implements StandardQueryBuilder {
  public PrefixWildcardQueryNodeBuilder() {
  }
  public PrefixQuery build(QueryNode queryNode) throws QueryNodeException {    
    PrefixWildcardQueryNode wildcardNode = (PrefixWildcardQueryNode) queryNode;
    String text = wildcardNode.getText().subSequence(0, wildcardNode.getText().length() - 1).toString();
    PrefixQuery q = new PrefixQuery(new Term(wildcardNode.getFieldAsString(), text));
    MultiTermQuery.RewriteMethod method = (MultiTermQuery.RewriteMethod)queryNode.getTag(MultiTermRewriteMethodAttribute.TAG_ID);
    if (method != null) {
      q.setRewriteMethod(method);
    }
    return q;
  }
}
