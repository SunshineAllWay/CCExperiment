package org.apache.lucene.analysis.tokenattributes;
import org.apache.lucene.util.Attribute;
public interface PositionIncrementAttribute extends Attribute {
  public void setPositionIncrement(int positionIncrement);
  public int getPositionIncrement();
}
