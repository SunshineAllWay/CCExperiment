package org.apache.lucene.queryParser.standard.config;
import java.util.Locale;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface LowercaseExpandedTermsAttribute extends Attribute {
  public void setLowercaseExpandedTerms(boolean lowercaseExpandedTerms);
  public boolean isLowercaseExpandedTerms();
}
