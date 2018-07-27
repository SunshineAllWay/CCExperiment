package org.apache.solr.client.solrj;
import java.net.UnknownHostException;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
public class SolrExceptionTest extends TestCase {
  public void testSolrException() throws Throwable {
    boolean gotExpectedError = false;
    try {
      SolrServer client = new CommonsHttpSolrServer("http://localhost:11235/solr/");
      SolrQuery query = new SolrQuery("test123");
      client.query(query);
    } catch (SolrServerException sse) {
      gotExpectedError = true;
    }
    assertTrue(gotExpectedError);
  }
}
