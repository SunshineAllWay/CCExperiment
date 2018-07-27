package org.apache.solr.core;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.util.TestHarness;
import java.io.File;
public class TestBadConfig extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema.xml"; }
  public String getSolrConfigFile() { return "bad_solrconfig.xml"; }
  public void setUp() throws Exception {
    dataDir = new File(System.getProperty("java.io.tmpdir")
                       + System.getProperty("file.separator")
                       + getClass().getName());
    dataDir.mkdirs();
    try {
      solrConfig = new SolrConfig(getSolrConfigFile());
      h = new TestHarness( dataDir.getAbsolutePath(),
                           solrConfig,
                           getSchemaFile());
      fail("Exception should have been thrown");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("unset.sys.property"));
    }
  }
  public void testNothing() {
    assertTrue(true);
  }
}