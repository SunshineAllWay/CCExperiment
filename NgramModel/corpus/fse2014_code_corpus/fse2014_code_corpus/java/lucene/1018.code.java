package org.apache.lucene.collation;
import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Analyzer;
import java.util.Locale;
public class TestICUCollationKeyAnalyzer extends CollationTestBase {
  private Collator collator = Collator.getInstance(new Locale("fa"));
  private Analyzer analyzer = new ICUCollationKeyAnalyzer(collator);
  private String firstRangeBeginning = encodeCollationKey
    (collator.getCollationKey(firstRangeBeginningOriginal).toByteArray());
  private String firstRangeEnd = encodeCollationKey
    (collator.getCollationKey(firstRangeEndOriginal).toByteArray());
  private String secondRangeBeginning = encodeCollationKey
    (collator.getCollationKey(secondRangeBeginningOriginal).toByteArray());
  private String secondRangeEnd = encodeCollationKey
    (collator.getCollationKey(secondRangeEndOriginal).toByteArray());
  public void testFarsiRangeFilterCollating() throws Exception {
    testFarsiRangeFilterCollating(analyzer, firstRangeBeginning, firstRangeEnd, 
                                  secondRangeBeginning, secondRangeEnd);
  }
  public void testFarsiRangeQueryCollating() throws Exception {
    testFarsiRangeQueryCollating(analyzer, firstRangeBeginning, firstRangeEnd, 
                                 secondRangeBeginning, secondRangeEnd);
  }
  public void testFarsiTermRangeQuery() throws Exception {
    testFarsiTermRangeQuery
      (analyzer, firstRangeBeginning, firstRangeEnd, 
       secondRangeBeginning, secondRangeEnd);
  }
  public void testCollationKeySort() throws Exception {
    Analyzer usAnalyzer = new ICUCollationKeyAnalyzer
      (Collator.getInstance(Locale.US));
    Analyzer franceAnalyzer = new ICUCollationKeyAnalyzer
      (Collator.getInstance(Locale.FRANCE));
    Analyzer swedenAnalyzer = new ICUCollationKeyAnalyzer
      (Collator.getInstance(new Locale("sv", "se")));
    Analyzer denmarkAnalyzer = new ICUCollationKeyAnalyzer
      (Collator.getInstance(new Locale("da", "dk")));
    testCollationKeySort
      (usAnalyzer, franceAnalyzer, swedenAnalyzer, denmarkAnalyzer, "BFJHD");
  }
}
