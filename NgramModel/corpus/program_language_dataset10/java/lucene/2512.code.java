package org.apache.solr.search;
import org.apache.lucene.search.*;
public class Sorting {
  public static SortField getStringSortField(String fieldName, boolean reverse, boolean nullLast, boolean nullFirst) {
    if (nullLast) {
      if (!reverse) return new SortField(fieldName, nullStringLastComparatorSource);
      else return new SortField(fieldName, SortField.STRING, true);
    } else if (nullFirst) {
      if (reverse) return new SortField(fieldName, nullStringLastComparatorSource, true);
      else return new SortField(fieldName, SortField.STRING, false);
    } else {
      return new SortField(fieldName, SortField.STRING, reverse);
    }
  }
  static final FieldComparatorSource nullStringLastComparatorSource = new MissingStringLastComparatorSource(null);
}
