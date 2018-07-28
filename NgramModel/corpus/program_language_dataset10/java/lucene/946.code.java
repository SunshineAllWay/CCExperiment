package org.apache.lucene.benchmark.byTask.feeds;
import java.io.IOException;
import java.util.Properties;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.benchmark.BenchmarkTestCase;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.tasks.AddDocTask;
import org.apache.lucene.benchmark.byTask.tasks.CloseIndexTask;
import org.apache.lucene.benchmark.byTask.tasks.CreateIndexTask;
import org.apache.lucene.benchmark.byTask.tasks.TaskSequence;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
public class DocMakerTest extends BenchmarkTestCase {
  static final class OneDocSource extends ContentSource {
    private boolean finish = false;
    @Override
    public void close() throws IOException {
    }
    @Override
    public DocData getNextDocData(DocData docData) throws NoMoreDataException,
        IOException {
      if (finish) {
        throw new NoMoreDataException();
      }
      docData.setBody("body");
      docData.setDate("date");
      docData.setTitle("title");
      Properties props = new Properties();
      props.setProperty("key", "value");
      docData.setProps(props);
      finish = true;
      return docData;
    }
  }
  private void doTestIndexProperties(boolean setIndexProps,
      boolean indexPropsVal, int numExpectedResults) throws Exception {
    Properties props = new Properties();
    props.setProperty("analyzer", SimpleAnalyzer.class.getName());
    props.setProperty("content.source", OneDocSource.class.getName());
    props.setProperty("directory", "RAMDirectory");
    if (setIndexProps) {
      props.setProperty("doc.index.props", Boolean.toString(indexPropsVal));
    }
    Config config = new Config(props);
    PerfRunData runData = new PerfRunData(config);
    TaskSequence tasks = new TaskSequence(runData, getName(), null, false);
    tasks.addTask(new CreateIndexTask(runData));
    tasks.addTask(new AddDocTask(runData));
    tasks.addTask(new CloseIndexTask(runData));
    tasks.doLogic();
    IndexSearcher searcher = new IndexSearcher(runData.getDirectory(), true);
    TopDocs td = searcher.search(new TermQuery(new Term("key", "value")), 10);
    assertEquals(numExpectedResults, td.totalHits);
    searcher.close();
  }
  private Document createTestNormsDocument(boolean setNormsProp,
      boolean normsPropVal, boolean setBodyNormsProp, boolean bodyNormsVal)
      throws Exception {
    Properties props = new Properties();
    props.setProperty("analyzer", SimpleAnalyzer.class.getName());
    props.setProperty("content.source", OneDocSource.class.getName());
    props.setProperty("directory", "RAMDirectory");
    if (setNormsProp) {
      props.setProperty("doc.tokenized.norms", Boolean.toString(normsPropVal));
    }
    if (setBodyNormsProp) {
      props.setProperty("doc.body.tokenized.norms", Boolean.toString(bodyNormsVal));
    }
    Config config = new Config(props);
    DocMaker dm = new DocMaker();
    dm.setConfig(config);
    return dm.makeDocument();
  }
  public void testIndexProperties() throws Exception {
    doTestIndexProperties(false, false, 0);
    doTestIndexProperties(true, false, 0);
    doTestIndexProperties(true, true, 1);
  }
  public void testNorms() throws Exception {
    Document doc;
    doc = createTestNormsDocument(false, false, false, false);
    assertTrue(doc.getField(DocMaker.TITLE_FIELD).getOmitNorms());
    assertFalse(doc.getField(DocMaker.BODY_FIELD).getOmitNorms());
    doc = createTestNormsDocument(true, false, false, false);
    assertTrue(doc.getField(DocMaker.TITLE_FIELD).getOmitNorms());
    assertFalse(doc.getField(DocMaker.BODY_FIELD).getOmitNorms());
    doc = createTestNormsDocument(true, true, false, false);
    assertFalse(doc.getField(DocMaker.TITLE_FIELD).getOmitNorms());
    assertFalse(doc.getField(DocMaker.BODY_FIELD).getOmitNorms());
    doc = createTestNormsDocument(false, false, true, false);
    assertTrue(doc.getField(DocMaker.TITLE_FIELD).getOmitNorms());
    assertTrue(doc.getField(DocMaker.BODY_FIELD).getOmitNorms());
    doc = createTestNormsDocument(false, false, true, true);
    assertTrue(doc.getField(DocMaker.TITLE_FIELD).getOmitNorms());
    assertFalse(doc.getField(DocMaker.BODY_FIELD).getOmitNorms());
  }
}
