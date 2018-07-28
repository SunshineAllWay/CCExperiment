package org.apache.solr.handler.dataimport;
import org.junit.After;
import org.junit.Before;
public class TestTikaEntityProcessor extends AbstractDataImportHandlerTest {
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }
  public String getSchemaFile() {
    return "dataimport-schema-no-unique-key.xml";
  }
  public String getSolrConfigFile() {
    return "dataimport-solrconfig.xml";
  }
  public void testIndexingWithTikaEntityProcessor() throws Exception {
    String conf =
            "<dataConfig>" +
                    "  <dataSource type=\"BinFileDataSource\"/>" +
                    "  <document>" +
                    "    <entity processor=\"TikaEntityProcessor\" url=\"../../../../../extraction/src/test/resources/solr-word.pdf\" >" +
                    "      <field column=\"Author\" meta=\"true\" name=\"author\"/>" +
                    "      <field column=\"title\" meta=\"true\" name=\"docTitle\"/>" +
                    "      <field column=\"text\"/>" +
                    "     </entity>" +
                    "  </document>" +
                    "</dataConfig>";
    super.runFullImport(conf);
    assertQ(req("*:*"), "//*[@numFound='1']");
  }
}
