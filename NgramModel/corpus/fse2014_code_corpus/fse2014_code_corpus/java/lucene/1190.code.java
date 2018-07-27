package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.AnalyzerQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class PositionIncrementsAttributeImpl extends AttributeImpl
				implements PositionIncrementsAttribute {
  private static final long serialVersionUID = -2804763012793049527L;
  private boolean positionIncrementsEnabled = false;
  public PositionIncrementsAttributeImpl() {
	  positionIncrementsEnabled = false; 
  }
  public void setPositionIncrementsEnabled(boolean positionIncrementsEnabled) {
    this.positionIncrementsEnabled = positionIncrementsEnabled;
  }
  public boolean isPositionIncrementsEnabled() {
    return this.positionIncrementsEnabled;
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
    if (other instanceof PositionIncrementsAttributeImpl
        && ((PositionIncrementsAttributeImpl) other).positionIncrementsEnabled == this.positionIncrementsEnabled) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return this.positionIncrementsEnabled ? -1 : Integer.MAX_VALUE;
  }
  @Override
  public String toString() {
    return "<positionIncrements positionIncrementsEnabled="
        + this.positionIncrementsEnabled + "/>";
  }
}
