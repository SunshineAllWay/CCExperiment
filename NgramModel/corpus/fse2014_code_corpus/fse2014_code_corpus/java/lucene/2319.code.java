package org.apache.solr.core;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
public class StandardDirectoryFactory extends DirectoryFactory {
  public Directory open(String path) throws IOException {
    return FSDirectory.open(new File(path));
  }
}
