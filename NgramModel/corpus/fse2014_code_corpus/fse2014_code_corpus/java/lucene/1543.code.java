package org.apache.lucene.index;
import java.util.Collection;
import java.util.Map;
import java.io.IOException;
import org.apache.lucene.store.Directory;
public abstract class IndexCommit {
  public abstract String getSegmentsFileName();
  public abstract Collection<String> getFileNames() throws IOException;
  public abstract Directory getDirectory();
  public void delete() {
    throw new UnsupportedOperationException("This IndexCommit does not support this method.");
  }
  public boolean isDeleted() {
    throw new UnsupportedOperationException("This IndexCommit does not support this method.");
  }
  public boolean isOptimized() {
    throw new UnsupportedOperationException("This IndexCommit does not support this method.");
  }
  @Override
  public boolean equals(Object other) {
    if (other instanceof IndexCommit) {
      IndexCommit otherCommit = (IndexCommit) other;
      return otherCommit.getDirectory().equals(getDirectory()) && otherCommit.getVersion() == getVersion();
    } else
      return false;
  }
  @Override
  public int hashCode() {
    return getDirectory().hashCode() + getSegmentsFileName().hashCode();
  }
  public long getVersion() {
    throw new UnsupportedOperationException("This IndexCommit does not support this method.");
  }
  public long getGeneration() {
    throw new UnsupportedOperationException("This IndexCommit does not support this method.");
  }
  public long getTimestamp() throws IOException {
    return getDirectory().fileModified(getSegmentsFileName());
  }
  public Map<String,String> getUserData() throws IOException {
    throw new UnsupportedOperationException("This IndexCommit does not support this method.");
  }
}
