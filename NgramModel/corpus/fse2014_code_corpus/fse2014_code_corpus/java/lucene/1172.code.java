package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.PhraseSlopQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class DefaultPhraseSlopAttributeImpl extends AttributeImpl 
				implements DefaultPhraseSlopAttribute {
  private static final long serialVersionUID = -2104763012527049527L;
  private int defaultPhraseSlop = 0;
  public DefaultPhraseSlopAttributeImpl() {
	  defaultPhraseSlop = 0; 
  }
  public void setDefaultPhraseSlop(int defaultPhraseSlop) {
    this.defaultPhraseSlop = defaultPhraseSlop;
  }
  public int getDefaultPhraseSlop() {
    return this.defaultPhraseSlop;
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
    if (other instanceof DefaultPhraseSlopAttributeImpl
        && ((DefaultPhraseSlopAttributeImpl) other).defaultPhraseSlop == this.defaultPhraseSlop) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return Integer.valueOf(this.defaultPhraseSlop).hashCode();
  }
  @Override
  public String toString() {
    return "<defaultPhraseSlop defaultPhraseSlop=" + this.defaultPhraseSlop
        + "/>";
  }
}
