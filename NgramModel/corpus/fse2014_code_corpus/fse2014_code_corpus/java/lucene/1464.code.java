package org.apache.lucene.analysis.tokenattributes;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Attribute;
public interface KeywordAttribute extends Attribute {
  public boolean isKeyword();
  public void setKeyword(boolean isKeyword);
}
