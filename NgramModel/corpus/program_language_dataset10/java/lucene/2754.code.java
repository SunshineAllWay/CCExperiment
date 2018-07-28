package org.apache.solr.client.solrj.embedded;
import org.apache.solr.client.solrj.SolrExampleTests;
import org.apache.solr.client.solrj.SolrServer;
public class SolrExampleEmbeddedTest extends SolrExampleTests {
  SolrServer server;
  @Override public void setUp() throws Exception 
  {
    super.setUp();
    server = createNewSolrServer();
  }
  @Override
  protected SolrServer getSolrServer()
  {
    return server;
  }
  @Override
  protected SolrServer createNewSolrServer()
  {
    return new EmbeddedSolrServer( h.getCoreContainer(), "" );
  }
}
