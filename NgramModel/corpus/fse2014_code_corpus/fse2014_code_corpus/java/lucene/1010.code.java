package org.apache.lucene.search.highlight;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
public class TokenStreamFromTermPositionVector extends TokenStream {
  private final List<Token> positionedTokens = new ArrayList<Token>();
  private Iterator<Token> tokensAtCurrentPosition;
  private TermAttribute termAttribute;
  private PositionIncrementAttribute positionIncrementAttribute;
  private OffsetAttribute offsetAttribute;
  public TokenStreamFromTermPositionVector(
      final TermPositionVector termPositionVector) {
    termAttribute = addAttribute(TermAttribute.class);
    positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
    offsetAttribute = addAttribute(OffsetAttribute.class);
    final String[] terms = termPositionVector.getTerms();
    for (int i = 0; i < terms.length; i++) {
      final TermVectorOffsetInfo[] offsets = termPositionVector.getOffsets(i);
      final int[] termPositions = termPositionVector.getTermPositions(i);
      for (int j = 0; j < termPositions.length; j++) {
        Token token;
        if (offsets != null) {
          token = new Token(terms[i].toCharArray(), 0, terms[i].length(),
              offsets[j].getStartOffset(), offsets[j].getEndOffset());
        } else {
          token = new Token();
          token.setTermBuffer(terms[i]);
        }
        token.setPositionIncrement(termPositions[j]);
        this.positionedTokens.add(token);
      }
    }
    final Comparator<Token> tokenComparator = new Comparator<Token>() {
      public int compare(final Token o1, final Token o2) {
        if (o1.getPositionIncrement() < o2.getPositionIncrement()) {
          return -1;
        }
        if (o1.getPositionIncrement() > o2.getPositionIncrement()) {
          return 1;
        }
        return 0;
      }
    };
    Collections.sort(this.positionedTokens, tokenComparator);
    int lastPosition = -1;
    for (final Token token : this.positionedTokens) {
      int thisPosition = token.getPositionIncrement();
      token.setPositionIncrement(thisPosition - lastPosition);
      lastPosition = thisPosition;
    }
    this.tokensAtCurrentPosition = this.positionedTokens.iterator();
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (this.tokensAtCurrentPosition.hasNext()) {
      final Token next = this.tokensAtCurrentPosition.next();
      clearAttributes();
      termAttribute.setTermBuffer(next.term());
      positionIncrementAttribute.setPositionIncrement(next
          .getPositionIncrement());
      offsetAttribute.setOffset(next.startOffset(), next.endOffset());
      return true;
    }
    return false;
  }
  @Override
  public void reset() throws IOException {
    this.tokensAtCurrentPosition = this.positionedTokens.iterator();
  }
}
