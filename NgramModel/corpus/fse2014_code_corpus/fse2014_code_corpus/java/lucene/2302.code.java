package org.apache.solr.core;
import java.io.IOException;
import org.apache.lucene.store.Directory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
public abstract class DirectoryFactory implements NamedListInitializedPlugin {
  public abstract Directory open(String path) throws IOException;
  public void init(NamedList args) {
  }
}
