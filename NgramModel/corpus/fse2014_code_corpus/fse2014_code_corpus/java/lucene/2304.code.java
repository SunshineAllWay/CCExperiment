package org.apache.solr.core;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
public abstract class IndexReaderFactory implements NamedListInitializedPlugin {
  protected int termInfosIndexDivisor = 1;
  public void init(NamedList args) {
    Integer v = (Integer)args.get("setTermIndexInterval");
    if (v != null) {
      termInfosIndexDivisor = v.intValue();
    }
  }
  public int getTermInfosIndexDivisor() {
    return termInfosIndexDivisor;
  }
  public abstract IndexReader newReader(Directory indexDir, boolean readOnly)
      throws IOException;
}
