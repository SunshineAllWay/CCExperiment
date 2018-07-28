package org.apache.lucene.analysis.miscellaneous;
import java.io.IOException;
import java.util.Map;
import org.apache.lucene.analysis.CharArrayMap;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
public final class StemmerOverrideFilter extends TokenFilter {
  private final CharArrayMap<String> dictionary;
  private final TermAttribute termAtt = addAttribute(TermAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
  public StemmerOverrideFilter(Version matchVersion, TokenStream input,
      Map<?,String> dictionary) {
    super(input);
    this.dictionary = dictionary instanceof CharArrayMap ? 
        (CharArrayMap<String>) dictionary : CharArrayMap.copy(matchVersion, dictionary);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAtt.isKeyword()) { 
        String stem = dictionary.get(termAtt.termBuffer(), 0, termAtt.termLength());
        if (stem != null) {
          termAtt.setTermBuffer(stem);
          keywordAtt.setKeyword(true);
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
