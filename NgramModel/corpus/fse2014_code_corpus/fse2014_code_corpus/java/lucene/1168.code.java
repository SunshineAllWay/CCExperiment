package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class DateResolutionAttributeImpl extends AttributeImpl 
				implements DateResolutionAttribute {
  private static final long serialVersionUID = -6804360312723049526L;
  private DateTools.Resolution dateResolution = null;
  public DateResolutionAttributeImpl() {
	  dateResolution = null; 
  }
  public void setDateResolution(DateTools.Resolution dateResolution) {
    this.dateResolution = dateResolution;
  }
  public DateTools.Resolution getDateResolution() {
    return this.dateResolution;
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
    if (other instanceof DateResolutionAttributeImpl) {
    	DateResolutionAttributeImpl dateResAttr = (DateResolutionAttributeImpl) other;
      if (dateResAttr.getDateResolution() == getDateResolution()
          || dateResAttr.getDateResolution().equals(getDateResolution())) {
        return true;
      }
    }
    return false;
  }
  @Override
  public int hashCode() {
    return (this.dateResolution == null) ? 0 : this.dateResolution.hashCode();
  }
  @Override
  public String toString() {
    return "<dateResolutionAttribute dateResolution='" + this.dateResolution
        + "'/>";
  }
}
