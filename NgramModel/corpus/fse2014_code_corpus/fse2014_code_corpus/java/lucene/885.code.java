package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.util.Version;
import java.io.IOException;
public class OpenIndexTask extends PerfTask {
  public static final int DEFAULT_MAX_BUFFERED = IndexWriterConfig.DEFAULT_MAX_BUFFERED_DOCS;
  public static final int DEFAULT_MAX_FIELD_LENGTH = IndexWriterConfig.UNLIMITED_FIELD_LENGTH;
  public static final int DEFAULT_MERGE_PFACTOR = LogMergePolicy.DEFAULT_MERGE_FACTOR;
  public static final double DEFAULT_RAM_FLUSH_MB = (int) IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;
  private String commitUserData;
  public OpenIndexTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws IOException {
    PerfRunData runData = getRunData();
    Config config = runData.getConfig();
    final IndexCommit ic;
    if (commitUserData != null) {
      ic = OpenReaderTask.findIndexCommit(runData.getDirectory(), commitUserData);
    } else {
      ic = null;
    }
    IndexWriter writer = new IndexWriter(runData.getDirectory(),
        new IndexWriterConfig(Version.LUCENE_CURRENT, runData.getAnalyzer())
            .setIndexDeletionPolicy(CreateIndexTask.getIndexDeletionPolicy(config))
            .setIndexCommit(ic));
    CreateIndexTask.setIndexWriterConfig(writer, config);
    runData.setIndexWriter(writer);
    return 1;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    if (params != null) {
      commitUserData = params;
    }
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
