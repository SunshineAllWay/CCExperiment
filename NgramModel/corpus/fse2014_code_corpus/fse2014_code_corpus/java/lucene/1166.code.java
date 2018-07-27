package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.standard.processors.MultiFieldQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class BoostAttributeImpl extends AttributeImpl 
				implements BoostAttribute {
  private static final long serialVersionUID = -2104763012523049527L;
  private float boost = 1.0f;
  public BoostAttributeImpl() {
  }
  public void setBoost(float boost) {
    this.boost = boost;
  }
  public float getBoost() {
    return this.boost;
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
    if (other instanceof BoostAttributeImpl
        && ((BoostAttributeImpl) other).boost == this.boost) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return Float.valueOf(this.boost).hashCode();
  }
  @Override
  public String toString() {
    return "<boostAttribute boost=" + this.boost + "/>";
  }
}
