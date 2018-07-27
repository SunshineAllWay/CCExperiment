package org.apache.lucene.analysis.ngram;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import java.io.StringReader;
public class EdgeNGramTokenFilterTest extends BaseTokenStreamTestCase {
  private TokenStream input;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    input = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader("abcde"));
  }
  public void testInvalidInput() throws Exception {
    boolean gotException = false;
    try {        
      new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.FRONT, 0, 0);
    } catch (IllegalArgumentException e) {
      gotException = true;
    }
    assertTrue(gotException);
  }
  public void testInvalidInput2() throws Exception {
    boolean gotException = false;
    try {        
      new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.FRONT, 2, 1);
    } catch (IllegalArgumentException e) {
      gotException = true;
    }
    assertTrue(gotException);
  }
  public void testInvalidInput3() throws Exception {
    boolean gotException = false;
    try {        
      new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.FRONT, -1, 2);
    } catch (IllegalArgumentException e) {
      gotException = true;
    }
    assertTrue(gotException);
  }
  public void testFrontUnigram() throws Exception {
    EdgeNGramTokenFilter tokenizer = new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.FRONT, 1, 1);
    assertTokenStreamContents(tokenizer, new String[]{"a"}, new int[]{0}, new int[]{1});
  }
  public void testBackUnigram() throws Exception {
    EdgeNGramTokenFilter tokenizer = new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.BACK, 1, 1);
    assertTokenStreamContents(tokenizer, new String[]{"e"}, new int[]{4}, new int[]{5});
  }
  public void testOversizedNgrams() throws Exception {
    EdgeNGramTokenFilter tokenizer = new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.FRONT, 6, 6);
    assertTokenStreamContents(tokenizer, new String[0], new int[0], new int[0]);
  }
  public void testFrontRangeOfNgrams() throws Exception {
    EdgeNGramTokenFilter tokenizer = new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.FRONT, 1, 3);
    assertTokenStreamContents(tokenizer, new String[]{"a","ab","abc"}, new int[]{0,0,0}, new int[]{1,2,3});
  }
  public void testBackRangeOfNgrams() throws Exception {
    EdgeNGramTokenFilter tokenizer = new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.BACK, 1, 3);
    assertTokenStreamContents(tokenizer, new String[]{"e","de","cde"}, new int[]{4,3,2}, new int[]{5,5,5});
  }
  public void testSmallTokenInStream() throws Exception {
    input = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader("abc de fgh"));
    EdgeNGramTokenFilter tokenizer = new EdgeNGramTokenFilter(input, EdgeNGramTokenFilter.Side.FRONT, 3, 3);
    assertTokenStreamContents(tokenizer, new String[]{"abc","fgh"}, new int[]{0,7}, new int[]{3,10});
  }
  public void testReset() throws Exception {
    WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader("abcde"));
    EdgeNGramTokenFilter filter = new EdgeNGramTokenFilter(tokenizer, EdgeNGramTokenFilter.Side.FRONT, 1, 3);
    assertTokenStreamContents(filter, new String[]{"a","ab","abc"}, new int[]{0,0,0}, new int[]{1,2,3});
    tokenizer.reset(new StringReader("abcde"));
    assertTokenStreamContents(filter, new String[]{"a","ab","abc"}, new int[]{0,0,0}, new int[]{1,2,3});
  }
}
