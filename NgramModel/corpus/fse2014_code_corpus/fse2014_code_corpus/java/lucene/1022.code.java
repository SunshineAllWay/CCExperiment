package org.apache.lucene.store.instantiated;
import org.apache.lucene.index.AbstractAllTermDocs;
class InstantiatedAllTermDocs extends AbstractAllTermDocs {
  private InstantiatedIndexReader reader;
  InstantiatedAllTermDocs(InstantiatedIndexReader reader) {
    super(reader.maxDoc());
    this.reader = reader;
  }
  @Override
  public boolean isDeleted(int doc) {
    return reader.isDeleted(doc);
  }
}
