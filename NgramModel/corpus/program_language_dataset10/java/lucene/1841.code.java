package org.apache.lucene.analysis;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.junit.Test;
public class TestKeywordMarkerTokenFilter extends BaseTokenStreamTestCase {
  @Test
  public void testIncrementToken() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 5, true);
    set.add("lucenefox");
    String[] output = new String[] { "the", "quick", "brown", "LuceneFox",
        "jumps" };
    assertTokenStreamContents(new LowerCaseFilterMock(
        new KeywordMarkerTokenFilter(new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(
            "The quIck browN LuceneFox Jumps")), set)), output);
    Set<String> jdkSet = new HashSet<String>();
    jdkSet.add("LuceneFox");
    assertTokenStreamContents(new LowerCaseFilterMock(
        new KeywordMarkerTokenFilter(new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(
            "The quIck browN LuceneFox Jumps")), jdkSet)), output);
    Set<?> set2 = set;
    assertTokenStreamContents(new LowerCaseFilterMock(
        new KeywordMarkerTokenFilter(new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(
            "The quIck browN LuceneFox Jumps")), set2)), output);
  }
  public static class LowerCaseFilterMock extends TokenFilter {
    private TermAttribute termAtt;
    private KeywordAttribute keywordAttr;
    public LowerCaseFilterMock(TokenStream in) {
      super(in);
      termAtt = addAttribute(TermAttribute.class);
      keywordAttr = addAttribute(KeywordAttribute.class);
    }
    @Override
    public boolean incrementToken() throws IOException {
      if (input.incrementToken()) {
        if (!keywordAttr.isKeyword())
          termAtt.setTermBuffer(termAtt.term().toLowerCase());
        return true;
      }
      return false;
    }
  }
}
