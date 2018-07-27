package org.apache.solr.core;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.solr.util.AbstractSolrTestCase;
public class AlternateIndexReaderTest extends AbstractSolrTestCase {
  public String getSchemaFile() {
    return "schema.xml";
  }
  public String getSolrConfigFile() {
    return "solrconfig-altdirectory.xml";
  }
  public void testAltReaderUsed() throws Exception {
    assertTrue(TestIndexReaderFactory.newReaderCalled);
  }
  static public class TestIndexReaderFactory extends IndexReaderFactory {
    static boolean newReaderCalled = false;
    public IndexReader newReader(Directory indexDir) throws IOException {
      TestIndexReaderFactory.newReaderCalled = true;
      return IndexReader.open(indexDir);
    }
    public IndexReader newReader(Directory indexDir, boolean readOnly)
        throws IOException {
      TestIndexReaderFactory.newReaderCalled = true;
      return IndexReader.open(indexDir, readOnly);
    }
  }
}
