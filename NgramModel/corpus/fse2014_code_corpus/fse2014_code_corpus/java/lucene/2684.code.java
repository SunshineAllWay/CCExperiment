package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public class CommonGramsFilterTest extends BaseTokenTestCase {
  private static final String[] commonWords = { "s", "a", "b", "c", "d", "the",
      "of" };
  public void testReset() throws Exception {
    final String input = "How the s a brown s cow d like A B thing?";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(input));
    CommonGramsFilter cgf = new CommonGramsFilter(wt, commonWords);
    TermAttribute term = (TermAttribute) cgf.addAttribute(TermAttribute.class);
    assertTrue(cgf.incrementToken());
    assertEquals("How", term.term());
    assertTrue(cgf.incrementToken());
    assertEquals("How_the", term.term());
    assertTrue(cgf.incrementToken());
    assertEquals("the", term.term());
    assertTrue(cgf.incrementToken());
    assertEquals("the_s", term.term());
    wt.reset(new StringReader(input));
    cgf.reset();
    assertTrue(cgf.incrementToken());
    assertEquals("How", term.term());
  }
  public void testQueryReset() throws Exception {
    final String input = "How the s a brown s cow d like A B thing?";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(input));
    CommonGramsFilter cgf = new CommonGramsFilter(wt, commonWords);
    CommonGramsQueryFilter nsf = new CommonGramsQueryFilter(cgf);
    TermAttribute term = (TermAttribute) wt.addAttribute(TermAttribute.class);
    assertTrue(nsf.incrementToken());
    assertEquals("How_the", term.term());
    assertTrue(nsf.incrementToken());
    assertEquals("the_s", term.term());
    wt.reset(new StringReader(input));
    nsf.reset();
    assertTrue(nsf.incrementToken());
    assertEquals("How_the", term.term());
  }
  public void testCommonGramsQueryFilter() throws Exception {
    Analyzer a = new Analyzer() {    
      @Override
      public TokenStream tokenStream(String field, Reader in) {
        return new CommonGramsQueryFilter(new CommonGramsFilter(
            new WhitespaceTokenizer(in), commonWords));
      } 
    };
    assertAnalyzesTo(a, "brown fox", 
        new String[] { "brown", "fox" });
    assertAnalyzesTo(a, "the fox", 
        new String[] { "the_fox" });
    assertAnalyzesTo(a, "fox of", 
        new String[] { "fox_of" });
    assertAnalyzesTo(a, "of the", 
        new String[] { "of_the" });
    assertAnalyzesTo(a, "the", 
        new String[] { "the" });
    assertAnalyzesTo(a, "foo", 
        new String[] { "foo" });
    assertAnalyzesTo(a, "n n n", 
        new String[] { "n", "n", "n" });
    assertAnalyzesTo(a, "quick brown fox", 
        new String[] { "quick", "brown", "fox" });
    assertAnalyzesTo(a, "n n s", 
        new String[] { "n", "n_s" });
    assertAnalyzesTo(a, "quick brown the", 
        new String[] { "quick", "brown_the" });
    assertAnalyzesTo(a, "n s n", 
        new String[] { "n_s", "s_n" });
    assertAnalyzesTo(a, "quick the brown", 
        new String[] { "quick_the", "the_brown" });
    assertAnalyzesTo(a, "n s s", 
        new String[] { "n_s", "s_s" });
    assertAnalyzesTo(a, "fox of the", 
        new String[] { "fox_of", "of_the" });
    assertAnalyzesTo(a, "s n n", 
        new String[] { "s_n", "n", "n" });
    assertAnalyzesTo(a, "the quick brown", 
        new String[] { "the_quick", "quick", "brown" });
    assertAnalyzesTo(a, "s n s", 
        new String[] { "s_n", "n_s" });
    assertAnalyzesTo(a, "the fox of", 
        new String[] { "the_fox", "fox_of" });
    assertAnalyzesTo(a, "s s n", 
        new String[] { "s_s", "s_n" });
    assertAnalyzesTo(a, "of the fox", 
        new String[] { "of_the", "the_fox" });
    assertAnalyzesTo(a, "s s s", 
        new String[] { "s_s", "s_s" });
    assertAnalyzesTo(a, "of the of", 
        new String[] { "of_the", "the_of" });
  }
  public void testCommonGramsFilter() throws Exception {
    Analyzer a = new Analyzer() {    
      @Override
      public TokenStream tokenStream(String field, Reader in) {
        return new CommonGramsFilter(
            new WhitespaceTokenizer(in), commonWords);
      } 
    };
    assertAnalyzesTo(a, "the", new String[] { "the" });
    assertAnalyzesTo(a, "foo", new String[] { "foo" });
    assertAnalyzesTo(a, "brown fox", 
        new String[] { "brown", "fox" }, 
        new int[] { 1, 1 });
    assertAnalyzesTo(a, "the fox", 
        new String[] { "the", "the_fox", "fox" }, 
        new int[] { 1, 0, 1 });
    assertAnalyzesTo(a, "fox of", 
        new String[] { "fox", "fox_of", "of" }, 
        new int[] { 1, 0, 1 });
    assertAnalyzesTo(a, "of the", 
        new String[] { "of", "of_the", "the" }, 
        new int[] { 1, 0, 1 });
    assertAnalyzesTo(a, "n n n", 
        new String[] { "n", "n", "n" }, 
        new int[] { 1, 1, 1 });
    assertAnalyzesTo(a, "quick brown fox", 
        new String[] { "quick", "brown", "fox" }, 
        new int[] { 1, 1, 1 });
    assertAnalyzesTo(a, "n n s", 
        new String[] { "n", "n", "n_s", "s" }, 
        new int[] { 1, 1, 0, 1 });
    assertAnalyzesTo(a, "quick brown the", 
        new String[] { "quick", "brown", "brown_the", "the" }, 
        new int[] { 1, 1, 0, 1 });
    assertAnalyzesTo(a, "n s n", 
        new String[] { "n", "n_s", "s", "s_n", "n" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "quick the fox", 
        new String[] { "quick", "quick_the", "the", "the_fox", "fox" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "n s s", 
        new String[] { "n", "n_s", "s", "s_s", "s" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "fox of the", 
        new String[] { "fox", "fox_of", "of", "of_the", "the" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "s n n", 
        new String[] { "s", "s_n", "n", "n" }, 
        new int[] { 1, 0, 1, 1 });
    assertAnalyzesTo(a, "the quick brown", 
        new String[] { "the", "the_quick", "quick", "brown" }, 
        new int[] { 1, 0, 1, 1 });
    assertAnalyzesTo(a, "s n s", 
        new String[] { "s", "s_n", "n", "n_s", "s" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "the fox of", 
        new String[] { "the", "the_fox", "fox", "fox_of", "of" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "s s n", 
        new String[] { "s", "s_s", "s", "s_n", "n" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "of the fox", 
        new String[] { "of", "of_the", "the", "the_fox", "fox" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "s s s", 
        new String[] { "s", "s_s", "s", "s_s", "s" }, 
        new int[] { 1, 0, 1, 0, 1 });
    assertAnalyzesTo(a, "of the of", 
        new String[] { "of", "of_the", "the", "the_of", "of" }, 
        new int[] { 1, 0, 1, 0, 1 });
  }
  public void testCaseSensitive() throws Exception {
    final String input = "How The s a brown s cow d like A B thing?";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(input));
    Set common = CommonGramsFilter.makeCommonSet(commonWords);
    TokenFilter cgf = new CommonGramsFilter(wt, common, false);
    assertTokenStreamContents(cgf, new String[] {"How", "The", "The_s", "s",
        "s_a", "a", "a_brown", "brown", "brown_s", "s", "s_cow", "cow",
        "cow_d", "d", "d_like", "like", "A", "B", "thing?"});
  }
  public void testLastWordisStopWord() throws Exception {
    final String input = "dog the";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(input));
    CommonGramsFilter cgf = new CommonGramsFilter(wt, commonWords);
    TokenFilter nsf = new CommonGramsQueryFilter(cgf);
    assertTokenStreamContents(nsf, new String[] { "dog_the" });
  }
  public void testFirstWordisStopWord() throws Exception {
    final String input = "the dog";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(input));
    CommonGramsFilter cgf = new CommonGramsFilter(wt, commonWords);
    TokenFilter nsf = new CommonGramsQueryFilter(cgf);
    assertTokenStreamContents(nsf, new String[] { "the_dog" });
  }
  public void testOneWordQueryStopWord() throws Exception {
    final String input = "the";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(input));
    CommonGramsFilter cgf = new CommonGramsFilter(wt, commonWords);
    TokenFilter nsf = new CommonGramsQueryFilter(cgf);
    assertTokenStreamContents(nsf, new String[] { "the" });
  }
  public void testOneWordQuery() throws Exception {
    final String input = "monster";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(input));
    CommonGramsFilter cgf = new CommonGramsFilter(wt, commonWords);
    TokenFilter nsf = new CommonGramsQueryFilter(cgf);
    assertTokenStreamContents(nsf, new String[] { "monster" });
  }
  public void TestFirstAndLastStopWord() throws Exception {
    final String input = "the of";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(input));
    CommonGramsFilter cgf = new CommonGramsFilter(wt, commonWords);
    TokenFilter nsf = new CommonGramsQueryFilter(cgf);
    assertTokenStreamContents(nsf, new String[] { "the_of" });
  }
}
