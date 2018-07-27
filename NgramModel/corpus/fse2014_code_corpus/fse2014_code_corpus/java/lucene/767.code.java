package org.apache.lucene.analysis.hi;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestHindiNormalizer extends BaseTokenStreamTestCase {
  public void testBasics() throws IOException {
    check("अँगरेज़ी", "अंगरेजि");
    check("अँगरेजी", "अंगरेजि");
    check("अँग्रेज़ी", "अंगरेजि");
    check("अँग्रेजी", "अंगरेजि");
    check("अंगरेज़ी", "अंगरेजि");
    check("अंगरेजी", "अंगरेजि");
    check("अंग्रेज़ी", "अंगरेजि");
    check("अंग्रेजी", "अंगरेजि");
  }
  public void testDecompositions() throws IOException {
    check("क़िताब", "किताब");
    check("फ़र्ज़", "फरज");
    check("क़र्ज़", "करज");
    check("ऱऴख़ग़ड़ढ़य़", "रळखगडढय");
    check("शार्‍मा", "शारमा");
    check("शार्‌मा", "शारमा");
    check("ॅॆॉॊऍऎऑऒ\u0972", "ेेोोएएओओअ");
    check("आईऊॠॡऐऔीूॄॣैौ", "अइउऋऌएओिुृॢेो");
  }
  private void check(String input, String output) throws IOException {
    Tokenizer tokenizer = new WhitespaceTokenizer(TEST_VERSION_CURRENT, 
        new StringReader(input));
    TokenFilter tf = new HindiNormalizationFilter(tokenizer);
    assertTokenStreamContents(tf, new String[] { output });
  }
}
