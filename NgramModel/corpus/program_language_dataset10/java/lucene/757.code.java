package org.apache.lucene.analysis.de;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
public class TestGermanStemFilter extends BaseTokenStreamTestCase {
  public void testStemming() throws Exception {
    Tokenizer tokenizer = new KeywordTokenizer(new StringReader(""));
    TokenFilter filter = new GermanStemFilter(new LowerCaseFilter(TEST_VERSION_CURRENT, tokenizer));
    InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("data.txt"), "iso-8859-1");
    BufferedReader breader = new BufferedReader(isr);
    while(true) {
      String line = breader.readLine();
      if (line == null)
        break;
      line = line.trim();
      if (line.startsWith("#") || line.equals(""))
        continue;    
      String[] parts = line.split(";");
      tokenizer.reset(new StringReader(parts[0]));
      filter.reset();
      assertTokenStreamContents(filter, new String[] { parts[1] });
    }
    breader.close();
    isr.close();
  }
}
