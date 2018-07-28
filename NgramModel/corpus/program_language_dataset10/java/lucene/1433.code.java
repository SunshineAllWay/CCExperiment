package org.apache.lucene.analysis;
import java.io.IOException;
import java.util.Set;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
public final class KeywordMarkerTokenFilter extends TokenFilter {
  private final KeywordAttribute keywordAttr;
  private final TermAttribute termAtt;
  private final CharArraySet keywordSet;
  public KeywordMarkerTokenFilter(final TokenStream in,
      final CharArraySet keywordSet) {
    super(in);
    termAtt = addAttribute(TermAttribute.class);
    keywordAttr = addAttribute(KeywordAttribute.class);
    this.keywordSet = keywordSet;
  }
  public KeywordMarkerTokenFilter(final TokenStream in, final Set<?> keywordSet) {
    this(in, keywordSet instanceof CharArraySet ? (CharArraySet) keywordSet
        : CharArraySet.copy(Version.LUCENE_31, keywordSet));
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      keywordAttr.setKeyword(keywordSet.contains(termAtt.termBuffer(), 0,
          termAtt.termLength()));
      return true;
    } else
      return false;
  }
}
