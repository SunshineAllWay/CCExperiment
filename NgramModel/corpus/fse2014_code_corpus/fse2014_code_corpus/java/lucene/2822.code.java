package org.apache.solr.highlight;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.util.TestHarness;
import java.util.HashMap;
public class HighlighterConfigTest extends AbstractSolrTestCase {
	  @Override public String getSchemaFile() { return "schema.xml"; }
	  @Override public String getSolrConfigFile() { return "solrconfig-highlight.xml"; }
	  @Override 
	  public void setUp() throws Exception {
	    super.setUp();
	  }
	  @Override 
	  public void tearDown() throws Exception {
	    super.tearDown();
	  }
	  public void testConfig()
	  {
	    SolrHighlighter highlighter = h.getCore().getHighlighter();
	    log.info( "highlighter" );
	    assertTrue( highlighter instanceof DummyHighlighter );
	    HashMap<String,String> args = new HashMap<String,String>();
	    args.put("hl", "true");
	    args.put("df", "t_text");
	    args.put("hl.fl", "");
	    TestHarness.LocalRequestFactory sumLRF = h.getRequestFactory(
	      "standard", 0, 200, args);
	    assertU(adoc("t_text", "a long day's night", "id", "1"));
	    assertU(commit());
	    assertU(optimize());
	    assertQ("Basic summarization",
	            sumLRF.makeRequest("long"),
	            "//lst[@name='highlighting']/str[@name='dummy']"
	            );
	  }
}
