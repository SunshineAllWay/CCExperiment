package org.apache.lucene.search;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.NamedThreadFactory;
import org.apache.lucene.util.ThreadInterruptedException;
public class ParallelMultiSearcher extends MultiSearcher {
  private final ExecutorService executor;
  private final Searchable[] searchables;
  private final int[] starts;
  public ParallelMultiSearcher(Searchable... searchables) throws IOException {
    super(searchables);
    this.searchables = searchables;
    this.starts = getStarts();
    executor = Executors.newCachedThreadPool(new NamedThreadFactory(this.getClass().getSimpleName())); 
  }
  @Override
  public int docFreq(final Term term) throws IOException {
    @SuppressWarnings("unchecked") final Future<Integer>[] searchThreads = new Future[searchables.length];
    for (int i = 0; i < searchables.length; i++) { 
      final Searchable searchable = searchables[i];
      searchThreads[i] = executor.submit(new Callable<Integer>() {
        public Integer call() throws IOException {
          return Integer.valueOf(searchable.docFreq(term));
        }
      });
    }
    final CountDocFreq func = new CountDocFreq();
    foreach(func, Arrays.asList(searchThreads));
    return func.docFreq;
  }
  @Override
  public TopDocs search(Weight weight, Filter filter, int nDocs) throws IOException {
    final HitQueue hq = new HitQueue(nDocs, false);
    final Lock lock = new ReentrantLock();
    @SuppressWarnings("unchecked") final Future<TopDocs>[] searchThreads = new Future[searchables.length];
    for (int i = 0; i < searchables.length; i++) { 
      searchThreads[i] = executor.submit(
          new MultiSearcherCallableNoSort(lock, searchables[i], weight, filter, nDocs, hq, i, starts));
    }
    final CountTotalHits<TopDocs> func = new CountTotalHits<TopDocs>();
    foreach(func, Arrays.asList(searchThreads));
    final ScoreDoc[] scoreDocs = new ScoreDoc[hq.size()];
    for (int i = hq.size() - 1; i >= 0; i--) 
      scoreDocs[i] = hq.pop();
    return new TopDocs(func.totalHits, scoreDocs, func.maxScore);
  }
  @Override
  public TopFieldDocs search(Weight weight, Filter filter, int nDocs, Sort sort) throws IOException {
    if (sort == null) throw new NullPointerException();
    final FieldDocSortedHitQueue hq = new FieldDocSortedHitQueue(nDocs);
    final Lock lock = new ReentrantLock();
    @SuppressWarnings("unchecked") final Future<TopFieldDocs>[] searchThreads = new Future[searchables.length];
    for (int i = 0; i < searchables.length; i++) { 
      searchThreads[i] = executor.submit(
          new MultiSearcherCallableWithSort(lock, searchables[i], weight, filter, nDocs, hq, sort, i, starts));
    }
    final CountTotalHits<TopFieldDocs> func = new CountTotalHits<TopFieldDocs>();
    foreach(func, Arrays.asList(searchThreads));
    final ScoreDoc[] scoreDocs = new ScoreDoc[hq.size()];
    for (int i = hq.size() - 1; i >= 0; i--) 
      scoreDocs[i] = hq.pop();
    return new TopFieldDocs(func.totalHits, scoreDocs, hq.getFields(), func.maxScore);
  }
  @Override
  public void search(final Weight weight, final Filter filter, final Collector collector)
   throws IOException {
   for (int i = 0; i < searchables.length; i++) {
     final int start = starts[i];
     final Collector hc = new Collector() {
       @Override
       public void setScorer(final Scorer scorer) throws IOException {
         collector.setScorer(scorer);
       }
       @Override
       public void collect(final int doc) throws IOException {
         collector.collect(doc);
       }
       @Override
       public void setNextReader(final IndexReader reader, final int docBase) throws IOException {
         collector.setNextReader(reader, start + docBase);
       }
       @Override
       public boolean acceptsDocsOutOfOrder() {
         return collector.acceptsDocsOutOfOrder();
       }
     };
     searchables[i].search(weight, filter, hc);
   }
  }
  @Override
  public void close() throws IOException {
    executor.shutdown();
    super.close();
  }
  @Override
  HashMap<Term, Integer> createDocFrequencyMap(Set<Term> terms) throws IOException {
    final Term[] allTermsArray = terms.toArray(new Term[terms.size()]);
    final int[] aggregatedDocFreqs = new int[terms.size()];
    final ArrayList<Future<int[]>> searchThreads = new ArrayList<Future<int[]>>(searchables.length);
    for (Searchable searchable : searchables) {
      final Future<int[]> future = executor.submit(
          new DocumentFrequencyCallable(searchable, allTermsArray));
      searchThreads.add(future);
    }
    foreach(new AggregateDocFrequency(aggregatedDocFreqs), searchThreads);
    final HashMap<Term,Integer> dfMap = new HashMap<Term,Integer>();
    for(int i=0; i<allTermsArray.length; i++) {
      dfMap.put(allTermsArray[i], Integer.valueOf(aggregatedDocFreqs[i]));
    }
    return dfMap;
  }
  private <T> void foreach(Function<T> func, List<Future<T>> list) throws IOException{
    for (Future<T> future : list) {
      try{
        func.apply(future.get());
      } catch (ExecutionException e) {
        final Throwable throwable = e.getCause();
        if (throwable instanceof IOException)
          throw (IOException) e.getCause();
        throw new RuntimeException(throwable);
      } catch (InterruptedException ie) {
        throw new ThreadInterruptedException(ie);
      }
    }
  }
  private static interface Function<T> {
    abstract void apply(T t);
  }
  private static final class CountTotalHits<T extends TopDocs> implements Function<T> {
    int totalHits = 0;
    float maxScore = Float.NEGATIVE_INFINITY;
    public void apply(T t) {
      totalHits += t.totalHits;
      maxScore = Math.max(maxScore, t.getMaxScore());
    }
  }
  private static final class CountDocFreq implements Function<Integer>{
    int docFreq = 0;
    public void apply(Integer t) {
      docFreq += t.intValue();
    }
  }
  private static final class AggregateDocFrequency implements Function<int[]>{
    final int[] aggregatedDocFreqs;
    public AggregateDocFrequency(int[] aggregatedDocFreqs){
      this.aggregatedDocFreqs = aggregatedDocFreqs;
    }
    public void apply(final int[] docFreqs) {
      for(int i=0; i<aggregatedDocFreqs.length; i++){
        aggregatedDocFreqs[i] += docFreqs[i];
      }
    }
  }
  private static final class DocumentFrequencyCallable implements Callable<int[]> {
    private final Searchable searchable;
    private final Term[] terms;
    public DocumentFrequencyCallable(Searchable searchable, Term[] terms) {
      this.searchable = searchable;
      this.terms = terms;
    }
    public int[] call() throws Exception {
      return searchable.docFreqs(terms);
    }
  }
}
