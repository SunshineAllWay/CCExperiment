package org.apache.solr.client.solrj;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest.ACTION;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
public abstract class MultiCoreExampleTestBase extends SolrExampleTestBase 
{
  protected static CoreContainer cores;
  @Override public String getSolrHome() { return "../../../example/multicore/"; }
  @Override public String getSchemaFile()     { return getSolrHome()+"core0/conf/schema.xml";     }
  @Override public String getSolrConfigFile() { return getSolrHome()+"core0/conf/solrconfig.xml"; }
  @Override public void setUp() throws Exception {
    super.setUp();
    cores = h.getCoreContainer();
    SolrCore.log.info("CORES=" + cores + " : " + cores.getCoreNames());
    cores.setPersistent(false);
  }
  @Override
  protected final SolrServer getSolrServer()
  {
    throw new UnsupportedOperationException();
  }
  @Override
  protected final SolrServer createNewSolrServer()
  {
    throw new UnsupportedOperationException();
  }
  protected abstract SolrServer getSolrCore0();
  protected abstract SolrServer getSolrCore1();
  protected abstract SolrServer getSolrAdmin();
  protected abstract SolrServer getSolrCore(String name);
  public void testMultiCore() throws Exception
  {
    UpdateRequest up = new UpdateRequest();
    up.setAction( ACTION.COMMIT, true, true );
    up.deleteByQuery( "*:*" );
    up.process( getSolrCore0() );
    up.process( getSolrCore1() );
    up.clear();
    SolrInputDocument doc = new SolrInputDocument();
    doc.setField( "id", "AAA" );
    doc.setField( "core0", "yup" );
    up.add( doc );
    up.process( getSolrCore0() );
    try {
      up.process( getSolrCore1() );
      fail( "Can't add core0 field to core1!" );
    }
    catch( Exception ex ) {}
    doc.setField( "id", "BBB" );
    doc.setField( "core1", "yup" );
    doc.removeField( "core0" );
    up.add( doc );
    up.process( getSolrCore1() );
    try {
      up.process( getSolrCore0() );
      fail( "Can't add core1 field to core0!" );
    }
    catch( Exception ex ) {}
    SolrQuery q = new SolrQuery();
    QueryRequest r = new QueryRequest( q );
    q.setQuery( "id:AAA" );
    assertEquals( 1, r.process( getSolrCore0() ).getResults().size() );
    assertEquals( 0, r.process( getSolrCore1() ).getResults().size() );
    assertEquals( 1, getSolrCore0().query( new SolrQuery( "id:AAA" ) ).getResults().size() );
    assertEquals( 0, getSolrCore0().query( new SolrQuery( "id:BBB" ) ).getResults().size() );
    assertEquals( 0, getSolrCore1().query( new SolrQuery( "id:AAA" ) ).getResults().size() );
    assertEquals( 1, getSolrCore1().query( new SolrQuery( "id:BBB" ) ).getResults().size() );
    String name = "core0";
    SolrServer coreadmin = getSolrAdmin();
    CoreAdminResponse mcr = CoreAdminRequest.getStatus( name, coreadmin );
    long before = mcr.getStartTime( name ).getTime();
    CoreAdminRequest.reloadCore( name, coreadmin );
    mcr = CoreAdminRequest.getStatus( name, coreadmin );
    long after = mcr.getStartTime( name ).getTime();
    assertTrue( "should have more recent time: "+after+","+before, after > before );
    CoreAdminRequest.renameCore("core1","corea",coreadmin);
    CoreAdminRequest.renameCore("corea","coreb",coreadmin);
    CoreAdminRequest.renameCore("coreb","corec",coreadmin);
    CoreAdminRequest.renameCore("corec","cored",coreadmin);
    CoreAdminRequest.renameCore("cored","corefoo",coreadmin);
    try {
      getSolrCore("core1").query( new SolrQuery( "id:BBB" ) );
      fail( "core1 should be gone" );
    }
    catch( Exception ex ) {}
    assertEquals( 1, getSolrCore("corefoo").query( new SolrQuery( "id:BBB" ) ).getResults().size() );
  }
}
