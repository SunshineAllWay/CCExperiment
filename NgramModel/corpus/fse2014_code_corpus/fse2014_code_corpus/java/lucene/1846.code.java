package org.apache.lucene.analysis;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.ZipFile;
public class TestPorterStemFilter extends BaseTokenStreamTestCase {  
  public void testPorterStemFilter() throws Exception {
    Tokenizer tokenizer = new KeywordTokenizer(new StringReader(""));
    TokenStream filter = new PorterStemFilter(tokenizer);   
    ZipFile zipFile = new ZipFile(getDataFile("porterTestData.zip"));
    InputStream voc = zipFile.getInputStream(zipFile.getEntry("voc.txt"));
    InputStream out = zipFile.getInputStream(zipFile.getEntry("output.txt"));
    BufferedReader vocReader = new BufferedReader(new InputStreamReader(
        voc, "UTF-8"));
    BufferedReader outputReader = new BufferedReader(new InputStreamReader(
        out, "UTF-8"));
    String inputWord = null;
    while ((inputWord = vocReader.readLine()) != null) {
      String expectedWord = outputReader.readLine();
      assertNotNull(expectedWord);
      tokenizer.reset(new StringReader(inputWord));
      filter.reset();
      assertTokenStreamContents(filter, new String[] { expectedWord });
    }
    vocReader.close();
    outputReader.close();
    zipFile.close();
  }
  public void testWithKeywordAttribute() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("yourselves");
    Tokenizer tokenizer = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader("yourselves yours"));
    TokenStream filter = new PorterStemFilter(new KeywordMarkerTokenFilter(tokenizer, set));   
    assertTokenStreamContents(filter, new String[] {"yourselves", "your"});
  }
}
