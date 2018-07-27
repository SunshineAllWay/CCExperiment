package org.apache.solr.client.solrj.embedded;
import org.apache.solr.client.solrj.SolrExampleTests;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.SchemaField;
import org.junit.Assert;
public class SolrExampleJettyTest extends SolrExampleTests {
  SolrServer server;
  JettySolrRunner jetty;
  int port = 0;
  static final String context = "/example";
  @Override public void setUp() throws Exception 
  {
    super.setUp();
    jetty = new JettySolrRunner( context, 0 );
    jetty.start();
    port = jetty.getLocalPort();
    log.info("Assigned Port#" + port);
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
  public void testBadSetup()
  {
    try {
      String url = "http://localhost/?core=xxx";
      CommonsHttpSolrServer s = new CommonsHttpSolrServer( url );
      Assert.fail( "CommonsHttpSolrServer should not allow a path with a parameter: "+s.getBaseURL() );
    }
    catch( Exception ex ) {
    }
  }
}
