package org.apache.solr.handler.dataimport;
import junit.framework.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
public class TestDataConfig extends AbstractDataImportHandlerTest {
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  @Override
  public String getSchemaFile() {
    return "dataimport-schema.xml";
  }
  @Override
  public String getSolrConfigFile() {
    return "dataimport-nodatasource-solrconfig.xml";
  }
  @Test
  @SuppressWarnings("unchecked")
  public void testDataConfigWithDataSource() throws Exception {
    List rows = new ArrayList();
    rows.add(createMap("id", "1", "desc", "one"));
    MockDataSource.setIterator("select * from x", rows.iterator());
    super.runFullImport(loadDataConfig("data-config-with-datasource.xml"));
    assertQ(req("id:1"), "//*[@numFound='1']");
  }
  @Test
  public void basic() throws Exception {
    javax.xml.parsers.DocumentBuilder builder = DocumentBuilderFactory
            .newInstance().newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
    DataConfig dc = new DataConfig();
    dc.readFromXml(doc.getDocumentElement());
    Assert.assertEquals("atrimlisting", dc.document.entities.get(0).name);
  }
  private static final String xml = "<dataConfig>\n"
          + "\t<document name=\"autos\" >\n"
          + "\t\t<entity name=\"atrimlisting\" pk=\"acode\"\n"
          + "\t\t\tquery=\"select acode,make,model,year,msrp,category,image,izmo_image_url,price_range_low,price_range_high,invoice_range_low,invoice_range_high from atrimlisting\"\n"
          + "\t\t\tdeltaQuery=\"select acode from atrimlisting where last_modified > '${indexer.last_index_time}'\">\n"
          +
          "\t\t</entity>\n" +
          "\t</document>\n" + "</dataConfig>";
}
