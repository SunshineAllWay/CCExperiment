package org.apache.solr.spelling;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import java.util.Collection;
import java.util.HashSet;
import java.io.StringReader;
import java.io.IOException;
class SimpleQueryConverter extends SpellingQueryConverter{
  @Override
  public Collection<Token> convert(String origQuery) {
    Collection<Token> result = new HashSet<Token>();
    WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
    TokenStream ts = analyzer.tokenStream("", new StringReader(origQuery));
    TermAttribute termAtt = (TermAttribute) ts.addAttribute(TermAttribute.class);
    OffsetAttribute offsetAtt = (OffsetAttribute) ts.addAttribute(OffsetAttribute.class);
    TypeAttribute typeAtt = (TypeAttribute) ts.addAttribute(TypeAttribute.class);
    FlagsAttribute flagsAtt = (FlagsAttribute) ts.addAttribute(FlagsAttribute.class);
    PayloadAttribute payloadAtt = (PayloadAttribute) ts.addAttribute(PayloadAttribute.class);
    PositionIncrementAttribute posIncAtt = (PositionIncrementAttribute) ts.addAttribute(PositionIncrementAttribute.class);
    try {
      ts.reset();
      while (ts.incrementToken()){
        Token tok = new Token();
        tok.setTermBuffer(termAtt.termBuffer(), 0, termAtt.termLength());
        tok.setOffset(offsetAtt.startOffset(), offsetAtt.endOffset());
        tok.setFlags(flagsAtt.getFlags());
        tok.setPayload(payloadAtt.getPayload());
        tok.setPositionIncrement(posIncAtt.getPositionIncrement());
        tok.setType(typeAtt.type());
        result.add(tok);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }
}
