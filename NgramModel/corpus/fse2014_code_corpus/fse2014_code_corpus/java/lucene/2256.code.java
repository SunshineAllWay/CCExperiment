package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;
import java.io.IOException;
import java.nio.CharBuffer;
public final class PatternReplaceFilter extends TokenFilter {
  private final Pattern p;
  private final String replacement;
  private final boolean all;
  private final TermAttribute termAtt;
  public PatternReplaceFilter(TokenStream in,
                              Pattern p,
                              String replacement,
                              boolean all) {
    super(in);
    this.p=p;
    this.replacement = (null == replacement) ? "" : replacement;
    this.all=all;
    this.termAtt = (TermAttribute)addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) return false;
    CharSequence text = CharBuffer.wrap(termAtt.termBuffer(), 0, termAtt.termLength());
    Matcher m = p.matcher(text);
    if (all) {
      termAtt.setTermBuffer(m.replaceAll(replacement));
    } else {
      termAtt.setTermBuffer(m.replaceFirst(replacement));
    }
    return true;
  }
}
