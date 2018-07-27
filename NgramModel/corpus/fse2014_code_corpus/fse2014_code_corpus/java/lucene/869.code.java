package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
public abstract class BenchmarkHighlighter {
  public abstract int doHighlight( IndexReader reader, int doc, String field,
      Document document, Analyzer analyzer, String text ) throws Exception ;
}
