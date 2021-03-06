package org.apache.lucene.collation;
import org.apache.lucene.analysis.Analyzer;
import java.text.Collator;
import java.util.Locale;
public class TestCollationKeyAnalyzer extends CollationTestBase {
  private Collator collator = Collator.getInstance(new Locale("ar"));
  private Analyzer analyzer = new CollationKeyAnalyzer(collator);
  private String firstRangeBeginning = encodeCollationKey
    (collator.getCollationKey(firstRangeBeginningOriginal).toByteArray());
  private String firstRangeEnd = encodeCollationKey
    (collator.getCollationKey(firstRangeEndOriginal).toByteArray());
  private String secondRangeBeginning = encodeCollationKey
    (collator.getCollationKey(secondRangeBeginningOriginal).toByteArray());
  private String secondRangeEnd = encodeCollationKey
    (collator.getCollationKey(secondRangeEndOriginal).toByteArray());
  public void testFarsiRangeFilterCollating() throws Exception {
    testFarsiRangeFilterCollating
      (analyzer, firstRangeBeginning, firstRangeEnd, 
       secondRangeBeginning, secondRangeEnd);
  }
  public void testFarsiRangeQueryCollating() throws Exception {
    testFarsiRangeQueryCollating
      (analyzer, firstRangeBeginning, firstRangeEnd, 
       secondRangeBeginning, secondRangeEnd);
  }
  public void testFarsiTermRangeQuery() throws Exception {
    testFarsiTermRangeQuery
      (analyzer, firstRangeBeginning, firstRangeEnd, 
       secondRangeBeginning, secondRangeEnd);
  }
  public void testCollationKeySort() throws Exception {
    Analyzer usAnalyzer 
      = new CollationKeyAnalyzer(Collator.getInstance(Locale.US));
    Analyzer franceAnalyzer 
      = new CollationKeyAnalyzer(Collator.getInstance(Locale.FRANCE));
    Analyzer swedenAnalyzer 
      = new CollationKeyAnalyzer(Collator.getInstance(new Locale("sv", "se")));
    Analyzer denmarkAnalyzer 
      = new CollationKeyAnalyzer(Collator.getInstance(new Locale("da", "dk")));
    testCollationKeySort
      (usAnalyzer, franceAnalyzer, swedenAnalyzer, denmarkAnalyzer, "BFJDH");
  }
}
