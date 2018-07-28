package org.apache.solr.client.solrj.response;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import junit.framework.Assert;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.util.NamedList;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
public class QueryResponseTest {
  @Test
  public void testDateFacets() throws Exception   {
    XMLResponseParser parser = new XMLResponseParser();
    FileReader in = new FileReader("sampleDateFacetResponse.xml");
    assertTrue("in is null and it shouldn't be", in != null);
    NamedList<Object> response = parser.processResponse(in);
    in.close();
    QueryResponse qr = new QueryResponse(response, null);
    Assert.assertNotNull(qr);
    Assert.assertNotNull(qr.getFacetDates());
    for (FacetField f : qr.getFacetDates()) {
      Assert.assertNotNull(f);
    }
  }
}
