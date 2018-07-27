package org.apache.solr.schema;
import java.util.LinkedList;
import java.util.List;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.AbstractSolrTestCase;
public class BadIndexSchemaTest extends AbstractSolrTestCase {
  @Override public String getSchemaFile() { return "bad-schema.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig.xml"; }
  @Override 
  public void setUp() throws Exception {
    super.setUp();
  }
  @Override 
  public void tearDown() throws Exception {
    super.tearDown();
  }
  private Throwable findErrorWithSubstring( List<Throwable> err, String v )
  {
    for( Throwable t : err ) {
      if( t.getMessage().indexOf( v ) > 0 ) {
        return t;
      }
    }
    return null;
  }
  public void testSevereErrorsForDuplicateNames() 
  {
    SolrCore core = h.getCore();
    IndexSchema schema = core.getSchema();
    for( Throwable t : SolrConfig.severeErrors ) {
      log.error( "ERROR:"+t.getMessage() );
    }
    assertEquals( 3, SolrConfig.severeErrors.size() );
    List<Throwable> err = new LinkedList<Throwable>();
    err.addAll( SolrConfig.severeErrors );
    Throwable t = findErrorWithSubstring( err, "*_twice" );
    assertNotNull( t );
    err.remove( t );
    t = findErrorWithSubstring( err, "ftAgain" );
    assertNotNull( t );
    err.remove( t );
    t = findErrorWithSubstring( err, "fAgain" );
    assertNotNull( t );
    err.remove( t );
    assertTrue( err.isEmpty() );
  }
}
