package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.tartarus.snowball.SnowballProgram;
import java.io.IOException;
import java.util.Set;
public final class KeepWordFilter extends TokenFilter {
  private final CharArraySet words;
  private final TermAttribute termAtt;
  public KeepWordFilter(TokenStream in, Set<String> words, boolean ignoreCase ) {
    super(in);
    this.words = new CharArraySet(words, ignoreCase);
    this.termAtt = (TermAttribute)addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    while (input.incrementToken()) {
      if (words.contains(termAtt.termBuffer(), 0, termAtt.termLength())) return true;
    }
    return false;
  }
}
