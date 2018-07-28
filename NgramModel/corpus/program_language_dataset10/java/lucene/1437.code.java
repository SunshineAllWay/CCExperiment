package org.apache.lucene.analysis;
import java.io.IOException;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.CharacterUtils;
import org.apache.lucene.util.Version;
public final class LowerCaseFilter extends TokenFilter {
  private final CharacterUtils charUtils;
  public LowerCaseFilter(Version matchVersion, TokenStream in) {
    super(in);
    termAtt = addAttribute(TermAttribute.class);
    charUtils = CharacterUtils.getInstance(matchVersion);
  }
  @Deprecated
  public LowerCaseFilter(TokenStream in) {
    this(Version.LUCENE_30, in);
  }
  private TermAttribute termAtt;
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      final char[] buffer = termAtt.termBuffer();
      final int length = termAtt.termLength();
      for (int i = 0; i < length;) {
       i += Character.toChars(
               Character.toLowerCase(
                   charUtils.codePointAt(buffer, i)), buffer, i);
      }
      return true;
    } else
      return false;
  }
}
