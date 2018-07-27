package org.apache.lucene.search;
import org.apache.lucene.util.PriorityQueue;
public abstract class TopDocsCollector<T extends ScoreDoc> extends Collector {
  protected static final TopDocs EMPTY_TOPDOCS = new TopDocs(0, new ScoreDoc[0], Float.NaN);
  protected PriorityQueue<T> pq;
  protected int totalHits;
  protected TopDocsCollector(PriorityQueue<T> pq) {
    this.pq = pq;
  }
  protected void populateResults(ScoreDoc[] results, int howMany) {
    for (int i = howMany - 1; i >= 0; i--) { 
      results[i] = pq.pop();
    }
  }
  protected TopDocs newTopDocs(ScoreDoc[] results, int start) {
    return results == null ? EMPTY_TOPDOCS : new TopDocs(totalHits, results);
  }
  public int getTotalHits() {
    return totalHits;
  }
  public final TopDocs topDocs() {
    return topDocs(0, totalHits < pq.size() ? totalHits : pq.size());
  }
  public final TopDocs topDocs(int start) {
    return topDocs(start, totalHits < pq.size() ? totalHits : pq.size());
  }
  public final TopDocs topDocs(int start, int howMany) {
    int size = totalHits < pq.size() ? totalHits : pq.size();
    if (start < 0 || start >= size || howMany <= 0) {
      return newTopDocs(null, start);
    }
    howMany = Math.min(size - start, howMany);
    ScoreDoc[] results = new ScoreDoc[howMany];
    for (int i = pq.size() - start - howMany; i > 0; i--) { pq.pop(); }
    populateResults(results, howMany);
    return newTopDocs(results, start);
  }
}
