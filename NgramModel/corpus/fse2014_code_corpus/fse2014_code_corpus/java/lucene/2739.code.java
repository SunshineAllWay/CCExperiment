package org.apache.solr.client.solrj;
import org.apache.solr.util.AbstractSolrTestCase;
abstract public class SolrExampleTestBase extends AbstractSolrTestCase 
{
  public String getSolrHome() { return "../../../example/solr/"; }
  @Override public String getSchemaFile()     { return getSolrHome()+"conf/schema.xml";     }
  @Override public String getSolrConfigFile() { return getSolrHome()+"conf/solrconfig.xml"; }
  @Override
  public void setUp() throws Exception
  {
    super.setUp();
    System.setProperty( "solr.solr.home", this.getSolrHome() ); 
    System.setProperty( "solr.data.dir", "./solr/data" ); 
  }
  protected abstract SolrServer getSolrServer();
  protected abstract SolrServer createNewSolrServer();
}
