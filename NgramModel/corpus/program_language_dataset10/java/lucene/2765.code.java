package org.apache.solr.client.solrj.util;
import junit.framework.TestCase;
public class ClientUtilsTest extends TestCase {
  public void testEscapeQuery() 
  { 
    assertEquals( "nochange", ClientUtils.escapeQueryChars( "nochange" ) );
    assertEquals( "12345", ClientUtils.escapeQueryChars( "12345" ) );
    assertEquals( "with\\ space", ClientUtils.escapeQueryChars( "with space" ) );
    assertEquals( "h\\:ello\\!", ClientUtils.escapeQueryChars( "h:ello!" ) );
    assertEquals( "h\\~\\!", ClientUtils.escapeQueryChars( "h~!" ) );
  }
}
