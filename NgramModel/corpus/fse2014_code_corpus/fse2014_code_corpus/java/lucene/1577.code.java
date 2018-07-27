package org.apache.lucene.index;
class ReadOnlySegmentReader extends SegmentReader {
  static void noWrite() {
    throw new UnsupportedOperationException("This IndexReader cannot make any changes to the index (it was opened with readOnly = true)");
  }
  @Override
  protected void acquireWriteLock() {
    noWrite();
  }
  @Override
  public boolean isDeleted(int n) {
    return deletedDocs != null && deletedDocs.get(n);
  }
}
