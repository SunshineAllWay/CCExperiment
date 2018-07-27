package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.ContentSource;
import org.apache.lucene.benchmark.byTask.feeds.DocData;
import org.apache.lucene.benchmark.byTask.utils.Config;
public class ConsumeContentSourceTask extends PerfTask {
  private ContentSource source;
  private DocData dd = new DocData();
  public ConsumeContentSourceTask(PerfRunData runData) {
    super(runData);
    Config config = runData.getConfig();
    String sourceClass = config.get("content.source", null);
    if (sourceClass == null) {
      throw new IllegalArgumentException("content.source must be defined");
    }
    try {
      source = Class.forName(sourceClass).asSubclass(ContentSource.class).newInstance();
      source.setConfig(config);
      source.resetInputs();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  protected String getLogMessage(int recsCount) {
    return "read " + recsCount + " documents from the content source";
  }
  @Override
  public void close() throws Exception {
    source.close();
    super.close();
  }
  @Override
  public int doLogic() throws Exception {
    dd = source.getNextDocData(dd);
    return 1;
  }
}
