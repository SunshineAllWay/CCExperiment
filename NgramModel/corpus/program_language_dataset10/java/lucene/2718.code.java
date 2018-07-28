package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import java.io.StringReader;
import java.util.regex.Pattern;
public class TestPatternReplaceFilter extends BaseTokenTestCase {
  public void testReplaceAll() throws Exception {
    String input = "aabfooaabfooabfoob ab caaaaaaaaab";
    TokenStream ts = new PatternReplaceFilter
            (new WhitespaceTokenizer(new StringReader(input)),
                    Pattern.compile("a*b"),
                    "-", true);
    assertTokenStreamContents(ts, 
        new String[] { "-foo-foo-foo-", "-", "c-" });
  }
  public void testReplaceFirst() throws Exception {
    String input = "aabfooaabfooabfoob ab caaaaaaaaab";
    TokenStream ts = new PatternReplaceFilter
            (new WhitespaceTokenizer(new StringReader(input)),
                    Pattern.compile("a*b"),
                    "-", false);
    assertTokenStreamContents(ts, 
        new String[] { "-fooaabfooabfoob", "-", "c-" });
  }
  public void testStripFirst() throws Exception {
    String input = "aabfooaabfooabfoob ab caaaaaaaaab";
    TokenStream ts = new PatternReplaceFilter
            (new WhitespaceTokenizer(new StringReader(input)),
                    Pattern.compile("a*b"),
                    null, false);
    assertTokenStreamContents(ts,
        new String[] { "fooaabfooabfoob", "", "c" });
  }
  public void testStripAll() throws Exception {
    String input = "aabfooaabfooabfoob ab caaaaaaaaab";
    TokenStream ts = new PatternReplaceFilter
            (new WhitespaceTokenizer(new StringReader(input)),
                    Pattern.compile("a*b"),
                    null, true);
    assertTokenStreamContents(ts,
        new String[] { "foofoofoo", "", "c" });
  }
  public void testReplaceAllWithBackRef() throws Exception {
    String input = "aabfooaabfooabfoob ab caaaaaaaaab";
    TokenStream ts = new PatternReplaceFilter
            (new WhitespaceTokenizer(new StringReader(input)),
                    Pattern.compile("(a*)b"),
                    "$1\\$", true);
    assertTokenStreamContents(ts,
        new String[] { "aa$fooaa$fooa$foo$", "a$", "caaaaaaaaa$" });
  }
}
