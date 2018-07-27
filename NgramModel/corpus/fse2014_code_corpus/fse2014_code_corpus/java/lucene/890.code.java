package org.apache.lucene.benchmark.byTask.tasks;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.QueryMaker;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
public abstract class ReadTask extends PerfTask {
  private final QueryMaker queryMaker;
  public ReadTask(PerfRunData runData) {
    super(runData);
    if (withSearch()) {
      queryMaker = getQueryMaker();
    } else {
      queryMaker = null;
    }
  }
  @Override
  public int doLogic() throws Exception {
    int res = 0;
    IndexSearcher searcher = getRunData().getIndexSearcher();
    IndexReader reader;
    final boolean closeSearcher;
    if (searcher == null) {
      Directory dir = getRunData().getDirectory();
      reader = IndexReader.open(dir, true);
      searcher = new IndexSearcher(reader);
      closeSearcher = true;
    } else {
      reader = searcher.getIndexReader();
      closeSearcher = false;
    }
    if (withWarm()) {
      Document doc = null;
      for (int m = 0; m < reader.maxDoc(); m++) {
        if (!reader.isDeleted(m)) {
          doc = reader.document(m);
          res += (doc == null ? 0 : 1);
        }
      }
    }
    if (withSearch()) {
      res++;
      Query q = queryMaker.makeQuery();
      Sort sort = getSort();
      TopDocs hits;
      final int numHits = numHits();
      if (numHits > 0) {
        if (sort != null) {
          Weight w = q.weight(searcher);
          TopFieldCollector collector = TopFieldCollector.create(sort, numHits,
                                                                 true, withScore(),
                                                                 withMaxScore(),
                                                                 !w.scoresDocsOutOfOrder());
          searcher.search(w, null, collector);
          hits = collector.topDocs();
        } else {
          hits = searcher.search(q, numHits);
        }
        final String printHitsField = getRunData().getConfig().get("print.hits.field", null);
        if (printHitsField != null && printHitsField.length() > 0) {
          if (q instanceof MultiTermQuery) {
            System.out.println("MultiTermQuery term count = " + ((MultiTermQuery) q).getTotalNumberOfTerms());
          }
          System.out.println("totalHits = " + hits.totalHits);
          System.out.println("maxDoc()  = " + reader.maxDoc());
          System.out.println("numDocs() = " + reader.numDocs());
          for(int i=0;i<hits.scoreDocs.length;i++) {
            final int docID = hits.scoreDocs[i].doc;
            final Document doc = reader.document(docID);
            System.out.println("  " + i + ": doc=" + docID + " score=" + hits.scoreDocs[i].score + " " + printHitsField + " =" + doc.get(printHitsField));
          }
        }
        if (withTraverse()) {
          final ScoreDoc[] scoreDocs = hits.scoreDocs;
          int traversalSize = Math.min(scoreDocs.length, traversalSize());
          if (traversalSize > 0) {
            boolean retrieve = withRetrieve();
            int numHighlight = Math.min(numToHighlight(), scoreDocs.length);
            Analyzer analyzer = getRunData().getAnalyzer();
            BenchmarkHighlighter highlighter = null;
            if (numHighlight > 0) {
              highlighter = getBenchmarkHighlighter(q);
            }
            for (int m = 0; m < traversalSize; m++) {
              int id = scoreDocs[m].doc;
              res++;
              if (retrieve) {
                Document document = retrieveDoc(reader, id);
                res += document != null ? 1 : 0;
                if (numHighlight > 0 && m < numHighlight) {
                  Collection<String> fieldsToHighlight = getFieldsToHighlight(document);
                  for (final String field : fieldsToHighlight) {
                    String text = document.get(field);
                    res += highlighter.doHighlight(reader, id, field, document, analyzer, text);
                  }
                }
              }
            }
          }
        }
      }
    }
    if (closeSearcher) {
      searcher.close();
      reader.close();
    } else {
      reader.decRef();
    }
    return res;
  }
  protected Document retrieveDoc(IndexReader ir, int id) throws IOException {
    return ir.document(id);
  }
  public abstract QueryMaker getQueryMaker();
  public abstract boolean withSearch();
  public abstract boolean withWarm();
  public abstract boolean withTraverse();
  public boolean withScore() {
    return true;
  }
  public boolean withMaxScore() {
    return true;
  }
  public int traversalSize() {
    return Integer.MAX_VALUE;
  }
  static final int DEFAULT_SEARCH_NUM_HITS = 10;
  private int numHits;
  @Override
  public void setup() throws Exception {
    super.setup();
    numHits = getRunData().getConfig().get("search.num.hits", DEFAULT_SEARCH_NUM_HITS);
  }
  public int numHits() {
    return numHits;
  }
  public abstract boolean withRetrieve();
  public int numToHighlight() {
    return 0;
  }
  protected BenchmarkHighlighter getBenchmarkHighlighter(Query q){
    return null;
  }
  protected Sort getSort() {
    return null;
  }
  protected Collection<String> getFieldsToHighlight(Document document) {
    List<Fieldable> fieldables = document.getFields();
    Set<String> result = new HashSet<String>(fieldables.size());
    for (final Fieldable fieldable : fieldables) {
      result.add(fieldable.name());
    }
    return result;
  }
}
