package org.apache.lucene.analysis.fa;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.ar.ArabicLetterTokenizer;
public class TestPersianNormalizationFilter extends BaseTokenStreamTestCase {
  public void testFarsiYeh() throws IOException {
    check("های", "هاي");
  }
  public void testYehBarree() throws IOException {
    check("هاے", "هاي");
  }
  public void testKeheh() throws IOException {
    check("کشاندن", "كشاندن");
  }
  public void testHehYeh() throws IOException {
    check("كتابۀ", "كتابه");
  }
  public void testHehHamzaAbove() throws IOException {
    check("كتابهٔ", "كتابه");
  }
  public void testHehGoal() throws IOException {
    check("زادہ", "زاده");
  }
  private void check(final String input, final String expected) throws IOException {
    ArabicLetterTokenizer tokenStream = new ArabicLetterTokenizer(TEST_VERSION_CURRENT, 
        new StringReader(input));
    PersianNormalizationFilter filter = new PersianNormalizationFilter(
        tokenStream);
    assertTokenStreamContents(filter, new String[]{expected});
  }
}
