package org.apache.lucene.search;
import java.io.IOException;
import java.io.Closeable;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
public interface Searchable extends Closeable {
  void search(Weight weight, Filter filter, Collector collector) throws IOException;
  void close() throws IOException;
  int docFreq(Term term) throws IOException;
  int[] docFreqs(Term[] terms) throws IOException;
  int maxDoc() throws IOException;
  TopDocs search(Weight weight, Filter filter, int n) throws IOException;
  Document doc(int i) throws CorruptIndexException, IOException;
  Document doc(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException;
  Query rewrite(Query query) throws IOException;
  Explanation explain(Weight weight, int doc) throws IOException;
  TopFieldDocs search(Weight weight, Filter filter, int n, Sort sort)
  throws IOException;
}
