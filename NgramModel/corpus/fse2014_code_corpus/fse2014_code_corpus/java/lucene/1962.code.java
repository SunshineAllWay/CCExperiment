package org.apache.lucene.search;
import java.io.IOException;
public class TestParallelMultiSearcher extends TestMultiSearcher {
  public TestParallelMultiSearcher(String name) {
    super(name);
  }
  @Override
  protected MultiSearcher getMultiSearcherInstance(Searcher[] searchers)
    throws IOException {
    return new ParallelMultiSearcher(searchers);
  }
}
