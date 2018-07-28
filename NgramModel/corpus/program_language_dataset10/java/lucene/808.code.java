package org.apache.lucene.analysis.tr;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestTurkishLowerCaseFilter extends BaseTokenStreamTestCase {
  public void testTurkishLowerCaseFilter() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(
        "\u0130STANBUL \u0130ZM\u0130R ISPARTA"));
    TurkishLowerCaseFilter filter = new TurkishLowerCaseFilter(stream);
    assertTokenStreamContents(filter, new String[] {"istanbul", "izmir",
        "\u0131sparta",});
  }
  public void testDecomposed() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(
        "\u0049\u0307STANBUL \u0049\u0307ZM\u0049\u0307R ISPARTA"));
    TurkishLowerCaseFilter filter = new TurkishLowerCaseFilter(stream);
    assertTokenStreamContents(filter, new String[] {"istanbul", "izmir",
        "\u0131sparta",});
  }
  public void testDecomposed2() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(
        "\u0049\u0316\u0307STANBUL \u0049\u0307ZM\u0049\u0307R I\u0316SPARTA"));
    TurkishLowerCaseFilter filter = new TurkishLowerCaseFilter(stream);
    assertTokenStreamContents(filter, new String[] {"i\u0316stanbul", "izmir",
        "\u0131\u0316sparta",});
  }
}
