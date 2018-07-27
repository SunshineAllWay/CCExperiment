package org.apache.lucene.analysis.ngram;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class EdgeNGramTokenizerTest extends BaseTokenStreamTestCase {
  private StringReader input;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    input = new StringReader("abcde");
  }
  public void testInvalidInput() throws Exception {
    boolean gotException = false;
    try {        
      new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.FRONT, 0, 0);
    } catch (IllegalArgumentException e) {
      gotException = true;
    }
    assertTrue(gotException);
  }
  public void testInvalidInput2() throws Exception {
    boolean gotException = false;
    try {        
      new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.FRONT, 2, 1);
    } catch (IllegalArgumentException e) {
      gotException = true;
    }
    assertTrue(gotException);
  }
  public void testInvalidInput3() throws Exception {
    boolean gotException = false;
    try {        
      new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.FRONT, -1, 2);
    } catch (IllegalArgumentException e) {
      gotException = true;
    }
    assertTrue(gotException);
  }
  public void testFrontUnigram() throws Exception {
    EdgeNGramTokenizer tokenizer = new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.FRONT, 1, 1);
    assertTokenStreamContents(tokenizer, new String[]{"a"}, new int[]{0}, new int[]{1}, 5 );
  }
  public void testBackUnigram() throws Exception {
    EdgeNGramTokenizer tokenizer = new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.BACK, 1, 1);
    assertTokenStreamContents(tokenizer, new String[]{"e"}, new int[]{4}, new int[]{5}, 5 );
  }
  public void testOversizedNgrams() throws Exception {
    EdgeNGramTokenizer tokenizer = new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.FRONT, 6, 6);
    assertTokenStreamContents(tokenizer, new String[0], new int[0], new int[0], 5 );
  }
  public void testFrontRangeOfNgrams() throws Exception {
    EdgeNGramTokenizer tokenizer = new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.FRONT, 1, 3);
    assertTokenStreamContents(tokenizer, new String[]{"a","ab","abc"}, new int[]{0,0,0}, new int[]{1,2,3}, 5 );
  }
  public void testBackRangeOfNgrams() throws Exception {
    EdgeNGramTokenizer tokenizer = new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.BACK, 1, 3);
    assertTokenStreamContents(tokenizer, new String[]{"e","de","cde"}, new int[]{4,3,2}, new int[]{5,5,5}, 5 );
  }
  public void testReset() throws Exception {
    EdgeNGramTokenizer tokenizer = new EdgeNGramTokenizer(input, EdgeNGramTokenizer.Side.FRONT, 1, 3);
    assertTokenStreamContents(tokenizer, new String[]{"a","ab","abc"}, new int[]{0,0,0}, new int[]{1,2,3}, 5 );
    tokenizer.reset(new StringReader("abcde"));
    assertTokenStreamContents(tokenizer, new String[]{"a","ab","abc"}, new int[]{0,0,0}, new int[]{1,2,3}, 5 );
  }
}
