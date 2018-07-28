package org.apache.solr.analysis;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
public class TestTrimFilter extends BaseTokenTestCase {
  public void testTrim() throws Exception {
    char[] a = " a ".toCharArray();
    char[] b = "b   ".toCharArray();
    char[] ccc = "cCc".toCharArray();
    char[] whitespace = "   ".toCharArray();
    char[] empty = "".toCharArray();
    TrimFilterFactory factory = new TrimFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("updateOffsets", "false");
    factory.init(args);
    TokenStream ts = factory.create(new IterTokenStream(new Token(a, 0, a.length, 1, 5),
                    new Token(b, 0, b.length, 6, 10),
                    new Token(ccc, 0, ccc.length, 11, 15),
                    new Token(whitespace, 0, whitespace.length, 16, 20),
                    new Token(empty, 0, empty.length, 21, 21)));
    assertTokenStreamContents(ts, new String[] { "a", "b", "cCc", "", ""});
    a = " a".toCharArray();
    b = "b ".toCharArray();
    ccc = " c ".toCharArray();
    whitespace = "   ".toCharArray();
    factory = new TrimFilterFactory();
    args = new HashMap<String,String>();
    args.put("updateOffsets", "true");
    factory.init(args);
    ts = factory.create(new IterTokenStream(
            new Token(a, 0, a.length, 0, 2),
            new Token(b, 0, b.length, 0, 2),
            new Token(ccc, 0, ccc.length, 0, 3),
            new Token(whitespace, 0, whitespace.length, 0, 3)));
    assertTokenStreamContents(ts, 
        new String[] { "a", "b", "c", "" },
        new int[] { 1, 0, 1, 3 },
        new int[] { 2, 1, 2, 3 },
        new int[] { 1, 1, 1, 1 });
  }
  private static class IterTokenStream extends TokenStream {
    final Token tokens[];
    int index = 0;
    TermAttribute termAtt = (TermAttribute) addAttribute(TermAttribute.class);
    OffsetAttribute offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
    PositionIncrementAttribute posIncAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
    FlagsAttribute flagsAtt = (FlagsAttribute) addAttribute(FlagsAttribute.class);
    TypeAttribute typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
    PayloadAttribute payloadAtt = (PayloadAttribute) addAttribute(PayloadAttribute.class);
    public IterTokenStream(Token... tokens) {
      super();
      this.tokens = tokens;
    }
    public IterTokenStream(Collection<Token> tokens) {
      this(tokens.toArray(new Token[tokens.size()]));
    }
    public boolean incrementToken() throws IOException {
      if (index >= tokens.length)
        return false;
      else {
        clearAttributes();
        Token token = tokens[index++];
        termAtt.setTermBuffer(token.term());
        offsetAtt.setOffset(token.startOffset(), token.endOffset());
        posIncAtt.setPositionIncrement(token.getPositionIncrement());
        flagsAtt.setFlags(token.getFlags());
        typeAtt.setType(token.type());
        payloadAtt.setPayload(token.getPayload());
        return true;
      }
    }
  }
}
