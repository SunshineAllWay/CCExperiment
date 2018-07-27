package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.util.Version;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
public class CreateIndexTask extends PerfTask {
  public CreateIndexTask(PerfRunData runData) {
    super(runData);
  }
  public static void setIndexWriterConfig(IndexWriter writer, Config config) throws IOException {
    final String mergeScheduler = config.get("merge.scheduler",
                                             "org.apache.lucene.index.ConcurrentMergeScheduler");
    try {
      writer.setMergeScheduler(Class.forName(mergeScheduler).asSubclass(MergeScheduler.class).newInstance());
    } catch (Exception e) {
      throw new RuntimeException("unable to instantiate class '" + mergeScheduler + "' as merge scheduler", e);
    }
    if (mergeScheduler.equals("org.apache.lucene.index.ConcurrentMergeScheduler")) {
      ConcurrentMergeScheduler cms = (ConcurrentMergeScheduler) writer.getMergeScheduler();
      int v = config.get("concurrent.merge.scheduler.max.thread.count", -1);
      if (v != -1) {
        cms.setMaxThreadCount(v);
      }
      v = config.get("concurrent.merge.scheduler.max.merge.count", -1);
      if (v != -1) {
        cms.setMaxMergeCount(v);
      }
    }
    final String mergePolicy = config.get("merge.policy",
                                          "org.apache.lucene.index.LogByteSizeMergePolicy");
    try {
      writer.setMergePolicy(Class.forName(mergePolicy).asSubclass(MergePolicy.class).getConstructor(IndexWriter.class).newInstance(writer));
    } catch (Exception e) {
      throw new RuntimeException("unable to instantiate class '" + mergePolicy + "' as merge policy", e);
    }
    writer.setUseCompoundFile(config.get("compound",true));
    writer.setMergeFactor(config.get("merge.factor",OpenIndexTask.DEFAULT_MERGE_PFACTOR));
    writer.setMaxFieldLength(config.get("max.field.length",OpenIndexTask.DEFAULT_MAX_FIELD_LENGTH));
    final double ramBuffer = config.get("ram.flush.mb",OpenIndexTask.DEFAULT_RAM_FLUSH_MB);
    final int maxBuffered = config.get("max.buffered",OpenIndexTask.DEFAULT_MAX_BUFFERED);
    if (maxBuffered == IndexWriterConfig.DISABLE_AUTO_FLUSH) {
      writer.setRAMBufferSizeMB(ramBuffer);
      writer.setMaxBufferedDocs(maxBuffered);
    } else {
      writer.setMaxBufferedDocs(maxBuffered);
      writer.setRAMBufferSizeMB(ramBuffer);
    }
    String infoStreamVal = config.get("writer.info.stream", null);
    if (infoStreamVal != null) {
      if (infoStreamVal.equals("SystemOut")) {
        writer.setInfoStream(System.out);
      } else if (infoStreamVal.equals("SystemErr")) {
        writer.setInfoStream(System.err);
      } else {
        File f = new File(infoStreamVal).getAbsoluteFile();
        writer.setInfoStream(new PrintStream(new BufferedOutputStream(new FileOutputStream(f))));
      }
    }
  }
  public static IndexDeletionPolicy getIndexDeletionPolicy(Config config) {
    String deletionPolicyName = config.get("deletion.policy", "org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy");
    IndexDeletionPolicy indexDeletionPolicy = null;
    RuntimeException err = null;
    try {
      indexDeletionPolicy = Class.forName(deletionPolicyName).asSubclass(IndexDeletionPolicy.class).newInstance();
    } catch (IllegalAccessException iae) {
      err = new RuntimeException("unable to instantiate class '" + deletionPolicyName + "' as IndexDeletionPolicy");
      err.initCause(iae);
    } catch (InstantiationException ie) {
      err = new RuntimeException("unable to instantiate class '" + deletionPolicyName + "' as IndexDeletionPolicy");
      err.initCause(ie);
    } catch (ClassNotFoundException cnfe) {
      err = new RuntimeException("unable to load class '" + deletionPolicyName + "' as IndexDeletionPolicy");
      err.initCause(cnfe);
    }
    if (err != null)
      throw err;
    return indexDeletionPolicy;
  }
  @Override
  public int doLogic() throws IOException {
    PerfRunData runData = getRunData();
    Config config = runData.getConfig();
    IndexWriter writer = new IndexWriter(runData.getDirectory(),
        new IndexWriterConfig(Version.LUCENE_31, runData.getAnalyzer())
            .setOpenMode(OpenMode.CREATE).setIndexDeletionPolicy(
                getIndexDeletionPolicy(config)));
    setIndexWriterConfig(writer, config);
    runData.setIndexWriter(writer);
    return 1;
  }
}
