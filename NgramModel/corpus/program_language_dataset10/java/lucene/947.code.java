package org.apache.lucene.benchmark.byTask.feeds;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.benchmark.BenchmarkTestCase;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.tasks.AddDocTask;
import org.apache.lucene.benchmark.byTask.tasks.CloseIndexTask;
import org.apache.lucene.benchmark.byTask.tasks.CreateIndexTask;
import org.apache.lucene.benchmark.byTask.tasks.TaskSequence;
import org.apache.lucene.benchmark.byTask.tasks.WriteLineDocTask;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
public class LineDocSourceTest extends BenchmarkTestCase {
  private static final CompressorStreamFactory csFactory = new CompressorStreamFactory();
  private void createBZ2LineFile(File file) throws Exception {
    OutputStream out = new FileOutputStream(file);
    out = csFactory.createCompressorOutputStream("bzip2", out);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
    StringBuffer doc = new StringBuffer();
    doc.append("title").append(WriteLineDocTask.SEP).append("date").append(WriteLineDocTask.SEP).append("body");
    writer.write(doc.toString());
    writer.newLine();
    writer.close();
  }
  private void createRegularLineFile(File file) throws Exception {
    OutputStream out = new FileOutputStream(file);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
    StringBuffer doc = new StringBuffer();
    doc.append("title").append(WriteLineDocTask.SEP).append("date").append(WriteLineDocTask.SEP).append("body");
    writer.write(doc.toString());
    writer.newLine();
    writer.close();
  }
  private void doIndexAndSearchTest(File file, boolean setBZCompress,
      String bz2CompressVal) throws Exception {
    Properties props = new Properties();
    props.setProperty("docs.file", file.getAbsolutePath());
    if (setBZCompress) {
      props.setProperty("bzip.compression", bz2CompressVal);
    }
    props.setProperty("analyzer", SimpleAnalyzer.class.getName());
    props.setProperty("content.source", LineDocSource.class.getName());
    props.setProperty("directory", "RAMDirectory");
    Config config = new Config(props);
    PerfRunData runData = new PerfRunData(config);
    TaskSequence tasks = new TaskSequence(runData, "testBzip2", null, false);
    tasks.addTask(new CreateIndexTask(runData));
    tasks.addTask(new AddDocTask(runData));
    tasks.addTask(new CloseIndexTask(runData));
    tasks.doLogic();
    IndexSearcher searcher = new IndexSearcher(runData.getDirectory(), true);
    TopDocs td = searcher.search(new TermQuery(new Term("body", "body")), 10);
    assertEquals(1, td.totalHits);
    assertNotNull(td.scoreDocs[0]);
    searcher.close();
  }
  public void testBZip2() throws Exception {
    File file = new File(getWorkDir(), "one-line.bz2");
    createBZ2LineFile(file);
    doIndexAndSearchTest(file, true, "true");
  }
  public void testBZip2AutoDetect() throws Exception {
    File file = new File(getWorkDir(), "one-line.bz2");
    createBZ2LineFile(file);
    doIndexAndSearchTest(file, false, null);
  }
  public void testRegularFile() throws Exception {
    File file = new File(getWorkDir(), "one-line");
    createRegularLineFile(file);
    doIndexAndSearchTest(file, false, null);
  }
  public void testInvalidFormat() throws Exception {
    String[] testCases = new String[] {
      "", 
      "title", 
      "title" + WriteLineDocTask.SEP, 
      "title" + WriteLineDocTask.SEP + "body", 
    };
    for (int i = 0; i < testCases.length; i++) {
      File file = new File(getWorkDir(), "one-line");
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
      writer.write(testCases[i]);
      writer.newLine();
      writer.close();
      try {
        doIndexAndSearchTest(file, false, null);
        fail("Some exception should have been thrown for: [" + testCases[i] + "]");
      } catch (Exception e) {
      }
    }
  }
}
