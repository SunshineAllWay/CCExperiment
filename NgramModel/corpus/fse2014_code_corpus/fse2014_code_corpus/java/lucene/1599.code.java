package org.apache.lucene.index;
import java.io.IOException;
import java.io.Closeable;
public interface TermDocs extends Closeable {
  void seek(Term term) throws IOException;
  void seek(TermEnum termEnum) throws IOException;
  int doc();
  int freq();
  boolean next() throws IOException;
  int read(int[] docs, int[] freqs) throws IOException;
  boolean skipTo(int target) throws IOException;
  void close() throws IOException;
}
