package org.apache.lucene.search;
import org.apache.lucene.util.PriorityQueue;
import java.text.Collator;
import java.util.Locale;
class FieldDocSortedHitQueue extends PriorityQueue<FieldDoc> {
  volatile SortField[] fields = null;
  volatile Collator[] collators = null;
  FieldDocSortedHitQueue (int size) {
    initialize (size);
  }
  void setFields (SortField[] fields) {
    this.fields = fields;
    this.collators = hasCollators (fields);
  }
  SortField[] getFields() {
    return fields;
  }
  private Collator[] hasCollators (final SortField[] fields) {
    if (fields == null) return null;
    Collator[] ret = new Collator[fields.length];
    for (int i=0; i<fields.length; ++i) {
      Locale locale = fields[i].getLocale();
      if (locale != null)
        ret[i] = Collator.getInstance (locale);
    }
    return ret;
  }
  @SuppressWarnings("unchecked") @Override
  protected final boolean lessThan(final FieldDoc docA, final FieldDoc docB) {
    final int n = fields.length;
    int c = 0;
    for (int i=0; i<n && c==0; ++i) {
      final int type = fields[i].getType();
      if (type == SortField.STRING) {
        final String s1 = (String) docA.fields[i];
        final String s2 = (String) docB.fields[i];
        if (s1 == null) {
          c = (s2 == null) ? 0 : -1;
        } else if (s2 == null) {
          c = 1;
        } else if (fields[i].getLocale() == null) {
          c = s1.compareTo(s2);
        } else {
          c = collators[i].compare(s1, s2);
        }
      } else {
        c = docA.fields[i].compareTo(docB.fields[i]);
        if (type == SortField.SCORE) {
          c = -c;
        }
      }
      if (fields[i].getReverse()) {
        c = -c;
      }
    }
    if (c == 0)
      return docA.doc > docB.doc;
    return c > 0;
  }
}
