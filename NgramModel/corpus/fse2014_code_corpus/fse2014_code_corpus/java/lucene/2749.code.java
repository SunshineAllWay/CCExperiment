package org.apache.solr.client.solrj.embedded;
import org.apache.solr.client.solrj.LargeVolumeTestBase;
import org.apache.solr.client.solrj.SolrServer;
public class LargeVolumeEmbeddedTest extends LargeVolumeTestBase {
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
