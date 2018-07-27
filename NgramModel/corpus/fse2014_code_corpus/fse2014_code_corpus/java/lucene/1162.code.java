package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.AllowLeadingWildcardProcessor;
import org.apache.lucene.util.AttributeImpl;
public class AllowLeadingWildcardAttributeImpl extends AttributeImpl 
				implements AllowLeadingWildcardAttribute {
  private static final long serialVersionUID = -2804763012723049527L;
  private boolean allowLeadingWildcard = false;  
  public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
    this.allowLeadingWildcard = allowLeadingWildcard;
  }
  public boolean isAllowLeadingWildcard() {
    return this.allowLeadingWildcard;
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
    if (other instanceof AllowLeadingWildcardAttributeImpl
        && ((AllowLeadingWildcardAttributeImpl) other).allowLeadingWildcard == this.allowLeadingWildcard) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return this.allowLeadingWildcard ? -1 : Integer.MAX_VALUE;
  }
  @Override
  public String toString() {
    return "<allowLeadingWildcard allowLeadingWildcard="
        + this.allowLeadingWildcard + "/>";
  }
}
