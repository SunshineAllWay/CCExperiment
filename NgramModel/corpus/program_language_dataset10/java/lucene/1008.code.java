package org.apache.lucene.search.highlight;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public class TokenGroup {
  private static final int MAX_NUM_TOKENS_PER_GROUP = 50;
  Token [] tokens=new Token[MAX_NUM_TOKENS_PER_GROUP];
  float[] scores = new float[MAX_NUM_TOKENS_PER_GROUP];
  int numTokens = 0;
  int startOffset = 0;
  int endOffset = 0;
  float tot;
  int matchStartOffset, matchEndOffset;
  private OffsetAttribute offsetAtt;
  private TermAttribute termAtt;
  public TokenGroup(TokenStream tokenStream) {
    offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
    termAtt = tokenStream.addAttribute(TermAttribute.class);
  }
  void addToken(float score) {
    if (numTokens < MAX_NUM_TOKENS_PER_GROUP) {
      int termStartOffset = offsetAtt.startOffset();
      int termEndOffset = offsetAtt.endOffset();
      if (numTokens == 0) {
        startOffset = matchStartOffset = termStartOffset;
        endOffset = matchEndOffset = termEndOffset;
        tot += score;
      } else {
        startOffset = Math.min(startOffset, termStartOffset);
        endOffset = Math.max(endOffset, termEndOffset);
        if (score > 0) {
          if (tot == 0) {
            matchStartOffset = offsetAtt.startOffset();
            matchEndOffset = offsetAtt.endOffset();
          } else {
            matchStartOffset = Math.min(matchStartOffset, termStartOffset);
            matchEndOffset = Math.max(matchEndOffset, termEndOffset);
          }
          tot += score;
        }
      }
      Token token = new Token(termStartOffset, termEndOffset);
      token.setTermBuffer(termAtt.term());
      tokens[numTokens] = token;
      scores[numTokens] = score;
      numTokens++;
    }
  }
  boolean isDistinct() {
    return offsetAtt.startOffset() >= endOffset;
  }
  void clear() {
    numTokens = 0;
    tot = 0;
  }
 public Token getToken(int index)
 {
     return tokens[index];
 }
  public float getScore(int index) {
    return scores[index];
  }
  public int getEndOffset() {
    return endOffset;
  }
  public int getNumTokens() {
    return numTokens;
  }
  public int getStartOffset() {
    return startOffset;
  }
  public float getTotalScore() {
    return tot;
  }
}
