package org.apache.solr.analysis;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource; 
import java.io.IOException;
import java.util.LinkedList;
@Deprecated
public abstract class BufferedTokenStream extends TokenFilter {
  private final LinkedList<Token> inQueue = new LinkedList<Token>();
  private final LinkedList<Token> outQueue = new LinkedList<Token>();
  private final TermAttribute termAtt = (TermAttribute) addAttribute(TermAttribute.class);
  private final OffsetAttribute offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
  private final TypeAttribute typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
  private final FlagsAttribute flagsAtt = (FlagsAttribute) addAttribute(FlagsAttribute.class);
  private final PayloadAttribute payloadAtt = (PayloadAttribute) addAttribute(PayloadAttribute.class);
  private final PositionIncrementAttribute posIncAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
  public BufferedTokenStream(TokenStream input) {
    super(input);
  }
  protected abstract Token process(Token t) throws IOException;
  public final boolean incrementToken() throws IOException {
    while (true) {
      if (!outQueue.isEmpty()) return writeToken(outQueue.removeFirst());
      Token t = read();
      if (null == t) return false;
      Token out = process(t);
      if (null != out) return writeToken(out);
    }
  }
  protected Token read() throws IOException {
    if (inQueue.isEmpty()) {
      Token t = readToken();
      return t;
    }
    return inQueue.removeFirst();
  }
  protected void pushBack(Token t) {
    inQueue.addFirst(t);
  }
  protected Token peek(int n) throws IOException {
    int fillCount = n-inQueue.size();
    for (int i=0; i < fillCount; i++) {
      Token t = readToken();
      if (null==t) return null;
      inQueue.addLast(t);
    }
    return inQueue.get(n-1);
  }
  private Token readToken() throws IOException {
    if (!input.incrementToken()) {
      return null;
    } else {
      Token token = new Token();
      token.setTermBuffer(termAtt.termBuffer(), 0, termAtt.termLength());
      token.setOffset(offsetAtt.startOffset(), offsetAtt.endOffset());
      token.setType(typeAtt.type());
      token.setFlags(flagsAtt.getFlags());
      token.setPositionIncrement(posIncAtt.getPositionIncrement());
      token.setPayload(payloadAtt.getPayload());
      return token;
    }
  }
  private boolean writeToken(Token token) throws IOException {
    clearAttributes();
    termAtt.setTermBuffer(token.termBuffer(), 0, token.termLength());
    offsetAtt.setOffset(token.startOffset(), token.endOffset());
    typeAtt.setType(token.type());
    flagsAtt.setFlags(token.getFlags());
    posIncAtt.setPositionIncrement(token.getPositionIncrement());
    payloadAtt.setPayload(token.getPayload());
    return true;
  }
  protected void write(Token t) {
    outQueue.addLast(t);
  }
  protected Iterable<Token> output() {
    return outQueue;
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    inQueue.clear();
    outQueue.clear();
  }
} 
