package org.apache.lucene.analysis.ar;
import java.io.Reader;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
public class ArabicLetterTokenizer extends LetterTokenizer {
  public ArabicLetterTokenizer(Version matchVersion, Reader in) {
    super(matchVersion, in);
  }
  public ArabicLetterTokenizer(Version matchVersion, AttributeSource source, Reader in) {
    super(matchVersion, source, in);
  }
  public ArabicLetterTokenizer(Version matchVersion, AttributeFactory factory, Reader in) {
    super(matchVersion, factory, in);
  }
  @Deprecated
  public ArabicLetterTokenizer(Reader in) {
    super(in);
  }
  @Deprecated
  public ArabicLetterTokenizer(AttributeSource source, Reader in) {
    super(source, in);
  }
  @Deprecated
  public ArabicLetterTokenizer(AttributeFactory factory, Reader in) {
    super(factory, in);
  }
  @Override
  protected boolean isTokenChar(int c) {
    return super.isTokenChar(c) || Character.getType(c) == Character.NON_SPACING_MARK;
  }
}
