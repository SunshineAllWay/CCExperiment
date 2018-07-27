package org.apache.solr.client.solrj;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.common.SolrInputDocument;
import java.util.Iterator;
import java.io.IOException;
public class TestBatchUpdate extends SolrExampleTestBase {
  static final int numdocs = 1000;
  SolrServer server;
  JettySolrRunner jetty;
  int port = 0;
  static final String context = "/example";
  public void testWithXml() throws Exception {
    CommonsHttpSolrServer commonsHttpSolrServer = (CommonsHttpSolrServer) getSolrServer();
    commonsHttpSolrServer.setRequestWriter(new RequestWriter());
    commonsHttpSolrServer.deleteByQuery( "*:*" ); 
    doIt(commonsHttpSolrServer);
  }
  public void testWithBinary()throws Exception{
    CommonsHttpSolrServer commonsHttpSolrServer = (CommonsHttpSolrServer) getSolrServer();
    commonsHttpSolrServer.setRequestWriter(new BinaryRequestWriter());
    commonsHttpSolrServer.deleteByQuery( "*:*" ); 
    doIt(commonsHttpSolrServer);
  }
  public void testWithBinaryBean()throws Exception{
    CommonsHttpSolrServer commonsHttpSolrServer = (CommonsHttpSolrServer) getSolrServer();
    commonsHttpSolrServer.setRequestWriter(new BinaryRequestWriter());
    commonsHttpSolrServer.deleteByQuery( "*:*" ); 
    final int[] counter = new int[1];
    counter[0] = 0;
    commonsHttpSolrServer.addBeans(new Iterator<Bean>() {
      public boolean hasNext() {
        return counter[0] < numdocs;
      }
      public Bean next() {
        Bean bean = new Bean();
        bean.id = "" + (++counter[0]);
        bean.cat = "foocat";
        return bean;
      }
      public void remove() {
      }
    });
    commonsHttpSolrServer.commit();
    SolrQuery query = new SolrQuery("*:*");
    QueryResponse response = commonsHttpSolrServer.query(query);
    assertEquals(0, response.getStatus());
    assertEquals(numdocs, response.getResults().getNumFound());
  }
  public static class Bean{
    @Field
    String id;
    @Field
    String cat;
  }
  private void doIt(CommonsHttpSolrServer commonsHttpSolrServer) throws SolrServerException, IOException {
    final int[] counter = new int[1];
    counter[0] = 0;
    commonsHttpSolrServer.add(new Iterator<SolrInputDocument>() {
      public boolean hasNext() {
        return counter[0] < numdocs;
      }
      public SolrInputDocument next() {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", "" + (++counter[0]));
        doc.addField("cat", "foocat");
        return doc;
      }
      public void remove() {
      }
    });
    commonsHttpSolrServer.commit();
    SolrQuery query = new SolrQuery("*:*");
    QueryResponse response = commonsHttpSolrServer.query(query);
    assertEquals(0, response.getStatus());
    assertEquals(numdocs, response.getResults().getNumFound());
  }
  @Override public void setUp() throws Exception
  {
    super.setUp();
    jetty = new JettySolrRunner( context, 0 );
    jetty.start();
    port = jetty.getLocalPort();
    server = this.createNewSolrServer();
  }
  @Override public void tearDown() throws Exception
  {
    super.tearDown();
    jetty.stop();  
  }
  @Override
  protected SolrServer getSolrServer()
  {
    return server;
  }
  @Override
  protected SolrServer createNewSolrServer()
  {
    try {
      String url = "http://localhost:"+port+context;
      CommonsHttpSolrServer s = new CommonsHttpSolrServer( url );
      s.setConnectionTimeout(100); 
      s.setDefaultMaxConnectionsPerHost(100);
      s.setMaxTotalConnections(100);
      return s;
    }
    catch( Exception ex ) {
      throw new RuntimeException( ex );
    }
  }
}
