package org.apache.lucene.analysis.tokenattributes;
import java.io.Serializable;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.AttributeImpl;
public class PositionIncrementAttributeImpl extends AttributeImpl implements PositionIncrementAttribute, Cloneable, Serializable {
  private int positionIncrement = 1;
  public void setPositionIncrement(int positionIncrement) {
    if (positionIncrement < 0)
      throw new IllegalArgumentException
        ("Increment must be zero or greater: " + positionIncrement);
    this.positionIncrement = positionIncrement;
  }
  public int getPositionIncrement() {
    return positionIncrement;
  }
  @Override
  public void clear() {
    this.positionIncrement = 1;
  }
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof PositionIncrementAttributeImpl) {
      return positionIncrement == ((PositionIncrementAttributeImpl) other).positionIncrement;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return positionIncrement;
  }
  @Override
  public void copyTo(AttributeImpl target) {
    PositionIncrementAttribute t = (PositionIncrementAttribute) target;
    t.setPositionIncrement(positionIncrement);
  }  
}
