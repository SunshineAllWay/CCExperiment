package org.apache.lucene.analysis.ar;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
public class TestArabicStemFilter extends BaseTokenStreamTestCase {
  public void testAlPrefix() throws IOException {
    check("الحسن", "حسن");
  }    
  public void testWalPrefix() throws IOException {
    check("والحسن", "حسن");
  }    
  public void testBalPrefix() throws IOException {
    check("بالحسن", "حسن");
  }    
  public void testKalPrefix() throws IOException {
    check("كالحسن", "حسن");
  }    
  public void testFalPrefix() throws IOException {
    check("فالحسن", "حسن");
  }    
  public void testLlPrefix() throws IOException {
    check("للاخر", "اخر"); 
  }
  public void testWaPrefix() throws IOException {
    check("وحسن", "حسن");
  } 
  public void testAhSuffix() throws IOException {
    check("زوجها", "زوج");
  } 
  public void testAnSuffix() throws IOException {
    check("ساهدان", "ساهد");
  } 
  public void testAtSuffix() throws IOException {
    check("ساهدات", "ساهد");
  } 
  public void testWnSuffix() throws IOException {
    check("ساهدون", "ساهد");
  } 
  public void testYnSuffix() throws IOException {
    check("ساهدين", "ساهد");
  } 
  public void testYhSuffix() throws IOException {
    check("ساهديه", "ساهد");
  } 
  public void testYpSuffix() throws IOException {
    check("ساهدية", "ساهد");
  } 
  public void testHSuffix() throws IOException {
    check("ساهده", "ساهد");
  } 
  public void testPSuffix() throws IOException {
    check("ساهدة", "ساهد");
  }
  public void testYSuffix() throws IOException {
    check("ساهدي", "ساهد");
  }
  public void testComboPrefSuf() throws IOException {
    check("وساهدون", "ساهد");
  }
  public void testComboSuf() throws IOException {
    check("ساهدهات", "ساهد");
  }
  public void testShouldntStem() throws IOException {
    check("الو", "الو");
  }
  public void testNonArabic() throws IOException {
    check("English", "English");
  }
  public void testWithKeywordAttribute() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("ساهدهات");
    ArabicLetterTokenizer tokenStream  = new ArabicLetterTokenizer(TEST_VERSION_CURRENT, new StringReader("ساهدهات"));
    ArabicStemFilter filter = new ArabicStemFilter(new KeywordMarkerTokenFilter(tokenStream, set));
    assertTokenStreamContents(filter, new String[]{"ساهدهات"});
  }
  private void check(final String input, final String expected) throws IOException {
    ArabicLetterTokenizer tokenStream  = new ArabicLetterTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
    ArabicStemFilter filter = new ArabicStemFilter(tokenStream);
    assertTokenStreamContents(filter, new String[]{expected});
  }
}
