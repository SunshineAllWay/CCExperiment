package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.AllowLeadingWildcardProcessor;
import org.apache.lucene.util.Attribute;
public interface AllowLeadingWildcardAttribute extends Attribute {
  public void setAllowLeadingWildcard(boolean allowLeadingWildcard);
  public boolean isAllowLeadingWildcard();
}