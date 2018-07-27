package org.apache.solr.servlet;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.util.AbstractSolrTestCase;
public class DirectSolrConnectionTest extends AbstractSolrTestCase 
{
  public String getSchemaFile() { return "solr/crazy-path-to-schema.xml"; }
  public String getSolrConfigFile() { return "solr/crazy-path-to-config.xml"; }
  DirectSolrConnection direct;
  @Override
  public void setUp() throws Exception
  {
    super.setUp();
    direct = new DirectSolrConnection(h.getCore());
  }
  public void testSimpleRequest() throws Exception 
  { 
    String pathAndParams = "/select?wt=xml&version=2.2&echoParams=explicit&q=*:*";
    String got = direct.request( pathAndParams, null );
    assertTrue( got.indexOf( "<str name=\"echoParams\">explicit</str>" ) > 5 );
    try {
      direct.request( "/path to nonexistang thingy!!", null );
      fail( "should throw an exception" );
    }
    catch( Exception ex ){}
  }
  public void testInsertThenSelect() throws Exception 
  { 
    String value = "Kittens!!! \u20AC";
    String[] cmds = new String[] {
      "<delete><id>42</id></delete>",
      "<add><doc><field name=\"id\">42</field><field name=\"subject\">"+value+"</field></doc></add>",
      "<commit/>"
    };
    String getIt = "/select?wt=xml&q=id:42";
    for( String cmd : cmds ) {
      direct.request( "/update?"+CommonParams.STREAM_BODY+"="+cmd, null );
    }
    String got = direct.request( getIt, null );
    assertTrue( got.indexOf( value ) > 0 );
    for( String cmd : cmds ) {
      direct.request( "/update", cmd );
    }
    got = direct.request( getIt, null );
    assertTrue( got.indexOf( value ) > 0 );
  }
}
