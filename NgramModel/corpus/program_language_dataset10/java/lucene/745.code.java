package org.apache.lucene.analysis.ar;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestArabicNormalizationFilter extends BaseTokenStreamTestCase {
  public void testAlifMadda() throws IOException {
    check("آجن", "اجن");
  }
  public void testAlifHamzaAbove() throws IOException {
    check("أحمد", "احمد");
  }
  public void testAlifHamzaBelow() throws IOException {
    check("إعاذ", "اعاذ");
  }
  public void testAlifMaksura() throws IOException {
    check("بنى", "بني");
  }
  public void testTehMarbuta() throws IOException {
    check("فاطمة", "فاطمه");
  }
  public void testTatweel() throws IOException {
    check("روبرـــــت", "روبرت");
  }
  public void testFatha() throws IOException {
    check("مَبنا", "مبنا");
  }
  public void testKasra() throws IOException {
    check("علِي", "علي");
  }
  public void testDamma() throws IOException {
    check("بُوات", "بوات");
  }
  public void testFathatan() throws IOException {
    check("ولداً", "ولدا");
  }
  public void testKasratan() throws IOException {
    check("ولدٍ", "ولد");
  }
  public void testDammatan() throws IOException {
    check("ولدٌ", "ولد");
  }  
  public void testSukun() throws IOException {
    check("نلْسون", "نلسون");
  }
  public void testShaddah() throws IOException {
    check("هتميّ", "هتمي");
  }  
  private void check(final String input, final String expected) throws IOException {
    ArabicLetterTokenizer tokenStream = new ArabicLetterTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
    ArabicNormalizationFilter filter = new ArabicNormalizationFilter(tokenStream);
    assertTokenStreamContents(filter, new String[]{expected});
  }
}
