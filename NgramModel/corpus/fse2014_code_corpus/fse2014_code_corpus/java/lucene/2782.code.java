package org.apache.solr.core;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.solr.util.AbstractSolrTestCase;
import java.io.IOException;
public class IndexReaderFactoryTest extends AbstractSolrTestCase {
  public String getSchemaFile() {
    return "schema.xml";
  }
  public String getSolrConfigFile() {
    return "solrconfig-termindex.xml";
  }
  public void testAltReaderUsed() throws Exception {
    IndexReaderFactory readerFactory = h.getCore().getIndexReaderFactory();
    assertNotNull("Factory is null", readerFactory);
    assertTrue("readerFactory is not an instanceof " + AlternateIndexReaderTest.TestIndexReaderFactory.class, readerFactory instanceof StandardIndexReaderFactory);
    assertTrue("termInfoIndexDivisor not set to 12", readerFactory.getTermInfosIndexDivisor() == 12);
  }
}