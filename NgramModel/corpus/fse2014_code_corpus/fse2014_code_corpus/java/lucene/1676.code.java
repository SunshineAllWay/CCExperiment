package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.OpenBitSet;
public class MultiTermQueryWrapperFilter<Q extends MultiTermQuery> extends Filter {
  protected final Q query;
  protected MultiTermQueryWrapperFilter(Q query) {
      this.query = query;
  }
  @Override
  public String toString() {
    return query.toString();
  }
  @Override
  public final boolean equals(final Object o) {
    if (o==this) return true;
    if (o==null) return false;
    if (this.getClass().equals(o.getClass())) {
      return this.query.equals( ((MultiTermQueryWrapperFilter)o).query );
    }
    return false;
  }
  @Override
  public final int hashCode() {
    return query.hashCode();
  }
  public int getTotalNumberOfTerms() {
    return query.getTotalNumberOfTerms();
  }
  public void clearTotalNumberOfTerms() {
    query.clearTotalNumberOfTerms();
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    final TermEnum enumerator = query.getEnum(reader);
    try {
      if (enumerator.term() == null)
        return DocIdSet.EMPTY_DOCIDSET;
      final OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
      final int[] docs = new int[32];
      final int[] freqs = new int[32];
      TermDocs termDocs = reader.termDocs();
      try {
        int termCount = 0;
        do {
          Term term = enumerator.term();
          if (term == null)
            break;
          termCount++;
          termDocs.seek(term);
          while (true) {
            final int count = termDocs.read(docs, freqs);
            if (count != 0) {
              for(int i=0;i<count;i++) {
                bitSet.set(docs[i]);
              }
            } else {
              break;
            }
          }
        } while (enumerator.next());
        query.incTotalNumberOfTerms(termCount);
      } finally {
        termDocs.close();
      }
      return bitSet;
    } finally {
      enumerator.close();
    }
  }
}
