package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.MultiTermQuery.RewriteMethod;
import org.apache.lucene.util.Attribute;
public interface MultiTermRewriteMethodAttribute extends Attribute {
  public static final CharSequence TAG_ID = "MultiTermRewriteMethodAttribute";
  public void setMultiTermRewriteMethod(MultiTermQuery.RewriteMethod method);
  public MultiTermQuery.RewriteMethod getMultiTermRewriteMethod();
}
