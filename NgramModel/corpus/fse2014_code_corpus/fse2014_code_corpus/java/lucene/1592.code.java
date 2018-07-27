package org.apache.lucene.index;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import org.apache.lucene.store.Directory;
public class SnapshotDeletionPolicy implements IndexDeletionPolicy {
  private IndexCommit lastCommit;
  private IndexDeletionPolicy primary;
  private String snapshot;
  public SnapshotDeletionPolicy(IndexDeletionPolicy primary) {
    this.primary = primary;
  }
  public synchronized void onInit(List<? extends IndexCommit> commits) throws IOException {
    primary.onInit(wrapCommits(commits));
    lastCommit = commits.get(commits.size()-1);
  }
  public synchronized void onCommit(List<? extends IndexCommit> commits) throws IOException {
    primary.onCommit(wrapCommits(commits));
    lastCommit = commits.get(commits.size()-1);
  }
  public synchronized IndexCommit snapshot() {
    if (snapshot == null)
      snapshot = lastCommit.getSegmentsFileName();
    else
      throw new IllegalStateException("snapshot is already set; please call release() first");
    return lastCommit;
  }
  public synchronized void release() {
    if (snapshot != null)
      snapshot = null;
    else
      throw new IllegalStateException("snapshot was not set; please call snapshot() first");
  }
  private class MyCommitPoint extends IndexCommit {
    IndexCommit cp;
    MyCommitPoint(IndexCommit cp) {
      this.cp = cp;
    }
    @Override
    public String getSegmentsFileName() {
      return cp.getSegmentsFileName();
    }
    @Override
    public Collection<String> getFileNames() throws IOException {
      return cp.getFileNames();
    }
    @Override
    public Directory getDirectory() {
      return cp.getDirectory();
    }
    @Override
    public void delete() {
      synchronized(SnapshotDeletionPolicy.this) {
        if (snapshot == null || !snapshot.equals(getSegmentsFileName()))
          cp.delete();
      }
    }
    @Override
    public boolean isDeleted() {
      return cp.isDeleted();
    }
    @Override
    public long getVersion() {
      return cp.getVersion();
    }
    @Override
    public long getGeneration() {
      return cp.getGeneration();
    }
    @Override
    public Map<String,String> getUserData() throws IOException {
      return cp.getUserData();
    }
  }
  private List<IndexCommit> wrapCommits(List<? extends IndexCommit> commits) {
    final int count = commits.size();
    List<IndexCommit> myCommits = new ArrayList<IndexCommit>(count);
    for(int i=0;i<count;i++)
      myCommits.add(new MyCommitPoint(commits.get(i)));
    return myCommits;
  }
}
