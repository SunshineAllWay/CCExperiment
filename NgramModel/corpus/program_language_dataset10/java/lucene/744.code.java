package org.apache.lucene.analysis.ar;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.util.Version;
public class TestArabicLetterTokenizer extends BaseTokenStreamTestCase {
  public void testArabicLetterTokenizer() throws IOException {
    StringReader reader = new StringReader("1234567890 Tokenizer \ud801\udc1c\u0300test");
    ArabicLetterTokenizer tokenizer = new ArabicLetterTokenizer(Version.LUCENE_31,
        reader);
    assertTokenStreamContents(tokenizer, new String[] {"Tokenizer",
        "\ud801\udc1c\u0300test"});
  }
  public void testArabicLetterTokenizerBWCompat() throws IOException {
    StringReader reader = new StringReader("1234567890 Tokenizer \ud801\udc1c\u0300test");
    ArabicLetterTokenizer tokenizer = new ArabicLetterTokenizer(Version.LUCENE_30,
        reader);
    assertTokenStreamContents(tokenizer, new String[] {"Tokenizer", "\u0300test"});
  }
}
