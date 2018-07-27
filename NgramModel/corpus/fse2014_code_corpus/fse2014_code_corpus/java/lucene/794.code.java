package org.apache.lucene.analysis.ru;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
public class TestRussianAnalyzer extends BaseTokenStreamTestCase
{
    private InputStreamReader inWords;
    private InputStreamReader sampleUnicode;
    @Deprecated
    public void testUnicode30() throws IOException
    {
        RussianAnalyzer ra = new RussianAnalyzer(Version.LUCENE_30);
        inWords =
            new InputStreamReader(
                getClass().getResourceAsStream("testUTF8.txt"),
                "UTF-8");
        sampleUnicode =
            new InputStreamReader(
                getClass().getResourceAsStream("resUTF8.htm"),
                "UTF-8");
        TokenStream in = ra.tokenStream("all", inWords);
        RussianLetterTokenizer sample =
            new RussianLetterTokenizer(TEST_VERSION_CURRENT,
                sampleUnicode);
        TermAttribute text = in.getAttribute(TermAttribute.class);
        TermAttribute sampleText = sample.getAttribute(TermAttribute.class);
        for (;;)
        {
          if (in.incrementToken() == false)
            break;
            boolean nextSampleToken = sample.incrementToken();
            assertEquals(
                "Unicode",
                text.term(),
                nextSampleToken == false
                ? null
                : sampleText.term());
        }
        inWords.close();
        sampleUnicode.close();
    }
    public void testDigitsInRussianCharset() 
    {
        Reader reader = new StringReader("text 1000");
        RussianAnalyzer ra = new RussianAnalyzer(TEST_VERSION_CURRENT);
        TokenStream stream = ra.tokenStream("", reader);
        TermAttribute termText = stream.getAttribute(TermAttribute.class);
        try {
            assertTrue(stream.incrementToken());
            assertEquals("text", termText.term());
            assertTrue(stream.incrementToken());
            assertEquals("RussianAnalyzer's tokenizer skips numbers from input text", "1000", termText.term());
            assertFalse(stream.incrementToken());
        }
        catch (IOException e)
        {
            fail("unexpected IOException");
        }
    }
    @Deprecated
    public void testReusableTokenStream30() throws Exception {
      Analyzer a = new RussianAnalyzer(Version.LUCENE_30);
      assertAnalyzesToReuse(a, "Вместе с тем о силе электромагнитной энергии имели представление еще",
          new String[] { "вмест", "сил", "электромагнитн", "энерг", "имел", "представлен" });
      assertAnalyzesToReuse(a, "Но знание это хранилось в тайне",
          new String[] { "знан", "хран", "тайн" });
    }
    public void testReusableTokenStream() throws Exception {
      Analyzer a = new RussianAnalyzer(TEST_VERSION_CURRENT);
      assertAnalyzesToReuse(a, "Вместе с тем о силе электромагнитной энергии имели представление еще",
          new String[] { "вмест", "сил", "электромагнитн", "энерг", "имел", "представлен" });
      assertAnalyzesToReuse(a, "Но знание это хранилось в тайне",
          new String[] { "знан", "эт", "хран", "тайн" });
    }
    public void testWithStemExclusionSet() throws Exception {
      CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
      set.add("представление");
      Analyzer a = new RussianAnalyzer(TEST_VERSION_CURRENT, RussianAnalyzer.getDefaultStopSet() , set);
      assertAnalyzesToReuse(a, "Вместе с тем о силе электромагнитной энергии имели представление еще",
          new String[] { "вмест", "сил", "электромагнитн", "энерг", "имел", "представление" });
    }
}
