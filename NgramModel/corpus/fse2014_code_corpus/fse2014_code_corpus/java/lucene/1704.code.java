package org.apache.lucene.search;
import org.apache.lucene.index.IndexReader;
import java.io.IOException;
public abstract class SpanFilter extends Filter{
  public abstract SpanFilterResult bitSpans(IndexReader reader) throws IOException;
}
