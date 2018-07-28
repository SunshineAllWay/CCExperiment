package org.apache.solr.core;
import org.apache.solr.handler.StandardRequestHandler;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.util.AbstractSolrTestCase;
public class RequestHandlersTest extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema.xml"; }
  public String getSolrConfigFile() { return "solrconfig.xml"; }
  public void testLazyLoading() {
    SolrCore core = h.getCore();
    SolrRequestHandler handler = core.getRequestHandler( "lazy" );
    assertFalse( handler instanceof StandardRequestHandler ); 
    assertU(adoc("id", "42",
                 "name", "Zapp Brannigan"));
    assertU(adoc("id", "43",
                 "title", "Democratic Order of Planets"));
    assertU(adoc("id", "44",
                 "name", "The Zapper"));
    assertU(adoc("id", "45",
                 "title", "25 star General"));
    assertU(adoc("id", "46",
                 "subject", "Defeated the pacifists of the Gandhi nebula"));
    assertU(adoc("id", "47",
                 "text", "line up and fly directly at the enemy death cannons, clogging them with wreckage!"));
    assertU(commit());
    assertQ("lazy request handler returns all matches",
            req("id:[42 TO 47]"),
            "*[count(//doc)=6]"
            );
    assertQ("lazy handler returns fewer matches",
            req("q", "id:[42 TO 47]",   "qt","defaults"),
            "*[count(//doc)=4]"
            );
    assertQ("lazy handler includes highlighting",
            req("q", "name:Zapp OR title:General",   "qt","defaults"),
            "//lst[@name='highlighting']"
            );
  }
  public void testPathNormalization()
  {
    SolrCore core = h.getCore();
    SolrRequestHandler h1 = core.getRequestHandler("/update/csv" );
    assertNotNull( h1 );
    SolrRequestHandler h2 = core.getRequestHandler("/update/csv/" );
    assertNotNull( h2 );
    assertEquals( h1, h2 ); 
    assertNull( core.getRequestHandler("/update/csv/asdgadsgas" ) ); 
  }
}
