package org.apache.solr.analysis;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.CharReader;
import org.apache.lucene.analysis.CharStream;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestPatternReplaceCharFilter extends BaseTokenTestCase {
  public void testNothingChange() throws IOException {
    final String BLOCK = "this is test.";
    PatternReplaceCharFilterFactory factory = new PatternReplaceCharFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("pattern", "(aa)\\s+(bb)\\s+(cc)");
    args.put("replacement", "$1$2$3");
    factory.init(args);
    CharStream cs = factory.create(
          CharReader.get( new StringReader( BLOCK ) ) );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertTokenStreamContents(ts,
        new String[] { "this", "is", "test." },
        new int[] { 0, 5, 8 },
        new int[] { 4, 7, 13 });
  }
  public void testReplaceByEmpty() throws IOException {
    final String BLOCK = "aa bb cc";
    PatternReplaceCharFilterFactory factory = new PatternReplaceCharFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("pattern", "(aa)\\s+(bb)\\s+(cc)");
    factory.init(args);
    CharStream cs = factory.create(
          CharReader.get( new StringReader( BLOCK ) ) );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertFalse(ts.incrementToken());
  }
  public void test1block1matchSameLength() throws IOException {
    final String BLOCK = "aa bb cc";
    PatternReplaceCharFilterFactory factory = new PatternReplaceCharFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("pattern", "(aa)\\s+(bb)\\s+(cc)");
    args.put("replacement", "$1#$2#$3");
    factory.init(args);
    CharStream cs = factory.create(
          CharReader.get( new StringReader( BLOCK ) ) );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertTokenStreamContents(ts,
        new String[] { "aa#bb#cc" },
        new int[] { 0 },
        new int[] { 8 });
  }
  public void test1block1matchLonger() throws IOException {
    final String BLOCK = "aa bb cc dd";
    CharStream cs = new PatternReplaceCharFilter( pattern("(aa)\\s+(bb)\\s+(cc)"), "$1##$2###$3",
          CharReader.get( new StringReader( BLOCK ) ) );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertTokenStreamContents(ts,
        new String[] { "aa##bb###cc", "dd" },
        new int[] { 0, 9 },
        new int[] { 8, 11 });
  }
  public void test1block2matchLonger() throws IOException {
    final String BLOCK = " a  a";
    CharStream cs = new PatternReplaceCharFilter( pattern("a"), "aa",
          CharReader.get( new StringReader( BLOCK ) ) );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertTokenStreamContents(ts,
        new String[] { "aa", "aa" },
        new int[] { 1, 4 },
        new int[] { 2, 5 });
  }
  public void test1block1matchShorter() throws IOException {
    final String BLOCK = "aa  bb   cc dd";
    CharStream cs = new PatternReplaceCharFilter( pattern("(aa)\\s+(bb)\\s+(cc)"), "$1#$2",
          CharReader.get( new StringReader( BLOCK ) ) );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertTokenStreamContents(ts,
        new String[] { "aa#bb", "dd" },
        new int[] { 0, 12 },
        new int[] { 11, 14 });
  }
  public void test1blockMultiMatches() throws IOException {
    final String BLOCK = "  aa bb cc --- aa bb aa   bb   cc";
    CharStream cs = new PatternReplaceCharFilter( pattern("(aa)\\s+(bb)\\s+(cc)"), "$1  $2  $3",
          CharReader.get( new StringReader( BLOCK ) ) );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertTokenStreamContents(ts,
        new String[] { "aa", "bb", "cc", "---", "aa", "bb", "aa", "bb", "cc" },
        new int[] { 2, 6, 9, 11, 15, 18, 21, 25, 29 },
        new int[] { 4, 8, 10, 14, 17, 20, 23, 27, 33 });
  }
  public void test2blocksMultiMatches() throws IOException {
    final String BLOCK = "  aa bb cc --- aa bb aa. bb aa   bb cc";
    CharStream cs = new PatternReplaceCharFilter( pattern("(aa)\\s+(bb)"), "$1##$2", ".",
          CharReader.get( new StringReader( BLOCK ) ) );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertTokenStreamContents(ts,
        new String[] { "aa##bb", "cc", "---", "aa##bb", "aa.", "bb", "aa##bb", "cc" },
        new int[] { 2, 8, 11, 15, 21, 25, 28, 36 },
        new int[] { 7, 10, 14, 20, 24, 27, 35, 38 });
  }
  public void testChain() throws IOException {
    final String BLOCK = " a bb - ccc . --- bb a . ccc ccc bb";
    CharStream cs = new PatternReplaceCharFilter( pattern("a"), "aa", ".",
        CharReader.get( new StringReader( BLOCK ) ) );
    cs = new PatternReplaceCharFilter( pattern("bb"), "b", ".", cs );
    cs = new PatternReplaceCharFilter( pattern("ccc"), "c", ".", cs );
    TokenStream ts = new WhitespaceTokenizer( cs );
    assertTokenStreamContents(ts,
        new String[] { "aa", "b", "-", "c", ".", "---", "b", "aa", ".", "c", "c", "b" },
        new int[] { 1, 3, 6, 8, 12, 14, 18, 21, 23, 25, 29, 33 },
        new int[] { 2, 5, 7, 11, 13, 17, 20, 22, 24, 28, 32, 35 });
  }
  private Pattern pattern( String p ){
    return Pattern.compile( p );
  }
}
