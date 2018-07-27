package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.MultiTermQuery.RewriteMethod;
import org.apache.lucene.util.AttributeImpl;
public class MultiTermRewriteMethodAttributeImpl extends AttributeImpl
    implements MultiTermRewriteMethodAttribute {
  private static final long serialVersionUID = -2104763012723049527L;
  private MultiTermQuery.RewriteMethod multiTermRewriteMethod = MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
  public MultiTermRewriteMethodAttributeImpl() {
  }
  public void setMultiTermRewriteMethod(MultiTermQuery.RewriteMethod method) {
    multiTermRewriteMethod = method;
  }
  public MultiTermQuery.RewriteMethod getMultiTermRewriteMethod() {
    return multiTermRewriteMethod;
  }
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
  @Override
  public void copyTo(AttributeImpl target) {
    throw new UnsupportedOperationException();
  }
  @Override
  public boolean equals(Object other) {
    if (other instanceof MultiTermRewriteMethodAttributeImpl
        && ((MultiTermRewriteMethodAttributeImpl) other).multiTermRewriteMethod == this.multiTermRewriteMethod) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return multiTermRewriteMethod.hashCode();
  }
  @Override
  public String toString() {
    return "<multiTermRewriteMethod multiTermRewriteMethod="
        + this.multiTermRewriteMethod + "/>";
  }
}
