package org.apache.lucene.benchmark.byTask;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.feeds.QueryMaker;
import org.apache.lucene.benchmark.byTask.stats.Points;
import org.apache.lucene.benchmark.byTask.tasks.ReadTask;
import org.apache.lucene.benchmark.byTask.tasks.SearchTask;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.benchmark.byTask.utils.FileUtils;
import org.apache.lucene.benchmark.byTask.tasks.NewAnalyzerTask;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
public class PerfRunData {
  private Points points;
  private Directory directory;
  private Analyzer analyzer;
  private DocMaker docMaker;
  private Locale locale;
  private HashMap<Class<? extends ReadTask>,QueryMaker> readTaskQueryMaker;
  private Class<? extends QueryMaker> qmkrClass;
  private IndexReader indexReader;
  private IndexSearcher indexSearcher;
  private IndexWriter indexWriter;
  private Config config;
  private long startTimeMillis;
  public PerfRunData (Config config) throws Exception {
    this.config = config;
    analyzer = NewAnalyzerTask.createAnalyzer(config.get("analyzer",
        "org.apache.lucene.analysis.standard.StandardAnalyzer"));
    docMaker = Class.forName(config.get("doc.maker",
        "org.apache.lucene.benchmark.byTask.feeds.DocMaker")).asSubclass(DocMaker.class).newInstance();
    docMaker.setConfig(config);
    readTaskQueryMaker = new HashMap<Class<? extends ReadTask>,QueryMaker>();
    qmkrClass = Class.forName(config.get("query.maker","org.apache.lucene.benchmark.byTask.feeds.SimpleQueryMaker")).asSubclass(QueryMaker.class);
    reinit(false);
    points = new Points(config);
    if (Boolean.valueOf(config.get("log.queries","false")).booleanValue()) {
      System.out.println("------------> queries:");
      System.out.println(getQueryMaker(new SearchTask(this)).printQueries());
    }
  }
  public void reinit(boolean eraseIndex) throws Exception {
    if (indexWriter!=null) {
      indexWriter.close();
      indexWriter = null;
    }
    if (indexReader!=null) {
      indexReader.close();
      indexReader = null;
    }
    if (directory!=null) {
      directory.close();
    }
    if ("FSDirectory".equals(config.get("directory","RAMDirectory"))) {
      File workDir = new File(config.get("work.dir","work"));
      File indexDir = new File(workDir,"index");
      if (eraseIndex && indexDir.exists()) {
        FileUtils.fullyDelete(indexDir);
      }
      indexDir.mkdirs();
      directory = FSDirectory.open(indexDir);
    } else {
      directory = new RAMDirectory();
    }
    resetInputs();
    System.runFinalization();
    System.gc();
    setStartTimeMillis();
  }
  public long setStartTimeMillis() {
    startTimeMillis = System.currentTimeMillis();
    return startTimeMillis;
  }
  public long getStartTimeMillis() {
    return startTimeMillis;
  }
  public Points getPoints() {
    return points;
  }
  public Directory getDirectory() {
    return directory;
  }
  public void setDirectory(Directory directory) {
    this.directory = directory;
  }
  public synchronized IndexReader getIndexReader() {
    if (indexReader != null) {
      indexReader.incRef();
    }
    return indexReader;
  }
  public synchronized IndexSearcher getIndexSearcher() {
    if (indexReader != null) {
      indexReader.incRef();
    }
    return indexSearcher;
  }
  public synchronized void setIndexReader(IndexReader indexReader) throws IOException {
    if (this.indexReader != null) {
      this.indexReader.decRef();
    }
    this.indexReader = indexReader;
    if (indexReader != null) {
      indexReader.incRef();
      indexSearcher = new IndexSearcher(indexReader);
    } else {
      indexSearcher = null;
    }
  }
  public IndexWriter getIndexWriter() {
    return indexWriter;
  }
  public void setIndexWriter(IndexWriter indexWriter) {
    this.indexWriter = indexWriter;
  }
  public Analyzer getAnalyzer() {
    return analyzer;
  }
  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }
  public DocMaker getDocMaker() {
    return docMaker;
  }
  public Locale getLocale() {
    return locale;
  }
  public void setLocale(Locale locale) {
    this.locale = locale;
  }
  public Config getConfig() {
    return config;
  }
  public void resetInputs() throws IOException {
    docMaker.resetInputs();
    for (final QueryMaker queryMaker : readTaskQueryMaker.values()) {
      queryMaker.resetInputs();
    }
  }
  synchronized public QueryMaker getQueryMaker(ReadTask readTask) {
    Class<? extends ReadTask> readTaskClass = readTask.getClass();
    QueryMaker qm = readTaskQueryMaker.get(readTaskClass);
    if (qm == null) {
      try {
        qm = qmkrClass.newInstance();
        qm.setConfig(config);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      readTaskQueryMaker.put(readTaskClass,qm);
    }
    return qm;
  }
}
