package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.solr.util.CharArrayMap;
import java.io.IOException;
public final class RemoveDuplicatesTokenFilter extends TokenFilter {
  private final TermAttribute termAttribute = (TermAttribute) addAttribute(TermAttribute.class);
  private final PositionIncrementAttribute posIncAttribute =  (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
  private final CharArrayMap<Boolean> previous = new CharArrayMap<Boolean>(8, false);
  public RemoveDuplicatesTokenFilter(TokenStream in) {
    super(in);
  }
  @Override
  public boolean incrementToken() throws IOException {
    while (input.incrementToken()) {
      final char term[] = termAttribute.termBuffer();
      final int length = termAttribute.termLength();
      final int posIncrement = posIncAttribute.getPositionIncrement();
      if (posIncrement > 0) {
        previous.clear();
      }
      boolean duplicate = (posIncrement == 0 && previous.get(term, 0, length) != null);
      char saved[] = new char[length];
      System.arraycopy(term, 0, saved, 0, length);
      previous.put(saved, Boolean.TRUE);
      if (!duplicate) {
        return true;
      }
    }
    return false;
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    previous.clear();
  }
} 
