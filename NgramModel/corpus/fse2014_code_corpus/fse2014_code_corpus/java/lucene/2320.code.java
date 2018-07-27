package org.apache.solr.core;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.solr.common.util.NamedList;
public class StandardIndexReaderFactory extends IndexReaderFactory {
  public IndexReader newReader(Directory indexDir, boolean readOnly)
      throws IOException {
    return IndexReader.open(indexDir, null, readOnly, termInfosIndexDivisor);
  }
}
