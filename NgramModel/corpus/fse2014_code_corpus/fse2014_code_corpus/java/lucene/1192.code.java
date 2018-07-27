package org.apache.lucene.queryParser.standard.config;
import java.text.Collator;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.AttributeImpl;
public class RangeCollatorAttributeImpl extends AttributeImpl
				implements RangeCollatorAttribute {
  private static final long serialVersionUID = -6804360312723049526L;
  private Collator rangeCollator;
  public RangeCollatorAttributeImpl() {
	  rangeCollator = null; 
  }
  public void setDateResolution(Collator rangeCollator) {
    this.rangeCollator = rangeCollator;
  }
  public Collator getRangeCollator() {
    return this.rangeCollator;
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
    if (other instanceof RangeCollatorAttributeImpl) {
    	RangeCollatorAttributeImpl rangeCollatorAttr = (RangeCollatorAttributeImpl) other;
      if (rangeCollatorAttr.rangeCollator == this.rangeCollator
          || rangeCollatorAttr.rangeCollator.equals(this.rangeCollator)) {
        return true;
      }
    }
    return false;
  }
  @Override
  public int hashCode() {
    return (this.rangeCollator == null) ? 0 : this.rangeCollator.hashCode();
  }
  @Override
  public String toString() {
    return "<rangeCollatorAttribute rangeCollator='" + this.rangeCollator
        + "'/>";
  }
}
