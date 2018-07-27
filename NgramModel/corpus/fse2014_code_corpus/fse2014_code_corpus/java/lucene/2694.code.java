package org.apache.solr.analysis;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import java.io.IOException;
import java.io.StringReader;
public class TestBufferedTokenStream extends BaseTokenTestCase {
  public static class AB_Q_Stream extends BufferedTokenStream {
    public AB_Q_Stream(TokenStream input) {super(input);}
    protected Token process(Token t) throws IOException {
      if ("A".equals(new String(t.termBuffer(), 0, t.termLength()))) {
        Token t2 = read();
        if (t2!=null && "B".equals(new String(t2.termBuffer(), 0, t2.termLength()))) t.setTermBuffer("Q");
        if (t2!=null) pushBack(t2);
      }
      return t;
    }
  }
  public static class AB_AAB_Stream extends BufferedTokenStream {
    public AB_AAB_Stream(TokenStream input) {super(input);}
    protected Token process(Token t) throws IOException {
      if ("A".equals(new String(t.termBuffer(), 0, t.termLength())) && 
          "B".equals(new String(peek(1).termBuffer(), 0, peek(1).termLength())))
        write((Token)t.clone());
      return t;
    }
  }
  public void testABQ() throws Exception {
    final String input = "How now A B brown A cow B like A B thing?";
    final String expected = "How now Q B brown A cow B like Q B thing?";
    TokenStream ts = new AB_Q_Stream
      (new WhitespaceTokenizer(new StringReader(input)));
    assertTokenStreamContents(ts, expected.split("\\s"));
  }
  public void testABAAB() throws Exception {
    final String input = "How now A B brown A cow B like A B thing?";
    final String expected = "How now A A B brown A cow B like A A B thing?";
    TokenStream ts = new AB_AAB_Stream
      (new WhitespaceTokenizer(new StringReader(input)));
    assertTokenStreamContents(ts, expected.split("\\s"));
  }
  public void testReset() throws Exception {
    final String input = "How now A B brown A cow B like A B thing?";
    Tokenizer tokenizer = new WhitespaceTokenizer(new StringReader(input));
    TokenStream ts = new AB_AAB_Stream(tokenizer);
    TermAttribute term = (TermAttribute) ts.addAttribute(TermAttribute.class);
    assertTrue(ts.incrementToken());
    assertEquals("How", term.term());
    assertTrue(ts.incrementToken());
    assertEquals("now", term.term());
    assertTrue(ts.incrementToken());
    assertEquals("A", term.term());
    tokenizer.reset(new StringReader(input));
    ts.reset();
    assertTrue(ts.incrementToken());
    assertEquals("How", term.term());
  }
}
