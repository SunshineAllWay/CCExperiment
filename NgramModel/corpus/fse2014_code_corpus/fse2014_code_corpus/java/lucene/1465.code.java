package org.apache.lucene.analysis.tokenattributes;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.AttributeImpl;
public final class KeywordAttributeImpl extends AttributeImpl implements
    KeywordAttribute {
  private boolean keyword;
  @Override
  public void clear() {
    keyword = false;
  }
  @Override
  public void copyTo(AttributeImpl target) {
    KeywordAttribute attr = (KeywordAttribute) target;
    attr.setKeyword(keyword);
  }
  @Override
  public int hashCode() {
    return keyword ? 31 : 37;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    final KeywordAttributeImpl other = (KeywordAttributeImpl) obj;
    return keyword == other.keyword;
  }
  public boolean isKeyword() {
    return keyword;
  }
  public void setKeyword(boolean isKeyword) {
    keyword = isKeyword;
  }
}
