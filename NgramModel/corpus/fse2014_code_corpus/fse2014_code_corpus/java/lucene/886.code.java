package org.apache.lucene.benchmark.byTask.tasks;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
public class OpenReaderTask extends PerfTask {
  public static final String USER_DATA = "userData";
  private boolean readOnly = true;
  private String commitUserData = null;
  public OpenReaderTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws IOException {
    Directory dir = getRunData().getDirectory();
    Config config = getRunData().getConfig();
    IndexReader r = null;
    final IndexDeletionPolicy deletionPolicy;
    if (readOnly) {
      deletionPolicy = null;
    } else {
      deletionPolicy = CreateIndexTask.getIndexDeletionPolicy(config);
    }
    if (commitUserData != null) {
      r = IndexReader.open(OpenReaderTask.findIndexCommit(dir, commitUserData),
                           deletionPolicy,
                           readOnly); 
    } else {
      r = IndexReader.open(dir,
                           deletionPolicy,
                           readOnly); 
    }
    getRunData().setIndexReader(r);
    r.decRef();
    return 1;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    if (params != null) {
      String[] split = params.split(",");
      if (split.length > 0) {
        readOnly = Boolean.valueOf(split[0]).booleanValue();
      }
      if (split.length > 1) {
        commitUserData = split[1];
      }
    }
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
  public static IndexCommit findIndexCommit(Directory dir, String userData) throws IOException {
    Collection<IndexCommit> commits = IndexReader.listCommits(dir);
    for (final IndexCommit ic : commits) {
      Map<String,String> map = ic.getUserData();
      String ud = null;
      if (map != null) {
        ud = map.get(USER_DATA);
      }
      if (ud != null && ud.equals(userData)) {
        return ic;
      }
    }
    throw new IOException("index does not contain commit with userData: " + userData);
  }
}
