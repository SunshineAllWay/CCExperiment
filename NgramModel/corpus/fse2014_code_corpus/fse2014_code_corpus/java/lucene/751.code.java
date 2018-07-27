package org.apache.lucene.analysis.cn;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
@Deprecated
public class TestChineseTokenizer extends BaseTokenStreamTestCase
{
    public void testOtherLetterOffset() throws IOException
    {
        String s = "a天b";
        ChineseTokenizer tokenizer = new ChineseTokenizer(new StringReader(s));
        int correctStartOffset = 0;
        int correctEndOffset = 1;
        OffsetAttribute offsetAtt = tokenizer.getAttribute(OffsetAttribute.class);
        while (tokenizer.incrementToken()) {
          assertEquals(correctStartOffset, offsetAtt.startOffset());
          assertEquals(correctEndOffset, offsetAtt.endOffset());
          correctStartOffset++;
          correctEndOffset++;
        }
    }
    public void testReusableTokenStream() throws Exception
    {
      Analyzer a = new ChineseAnalyzer();
      assertAnalyzesToReuse(a, "中华人民共和国", 
        new String[] { "中", "华", "人", "民", "共", "和", "国" },
        new int[] { 0, 1, 2, 3, 4, 5, 6 },
        new int[] { 1, 2, 3, 4, 5, 6, 7 });
      assertAnalyzesToReuse(a, "北京市", 
        new String[] { "北", "京", "市" },
        new int[] { 0, 1, 2 },
        new int[] { 1, 2, 3 });
    }
    private class JustChineseTokenizerAnalyzer extends Analyzer {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new ChineseTokenizer(reader);
      }   
    }
    private class JustChineseFilterAnalyzer extends Analyzer {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new ChineseFilter(new WhitespaceTokenizer(Version.LUCENE_CURRENT, reader));
      }
    }
    public void testNumerics() throws Exception
    { 
      Analyzer justTokenizer = new JustChineseTokenizerAnalyzer();
      assertAnalyzesTo(justTokenizer, "中1234", new String[] { "中", "1234" });
      Analyzer a = new ChineseAnalyzer(); 
      assertAnalyzesTo(a, "中1234", new String[] { "中" });
    }
    public void testEnglish() throws Exception
    {
      Analyzer chinese = new ChineseAnalyzer();
      assertAnalyzesTo(chinese, "This is a Test. b c d",
          new String[] { "test" });
      Analyzer justTokenizer = new JustChineseTokenizerAnalyzer();
      assertAnalyzesTo(justTokenizer, "This is a Test. b c d",
          new String[] { "this", "is", "a", "test", "b", "c", "d" });
      Analyzer justFilter = new JustChineseFilterAnalyzer();
      assertAnalyzesTo(justFilter, "This is a Test. b c d", 
          new String[] { "This", "Test." });
    }
}
