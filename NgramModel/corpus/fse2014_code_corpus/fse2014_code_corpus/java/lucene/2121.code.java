package org.apache.solr.handler.dataimport;
import junit.framework.Assert;
import static org.apache.solr.handler.dataimport.AbstractDataImportHandlerTest.createMap;
import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TestFieldReader {
  @Test
  public void simple() {
    DataImporter di = new DataImporter();
    di.loadAndInit(config);
    TestDocBuilder.SolrWriterImpl sw = new TestDocBuilder.SolrWriterImpl();
    DataImporter.RequestParams rp = new DataImporter.RequestParams(createMap("command", "full-import"));
    List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
    l.add(createMap("xml", xml));
    MockDataSource.setIterator("select * from a", l.iterator());
    di.runCmd(rp, sw);
    Assert.assertEquals(sw.docs.get(0).getFieldValue("y"), "Hello");
    MockDataSource.clearCache();
  }
  String config = "<dataConfig>\n" +
          "  <dataSource type=\"FieldReaderDataSource\" name=\"f\"/>\n" +
          "  <dataSource type=\"MockDataSource\"/>\n" +
          "  <document>\n" +
          "    <entity name=\"a\" query=\"select * from a\" >\n" +
          "      <entity name=\"b\" dataSource=\"f\" processor=\"XPathEntityProcessor\" forEach=\"/x\" dataField=\"a.xml\">\n" +
          "        <field column=\"y\" xpath=\"/x/y\"/>\n" +
          "      </entity>\n" +
          "    </entity>\n" +
          "  </document>\n" +
          "</dataConfig>";
  String xml = "<x>\n" +
          " <y>Hello</y>\n" +
          "</x>";
}
