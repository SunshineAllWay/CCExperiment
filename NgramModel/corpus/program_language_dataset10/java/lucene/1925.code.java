package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.util.PriorityQueue;
final class JustCompileSearch {
  private static final String UNSUPPORTED_MSG = "unsupported: used for back-compat testing only !";
  static final class JustCompileSearcher extends Searcher {
    @Override
    protected Weight createWeight(Query query) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void close() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Document doc(int i) throws CorruptIndexException, IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int[] docFreqs(Term[] terms) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Explanation explain(Query query, int doc) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Similarity getSimilarity() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void search(Query query, Collector results) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void search(Query query, Filter filter, Collector results)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public TopDocs search(Query query, Filter filter, int n) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public TopFieldDocs search(Query query, Filter filter, int n, Sort sort)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public TopDocs search(Query query, int n) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void setSimilarity(Similarity similarity) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int docFreq(Term term) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Explanation explain(Weight weight, int doc) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int maxDoc() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Query rewrite(Query query) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void search(Weight weight, Filter filter, Collector results)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public TopDocs search(Weight weight, Filter filter, int n)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public TopFieldDocs search(Weight weight, Filter filter, int n, Sort sort)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Document doc(int n, FieldSelector fieldSelector)
        throws CorruptIndexException, IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileCollector extends Collector {
    @Override
    public void collect(int doc) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void setScorer(Scorer scorer) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileDocIdSet extends DocIdSet {
    @Override
    public DocIdSetIterator iterator() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileDocIdSetIterator extends DocIdSetIterator {
    @Override
    public int docID() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int nextDoc() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int advance(int target) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileExtendedFieldCacheLongParser implements FieldCache.LongParser {
    public long parseLong(String string) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileExtendedFieldCacheDoubleParser implements FieldCache.DoubleParser {
    public double parseDouble(String string) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileFieldComparator extends FieldComparator {
    @Override
    public int compare(int slot1, int slot2) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int compareBottom(int doc) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void copy(int slot, int doc) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void setBottom(int slot) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Comparable<?> value(int slot) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileFieldComparatorSource extends FieldComparatorSource {
    @Override
    public FieldComparator newComparator(String fieldname, int numHits,
        int sortPos, boolean reversed) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileFilter extends Filter {
    @Override
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
      return null;
    }
  }
  static final class JustCompileFilteredDocIdSet extends FilteredDocIdSet {
    public JustCompileFilteredDocIdSet(DocIdSet innerSet) {
      super(innerSet);
    }
    @Override
    protected boolean match(int docid) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileFilteredDocIdSetIterator extends FilteredDocIdSetIterator {
    public JustCompileFilteredDocIdSetIterator(DocIdSetIterator innerIter) {
      super(innerIter);
    }
    @Override
    protected boolean match(int doc) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileFilteredTermEnum extends FilteredTermEnum {
    @Override
    public float difference() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    protected boolean endEnum() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    protected boolean termCompare(Term term) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompilePhraseScorer extends PhraseScorer {
    JustCompilePhraseScorer(Weight weight, TermPositions[] tps, int[] offsets,
        Similarity similarity, byte[] norms) {
      super(weight, tps, offsets, similarity, norms);
    }
    @Override
    protected float phraseFreq() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileQuery extends Query {
    @Override
    public String toString(String field) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileScorer extends Scorer {
    protected JustCompileScorer(Similarity similarity) {
      super(similarity);
    }
    @Override
    protected boolean score(Collector collector, int max, int firstDocID)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public float score() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int docID() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int nextDoc() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int advance(int target) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileSimilarity extends Similarity {
    @Override
    public float coord(int overlap, int maxOverlap) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public float idf(int docFreq, int numDocs) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public float lengthNorm(String fieldName, int numTokens) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public float queryNorm(float sumOfSquaredWeights) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public float sloppyFreq(int distance) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public float tf(float freq) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileSpanFilter extends SpanFilter {
    @Override
    public SpanFilterResult bitSpans(IndexReader reader) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
      return null;
    }    
  }
  static final class JustCompileTopDocsCollector extends TopDocsCollector<ScoreDoc> {
    protected JustCompileTopDocsCollector(PriorityQueue<ScoreDoc> pq) {
      super(pq);
    }
    @Override
    public void collect(int doc) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void setScorer(Scorer scorer) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileWeight extends Weight {
    @Override
    public Explanation explain(IndexReader reader, int doc) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Query getQuery() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public float getValue() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public void normalize(float norm) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public float sumOfSquaredWeights() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer)
        throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
}
