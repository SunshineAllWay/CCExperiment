package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.index.TermDocs;  
public class FieldCacheTermsFilter extends Filter {
  private String field;
  private String[] terms;
  public FieldCacheTermsFilter(String field, String... terms) {
    this.field = field;
    this.terms = terms;
  }
  public FieldCache getFieldCache() {
    return FieldCache.DEFAULT;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    return new FieldCacheTermsFilterDocIdSet(getFieldCache().getStringIndex(reader, field));
  }
  protected class FieldCacheTermsFilterDocIdSet extends DocIdSet {
    private FieldCache.StringIndex fcsi;
    private OpenBitSet openBitSet;
    public FieldCacheTermsFilterDocIdSet(FieldCache.StringIndex fcsi) {
      this.fcsi = fcsi;
      openBitSet = new OpenBitSet(this.fcsi.lookup.length);
      for (int i=0;i<terms.length;i++) {
        int termNumber = this.fcsi.binarySearchLookup(terms[i]);
        if (termNumber > 0) {
          openBitSet.fastSet(termNumber);
        }
      }
    }
    @Override
    public DocIdSetIterator iterator() {
      return new FieldCacheTermsFilterDocIdSetIterator();
    }
    @Override
    public boolean isCacheable() {
      return true;
    }
    protected class FieldCacheTermsFilterDocIdSetIterator extends DocIdSetIterator {
      private int doc = -1;
      @Override
      public int docID() {
        return doc;
      }
      @Override
      public int nextDoc() {
        try {
          while (!openBitSet.fastGet(fcsi.order[++doc])) {}
        } catch (ArrayIndexOutOfBoundsException e) {
          doc = NO_MORE_DOCS;
        }
        return doc;
      }
      @Override
      public int advance(int target) {
        try {
          doc = target;
          while (!openBitSet.fastGet(fcsi.order[doc])) {
            doc++;
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          doc = NO_MORE_DOCS;
        }
        return doc;
      }
    }
  }
}
