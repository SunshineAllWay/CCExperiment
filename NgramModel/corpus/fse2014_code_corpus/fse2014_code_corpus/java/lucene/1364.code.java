package org.apache.lucene.wordnet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestSynonymTokenFilter extends BaseTokenStreamTestCase {
  final String testFile = "testSynonyms.txt";
  public void testSynonyms() throws Exception {
    SynonymMap map = new SynonymMap(getClass().getResourceAsStream(testFile));
    Analyzer analyzer = new SynonymWhitespaceAnalyzer(map, Integer.MAX_VALUE);
    assertAnalyzesTo(analyzer, "Lost in the woods",
        new String[] { "lost", "in", "the", "woods", "forest", "wood" },
        new int[] { 0, 5, 8, 12, 12, 12 },
        new int[] { 4, 7, 11, 17, 17, 17 },
        new int[] { 1, 1, 1, 1, 0, 0 });
  }
  public void testSynonymsSingleQuote() throws Exception {
    SynonymMap map = new SynonymMap(getClass().getResourceAsStream(testFile));
    Analyzer analyzer = new SynonymWhitespaceAnalyzer(map, Integer.MAX_VALUE);
    assertAnalyzesTo(analyzer, "king",
        new String[] { "king", "baron" });
  }
  public void testSynonymsLimitedAmount() throws Exception {
    SynonymMap map = new SynonymMap(getClass().getResourceAsStream(testFile));
    Analyzer analyzer = new SynonymWhitespaceAnalyzer(map, 1);
    assertAnalyzesTo(analyzer, "Lost in the woods",
        new String[] { "lost", "in", "the", "woods", "wood" },
        new int[] { 0, 5, 8, 12, 12 },
        new int[] { 4, 7, 11, 17, 17 },
        new int[] { 1, 1, 1, 1, 0 });
  }
  public void testReusableTokenStream() throws Exception {
    SynonymMap map = new SynonymMap(getClass().getResourceAsStream(testFile));
    Analyzer analyzer = new SynonymWhitespaceAnalyzer(map, 1);
    assertAnalyzesToReuse(analyzer, "Lost in the woods",
        new String[] { "lost", "in", "the", "woods", "wood" },
        new int[] { 0, 5, 8, 12, 12 },
        new int[] { 4, 7, 11, 17, 17 },
        new int[] { 1, 1, 1, 1, 0 });
    assertAnalyzesToReuse(analyzer, "My wolfish dog went to the forest",
        new String[] { "my", "wolfish", "ravenous", "dog", "went", "to",
          "the", "forest", "woods" },
        new int[] { 0, 3, 3, 11, 15, 20, 23, 27, 27 },
        new int[] { 2, 10, 10, 14, 19, 22, 26, 33, 33 },
        new int[] { 1, 1, 0, 1, 1, 1, 1, 1, 0 });
  }
  private class SynonymWhitespaceAnalyzer extends Analyzer {
    private SynonymMap synonyms;
    private int maxSynonyms;
    public SynonymWhitespaceAnalyzer(SynonymMap synonyms, int maxSynonyms) {
      this.synonyms = synonyms;
      this.maxSynonyms = maxSynonyms;
    }
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      TokenStream ts = new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader);
      ts = new LowerCaseFilter(TEST_VERSION_CURRENT, ts);
      ts = new SynonymTokenFilter(ts, synonyms, maxSynonyms);
      return ts;
    }
    private class SavedStreams {
      Tokenizer source;
      TokenStream result;
    }
    @Override
    public TokenStream reusableTokenStream(String fieldName, Reader reader)
        throws IOException {
      SavedStreams streams = (SavedStreams) getPreviousTokenStream();
      if (streams == null) {
        streams = new SavedStreams();
        streams.source = new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader);
        streams.result = new LowerCaseFilter(TEST_VERSION_CURRENT, streams.source);
        streams.result = new SynonymTokenFilter(streams.result, synonyms, maxSynonyms);
        setPreviousTokenStream(streams);
      } else {
        streams.source.reset(reader);
        streams.result.reset(); 
      }
      return streams.result;
    }
  }
}
