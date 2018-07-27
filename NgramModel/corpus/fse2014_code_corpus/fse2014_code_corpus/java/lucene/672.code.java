package org.apache.lucene.analysis.in;
import java.io.Reader;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
public final class IndicTokenizer extends CharTokenizer {
  public IndicTokenizer(Version matchVersion, AttributeFactory factory, Reader input) {
    super(matchVersion, factory, input);
  }
  public IndicTokenizer(Version matchVersion, AttributeSource source, Reader input) {
    super(matchVersion, source, input);
  }
  public IndicTokenizer(Version matchVersion, Reader input) {
    super(matchVersion, input);
  }
  @Override
  protected boolean isTokenChar(int c) {
    return Character.isLetter(c)
    || Character.getType(c) == Character.NON_SPACING_MARK
    || Character.getType(c) == Character.FORMAT
    || Character.getType(c) == Character.COMBINING_SPACING_MARK;
  }
}
