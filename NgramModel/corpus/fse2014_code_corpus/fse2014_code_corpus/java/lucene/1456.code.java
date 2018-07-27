package org.apache.lucene.analysis;
import java.io.Reader;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
public class WhitespaceTokenizer extends CharTokenizer {
  public WhitespaceTokenizer(Version matchVersion, Reader in) {
    super(matchVersion, in);
  }
  public WhitespaceTokenizer(Version matchVersion, AttributeSource source, Reader in) {
    super(matchVersion, source, in);
  }
  public WhitespaceTokenizer(Version matchVersion, AttributeFactory factory, Reader in) {
    super(matchVersion, factory, in);
  }
  @Deprecated
  public WhitespaceTokenizer(Reader in) {
    super(in);
  }
  @Deprecated
  public WhitespaceTokenizer(AttributeSource source, Reader in) {
    super(source, in);
  }
  @Deprecated
  public WhitespaceTokenizer(AttributeFactory factory, Reader in) {
    super(factory, in);
  }
  @Override
  protected boolean isTokenChar(int c) {
    return !Character.isWhitespace(c);
  }
}
