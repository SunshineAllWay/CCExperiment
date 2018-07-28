package org.apache.lucene.analysis.position;
import java.io.IOException;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public class PositionFilterTest extends BaseTokenStreamTestCase {
  public class TestTokenStream extends TokenStream {
    protected int index = 0;
    protected String[] testToken;
    protected TermAttribute termAtt;
    public TestTokenStream(String[] testToken) {
      super();
      this.testToken = testToken;
      termAtt = addAttribute(TermAttribute.class);
    }
    @Override
    public final boolean incrementToken() throws IOException {
      clearAttributes();
      if (index < testToken.length) {
        termAtt.setTermBuffer(testToken[index++]);
        return true;
      } else {
        return false;
      }
    }
    @Override
    public void reset() {
      index = 0;
    }
  }
  public static final String[] TEST_TOKEN = new String[]{
    "please",
    "divide",
    "this",
    "sentence",
    "into",
    "shingles",
  };
  public static final int[] TEST_TOKEN_POSITION_INCREMENTS = new int[]{
    1, 0, 0, 0, 0, 0
  };
  public static final int[] TEST_TOKEN_NON_ZERO_POSITION_INCREMENTS = new int[]{
    1, 5, 5, 5, 5, 5
  };
  public static final String[] SIX_GRAM_NO_POSITIONS_TOKENS = new String[]{
    "please",
    "please divide",
    "please divide this",
    "please divide this sentence",
    "please divide this sentence into",
    "please divide this sentence into shingles",
    "divide",
    "divide this",
    "divide this sentence",
    "divide this sentence into",
    "divide this sentence into shingles",
    "this",
    "this sentence",
    "this sentence into",
    "this sentence into shingles",
    "sentence",
    "sentence into",
    "sentence into shingles",
    "into",
    "into shingles",
    "shingles",
  };
  public static final int[] SIX_GRAM_NO_POSITIONS_INCREMENTS = new int[]{
    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
  };
  public static final String[] SIX_GRAM_NO_POSITIONS_TYPES = new String[]{
    "word", "shingle", "shingle", "shingle", "shingle", "shingle",
    "word", "shingle", "shingle", "shingle", "shingle",
    "word", "shingle", "shingle", "shingle",
    "word", "shingle", "shingle",
    "word", "shingle",
    "word"
  };
  public void testFilter() throws Exception {
    assertTokenStreamContents(new PositionFilter(new TestTokenStream(TEST_TOKEN)),
               TEST_TOKEN,
               TEST_TOKEN_POSITION_INCREMENTS);
  }
  public void testNonZeroPositionIncrement() throws Exception {
    assertTokenStreamContents(new PositionFilter(new TestTokenStream(TEST_TOKEN), 5),
               TEST_TOKEN,
               TEST_TOKEN_NON_ZERO_POSITION_INCREMENTS);
  }
  public void testReset() throws Exception {
    PositionFilter filter = new PositionFilter(new TestTokenStream(TEST_TOKEN));
    assertTokenStreamContents(filter, TEST_TOKEN, TEST_TOKEN_POSITION_INCREMENTS);
    filter.reset();
    assertTokenStreamContents(filter, TEST_TOKEN, TEST_TOKEN_POSITION_INCREMENTS);
  }
  public void test6GramFilterNoPositions() throws Exception {
    ShingleFilter filter = new ShingleFilter(new TestTokenStream(TEST_TOKEN), 6);
    assertTokenStreamContents(new PositionFilter(filter),
               SIX_GRAM_NO_POSITIONS_TOKENS,
               SIX_GRAM_NO_POSITIONS_INCREMENTS);
  }
}
