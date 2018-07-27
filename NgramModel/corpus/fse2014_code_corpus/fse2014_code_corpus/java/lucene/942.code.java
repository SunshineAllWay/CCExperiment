package org.apache.lucene.benchmark.utils;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;
public class NoDeletionPolicy implements IndexDeletionPolicy {
  public void onCommit(List<? extends IndexCommit> commits) throws IOException {
  }
  public void onInit(List<? extends IndexCommit> commits) throws IOException {
  }
}
