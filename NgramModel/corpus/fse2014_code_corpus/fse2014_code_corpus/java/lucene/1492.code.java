package org.apache.lucene.index;
import java.io.IOException;
public abstract class AbstractAllTermDocs implements TermDocs {
  protected int maxDoc;
  protected int doc = -1;
  protected AbstractAllTermDocs(int maxDoc) {
    this.maxDoc = maxDoc;
  }
  public void seek(Term term) throws IOException {
    if (term==null) {
      doc = -1;
    } else {
      throw new UnsupportedOperationException();
    }
  }
  public void seek(TermEnum termEnum) throws IOException {
    throw new UnsupportedOperationException();
  }
  public int doc() {
    return doc;
  }
  public int freq() {
    return 1;
  }
  public boolean next() throws IOException {
    return skipTo(doc+1);
  }
  public int read(int[] docs, int[] freqs) throws IOException {
    final int length = docs.length;
    int i = 0;
    while (i < length && doc < maxDoc) {
      if (!isDeleted(doc)) {
        docs[i] = doc;
        freqs[i] = 1;
        ++i;
      }
      doc++;
    }
    return i;
  }
  public boolean skipTo(int target) throws IOException {
    doc = target;
    while (doc < maxDoc) {
      if (!isDeleted(doc)) {
        return true;
      }
      doc++;
    }
    return false;
  }
  public void close() throws IOException {
  }
  public abstract boolean isDeleted(int doc);
}