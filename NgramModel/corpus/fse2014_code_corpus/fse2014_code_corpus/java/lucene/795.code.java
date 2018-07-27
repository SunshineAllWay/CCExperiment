package org.apache.lucene.analysis.ru;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.util.Version;
@Deprecated
public class TestRussianLetterTokenizer extends BaseTokenStreamTestCase {
  public void testRussianLetterTokenizer() throws IOException {
    StringReader reader = new StringReader("1234567890 Вместе \ud801\udc1ctest");
    RussianLetterTokenizer tokenizer = new RussianLetterTokenizer(Version.LUCENE_CURRENT,
        reader);
    assertTokenStreamContents(tokenizer, new String[] {"1234567890", "Вместе",
        "\ud801\udc1ctest"});
  }
  public void testRussianLetterTokenizerBWCompat() throws IOException {
    StringReader reader = new StringReader("1234567890 Вместе \ud801\udc1ctest");
    RussianLetterTokenizer tokenizer = new RussianLetterTokenizer(Version.LUCENE_30,
        reader);
    assertTokenStreamContents(tokenizer, new String[] {"1234567890", "Вместе", "test"});
  }
}
