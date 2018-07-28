package org.apache.lucene.analysis.hi;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestHindiStemmer extends BaseTokenStreamTestCase {
  public void testMasculineNouns() throws IOException {
    check("लडका", "लडक");
    check("लडके", "लडक");
    check("लडकों", "लडक");
    check("गुरु", "गुर");
    check("गुरुओं", "गुर");
    check("दोस्त", "दोस्त");
    check("दोस्तों", "दोस्त");
  }
  public void testFeminineNouns() throws IOException {
    check("लडकी", "लडक");
    check("लडकियों", "लडक");
    check("किताब", "किताब");
    check("किताबें", "किताब");
    check("किताबों", "किताब");
    check("आध्यापीका", "आध्यापीक");
    check("आध्यापीकाएं", "आध्यापीक");
    check("आध्यापीकाओं", "आध्यापीक");
  }
  public void testVerbs() throws IOException {
    check("खाना", "खा");
    check("खाता", "खा");
    check("खाती", "खा");
    check("खा", "खा");
  }
  public void testExceptions() throws IOException {
    check("कठिनाइयां", "कठिन");
    check("कठिन", "कठिन");
  }
  private void check(String input, String output) throws IOException {
    Tokenizer tokenizer = new WhitespaceTokenizer(TEST_VERSION_CURRENT, 
        new StringReader(input));
    TokenFilter tf = new HindiStemFilter(tokenizer);
    assertTokenStreamContents(tf, new String[] { output });
  }
}
