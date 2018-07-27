package org.apache.lucene.queryParser.standard.config;
import java.util.Locale;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class LowercaseExpandedTermsAttributeImpl extends AttributeImpl
				implements LowercaseExpandedTermsAttribute {
  private static final long serialVersionUID = -2804760312723049527L;
  private boolean lowercaseExpandedTerms = true;
  public LowercaseExpandedTermsAttributeImpl() {
    lowercaseExpandedTerms = true; 
  }
  public void setLowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
	  this.lowercaseExpandedTerms = lowercaseExpandedTerms; 
  }
  public boolean isLowercaseExpandedTerms() {
    return this.lowercaseExpandedTerms;
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
    if (other instanceof LowercaseExpandedTermsAttributeImpl
        && ((LowercaseExpandedTermsAttributeImpl) other).lowercaseExpandedTerms == this.lowercaseExpandedTerms) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return this.lowercaseExpandedTerms ? -1 : Integer.MAX_VALUE;
  }
  @Override
  public String toString() {
    return "<lowercaseExpandedTerms lowercaseExpandedTerms="
        + this.lowercaseExpandedTerms + "/>";
  }
}
