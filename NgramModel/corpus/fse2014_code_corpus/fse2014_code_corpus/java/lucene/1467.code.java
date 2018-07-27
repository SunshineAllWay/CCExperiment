package org.apache.lucene.analysis.tokenattributes;
import java.io.Serializable;
import org.apache.lucene.util.AttributeImpl;
public class OffsetAttributeImpl extends AttributeImpl implements OffsetAttribute, Cloneable, Serializable {
  private int startOffset;
  private int endOffset;
  public int startOffset() {
    return startOffset;
  }
  public void setOffset(int startOffset, int endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
  }
  public int endOffset() {
    return endOffset;
  }
  @Override
  public void clear() {
    startOffset = 0;
    endOffset = 0;
  }
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof OffsetAttributeImpl) {
      OffsetAttributeImpl o = (OffsetAttributeImpl) other;
      return o.startOffset == startOffset && o.endOffset == endOffset;
    }
    return false;
  }
  @Override
  public int hashCode() {
    int code = startOffset;
    code = code * 31 + endOffset;
    return code;
  } 
  @Override
  public void copyTo(AttributeImpl target) {
    OffsetAttribute t = (OffsetAttribute) target;
    t.setOffset(startOffset, endOffset);
  }  
}
