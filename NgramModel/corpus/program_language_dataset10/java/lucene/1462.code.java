package org.apache.lucene.analysis.tokenattributes;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Attribute;
public interface FlagsAttribute extends Attribute {
  public int getFlags();
  public void setFlags(int flags);  
}
