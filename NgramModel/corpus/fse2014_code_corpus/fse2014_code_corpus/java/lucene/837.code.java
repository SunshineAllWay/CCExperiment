package org.apache.lucene.ant;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.lucene.util.LuceneTestCase;
public class IndexTaskTest extends LuceneTestCase {
    private final static String docHandler =
            "org.apache.lucene.ant.FileExtensionDocumentHandler";
    private String docsDir = System.getProperty("docs.dir");
    private File indexDir = new File(System.getProperty("index.dir"));
    private Searcher searcher;
    private Analyzer analyzer;
    private FSDirectory dir;
    @Override
    protected void setUp() throws Exception {
      super.setUp();
        Project project = new Project();
        IndexTask task = new IndexTask();
        FileSet fs = new FileSet();
        fs.setProject(project);
        fs.setDir(new File(docsDir));
        task.addFileset(fs);
        task.setOverwrite(true);
        task.setDocumentHandler(docHandler);
        task.setIndex(indexDir);
        task.setProject(project);
        task.execute();
        dir = FSDirectory.open(indexDir);
        searcher = new IndexSearcher(dir, true);
        analyzer = new StopAnalyzer(TEST_VERSION_CURRENT);
    }
    public void testSearch() throws Exception {
      Query query = new QueryParser(TEST_VERSION_CURRENT, "contents",analyzer).parse("test");
        int numHits = searcher.search(query, null, 1000).totalHits;
        assertEquals("Find document(s)", 2, numHits);
    }
    @Override
    protected void tearDown() throws Exception {
        searcher.close();
        dir.close();
        super.tearDown();
    }
}
