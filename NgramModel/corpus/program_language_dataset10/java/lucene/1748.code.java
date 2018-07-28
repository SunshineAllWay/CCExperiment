package org.apache.lucene.search.spans;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Weight;
public abstract class SpanQuery extends Query {
  public abstract Spans getSpans(IndexReader reader) throws IOException;
  public abstract String getField();
  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    return new SpanWeight(this, searcher);
  }
}
