package org.apache.lucene.index;
import org.apache.lucene.util.BitVector;
class AllTermDocs extends AbstractAllTermDocs {
  protected BitVector deletedDocs;
  protected AllTermDocs(SegmentReader parent) {
    super(parent.maxDoc());
    synchronized (parent) {
      this.deletedDocs = parent.deletedDocs;
    }
  }
  @Override
  public boolean isDeleted(int doc) {
    return deletedDocs != null && deletedDocs.get(doc);
  }
}
