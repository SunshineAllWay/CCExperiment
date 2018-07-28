package org.apache.lucene.search;
import java.text.Collator;
public class TermRangeFilter extends MultiTermQueryWrapperFilter<TermRangeQuery> {
  public TermRangeFilter(String fieldName, String lowerTerm, String upperTerm,
                     boolean includeLower, boolean includeUpper) {
      super(new TermRangeQuery(fieldName, lowerTerm, upperTerm, includeLower, includeUpper));
  }
  public TermRangeFilter(String fieldName, String lowerTerm, String upperTerm,
                     boolean includeLower, boolean includeUpper,
                     Collator collator) {
      super(new TermRangeQuery(fieldName, lowerTerm, upperTerm, includeLower, includeUpper, collator));
  }
  public static TermRangeFilter Less(String fieldName, String upperTerm) {
      return new TermRangeFilter(fieldName, null, upperTerm, false, true);
  }
  public static TermRangeFilter More(String fieldName, String lowerTerm) {
      return new TermRangeFilter(fieldName, lowerTerm, null, true, false);
  }
  public String getField() { return query.getField(); }
  public String getLowerTerm() { return query.getLowerTerm(); }
  public String getUpperTerm() { return query.getUpperTerm(); }
  public boolean includesLower() { return query.includesLower(); }
  public boolean includesUpper() { return query.includesUpper(); }
  public Collator getCollator() { return query.getCollator(); }
}
