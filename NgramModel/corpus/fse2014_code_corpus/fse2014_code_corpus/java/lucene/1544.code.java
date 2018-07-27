package org.apache.lucene.index;
import java.util.List;
import java.io.IOException;
public interface IndexDeletionPolicy {
  public void onInit(List<? extends IndexCommit> commits) throws IOException;
  public void onCommit(List<? extends IndexCommit> commits) throws IOException;
}
