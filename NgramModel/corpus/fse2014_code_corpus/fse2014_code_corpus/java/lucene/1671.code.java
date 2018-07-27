package org.apache.lucene.search;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.ReaderUtil;
public class IndexSearcher extends Searcher {
  IndexReader reader;
  private boolean closeReader;
  protected IndexReader[] subReaders;
  protected int[] docStarts;
  public IndexSearcher(Directory path) throws CorruptIndexException, IOException {
    this(IndexReader.open(path, true), true);
  }
  public IndexSearcher(Directory path, boolean readOnly) throws CorruptIndexException, IOException {
    this(IndexReader.open(path, readOnly), true);
  }
  public IndexSearcher(IndexReader r) {
    this(r, false);
  }
  public IndexSearcher(IndexReader reader, IndexReader[] subReaders, int[] docStarts) {
    this.reader = reader;
    this.subReaders = subReaders;
    this.docStarts = docStarts;
    closeReader = false;
  }
  private IndexSearcher(IndexReader r, boolean closeReader) {
    reader = r;
    this.closeReader = closeReader;
    List<IndexReader> subReadersList = new ArrayList<IndexReader>();
    gatherSubReaders(subReadersList, reader);
    subReaders = subReadersList.toArray(new IndexReader[subReadersList.size()]);
    docStarts = new int[subReaders.length];
    int maxDoc = 0;
    for (int i = 0; i < subReaders.length; i++) {
      docStarts[i] = maxDoc;
      maxDoc += subReaders[i].maxDoc();
    }
  }
  protected void gatherSubReaders(List<IndexReader> allSubReaders, IndexReader r) {
    ReaderUtil.gatherSubReaders(allSubReaders, r);
  }
  public IndexReader getIndexReader() {
    return reader;
  }
  @Override
  public void close() throws IOException {
    if(closeReader)
      reader.close();
  }
  @Override
  public int docFreq(Term term) throws IOException {
    return reader.docFreq(term);
  }
  @Override
  public Document doc(int i) throws CorruptIndexException, IOException {
    return reader.document(i);
  }
  @Override
  public Document doc(int i, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
	    return reader.document(i, fieldSelector);
  }
  @Override
  public int maxDoc() throws IOException {
    return reader.maxDoc();
  }
  @Override
  public TopDocs search(Weight weight, Filter filter, int nDocs) throws IOException {
    if (nDocs <= 0) {
      throw new IllegalArgumentException("nDocs must be > 0");
    }
    nDocs = Math.min(nDocs, reader.numDocs());
    TopScoreDocCollector collector = TopScoreDocCollector.create(nDocs, !weight.scoresDocsOutOfOrder());
    search(weight, filter, collector);
    return collector.topDocs();
  }
  @Override
  public TopFieldDocs search(Weight weight, Filter filter,
      final int nDocs, Sort sort) throws IOException {
    return search(weight, filter, nDocs, sort, true);
  }
  public TopFieldDocs search(Weight weight, Filter filter, int nDocs,
                             Sort sort, boolean fillFields)
      throws IOException {
    nDocs = Math.min(nDocs, reader.numDocs());
    TopFieldCollector collector = TopFieldCollector.create(sort, nDocs,
        fillFields, fieldSortDoTrackScores, fieldSortDoMaxScore, !weight.scoresDocsOutOfOrder());
    search(weight, filter, collector);
    return (TopFieldDocs) collector.topDocs();
  }
  @Override
  public void search(Weight weight, Filter filter, Collector collector)
      throws IOException {
    if (filter == null) {
      for (int i = 0; i < subReaders.length; i++) { 
        collector.setNextReader(subReaders[i], docStarts[i]);
        Scorer scorer = weight.scorer(subReaders[i], !collector.acceptsDocsOutOfOrder(), true);
        if (scorer != null) {
          scorer.score(collector);
        }
      }
    } else {
      for (int i = 0; i < subReaders.length; i++) { 
        collector.setNextReader(subReaders[i], docStarts[i]);
        searchWithFilter(subReaders[i], weight, filter, collector);
      }
    }
  }
  private void searchWithFilter(IndexReader reader, Weight weight,
      final Filter filter, final Collector collector) throws IOException {
    assert filter != null;
    Scorer scorer = weight.scorer(reader, true, false);
    if (scorer == null) {
      return;
    }
    int docID = scorer.docID();
    assert docID == -1 || docID == DocIdSetIterator.NO_MORE_DOCS;
    DocIdSet filterDocIdSet = filter.getDocIdSet(reader);
    if (filterDocIdSet == null) {
      return;
    }
    DocIdSetIterator filterIter = filterDocIdSet.iterator();
    if (filterIter == null) {
      return;
    }
    int filterDoc = filterIter.nextDoc();
    int scorerDoc = scorer.advance(filterDoc);
    collector.setScorer(scorer);
    while (true) {
      if (scorerDoc == filterDoc) {
        if (scorerDoc == DocIdSetIterator.NO_MORE_DOCS) {
          break;
        }
        collector.collect(scorerDoc);
        filterDoc = filterIter.nextDoc();
        scorerDoc = scorer.advance(filterDoc);
      } else if (scorerDoc > filterDoc) {
        filterDoc = filterIter.advance(scorerDoc);
      } else {
        scorerDoc = scorer.advance(filterDoc);
      }
    }
  }
  @Override
  public Query rewrite(Query original) throws IOException {
    Query query = original;
    for (Query rewrittenQuery = query.rewrite(reader); rewrittenQuery != query;
         rewrittenQuery = query.rewrite(reader)) {
      query = rewrittenQuery;
    }
    return query;
  }
  @Override
  public Explanation explain(Weight weight, int doc) throws IOException {
    int n = ReaderUtil.subIndex(doc, docStarts);
    int deBasedDoc = doc - docStarts[n];
    return weight.explain(subReaders[n], deBasedDoc);
  }
  private boolean fieldSortDoTrackScores;
  private boolean fieldSortDoMaxScore;
  public void setDefaultFieldSortScoring(boolean doTrackScores, boolean doMaxScore) {
    fieldSortDoTrackScores = doTrackScores;
    fieldSortDoMaxScore = doMaxScore;
  }
}
