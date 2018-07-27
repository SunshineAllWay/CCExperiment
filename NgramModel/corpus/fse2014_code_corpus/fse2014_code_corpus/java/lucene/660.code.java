package org.apache.lucene.analysis.fr;
import java.io.IOException;
import java.util.Set;
import java.util.Arrays;
import org.apache.lucene.analysis.standard.StandardTokenizer; 
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
public final class ElisionFilter extends TokenFilter {
  private CharArraySet articles = CharArraySet.EMPTY_SET;
  private final TermAttribute termAtt;
  private static final CharArraySet DEFAULT_ARTICLES = CharArraySet.unmodifiableSet(
      new CharArraySet(Version.LUCENE_CURRENT, Arrays.asList(
          "l", "m", "t", "qu", "n", "s", "j"), true));
  private static char[] apostrophes = {'\'', '\u2019'};
  @Deprecated
  public void setArticles(Version matchVersion, Set<?> articles) {
    this.articles = CharArraySet.unmodifiableSet(
        CharArraySet.copy(matchVersion, articles));
  }
  @Deprecated
  public void setArticles(Set<?> articles) {
    setArticles(Version.LUCENE_CURRENT, articles);
  }
  public ElisionFilter(Version matchVersion, TokenStream input) {
    this(matchVersion, input, DEFAULT_ARTICLES);
  }
  @Deprecated
  public ElisionFilter(TokenStream input) {
    this(Version.LUCENE_30, input);
  }
  @Deprecated
  public ElisionFilter(TokenStream input, Set<?> articles) {
    this(Version.LUCENE_30, input, articles);
  }
  public ElisionFilter(Version matchVersion, TokenStream input, Set<?> articles) {
    super(input);
    this.articles = CharArraySet.unmodifiableSet(
        new CharArraySet(matchVersion, articles, true));
    termAtt = addAttribute(TermAttribute.class);
  }
  @Deprecated
  public ElisionFilter(TokenStream input, String[] articles) {
    this(Version.LUCENE_CURRENT, input,
        new CharArraySet(Version.LUCENE_CURRENT,
            Arrays.asList(articles), true));
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      char[] termBuffer = termAtt.termBuffer();
      int termLength = termAtt.termLength();
      int minPoz = Integer.MAX_VALUE;
      for (int i = 0; i < apostrophes.length; i++) {
        char apos = apostrophes[i];
        for (int poz = 0; poz < termLength ; poz++) {
          if (termBuffer[poz] == apos) {
            minPoz = Math.min(poz, minPoz);
            break;
          }
        }
      }
      if (minPoz != Integer.MAX_VALUE
          && articles.contains(termAtt.termBuffer(), 0, minPoz)) {
        termAtt.setTermBuffer(termAtt.termBuffer(), minPoz + 1, termAtt.termLength() - (minPoz + 1));
      }
      return true;
    } else {
      return false;
    }
  }
}
