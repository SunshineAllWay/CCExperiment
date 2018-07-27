package org.apache.solr.highlight;
import java.util.HashMap;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.util.TestHarness;
public class FastVectorHighlighterTest extends AbstractSolrTestCase {
  @Override public String getSchemaFile() { return "schema.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig.xml"; }
  public void testConfig(){
    SolrHighlighter highlighter = h.getCore().getHighlighter();
    SolrFragListBuilder solrFlbNull = highlighter.fragListBuilders.get( null );
    SolrFragListBuilder solrFlbEmpty = highlighter.fragListBuilders.get( "" );
    SolrFragListBuilder solrFlbSimple = highlighter.fragListBuilders.get( "simple" );
    assertSame( solrFlbNull, solrFlbEmpty );
    assertTrue( solrFlbNull instanceof SimpleFragListBuilder );
    assertTrue( solrFlbSimple instanceof SimpleFragListBuilder );
    SolrFragmentsBuilder solrFbNull = highlighter.fragmentsBuilders.get( null );
    SolrFragmentsBuilder solrFbEmpty = highlighter.fragmentsBuilders.get( "" );
    SolrFragmentsBuilder solrFbColored = highlighter.fragmentsBuilders.get( "colored" );
    SolrFragmentsBuilder solrFbSO = highlighter.fragmentsBuilders.get( "scoreOrder" );
    assertSame( solrFbNull, solrFbEmpty );
    assertTrue( solrFbNull instanceof ScoreOrderFragmentsBuilder );
    assertTrue( solrFbColored instanceof MultiColoredScoreOrderFragmentsBuilder );
    assertTrue( solrFbSO instanceof ScoreOrderFragmentsBuilder );
  }
  public void test() {
    HashMap<String,String> args = new HashMap<String,String>();
    args.put("hl", "true");
    args.put("hl.fl", "tv_text");
    args.put("hl.snippets", "2");
    args.put("hl.useFastVectorHighlighter", "true");
    TestHarness.LocalRequestFactory sumLRF = h.getRequestFactory(
      "standard",0,200,args);
    assertU(adoc("tv_text", "basic fast vector highlighter test", 
                 "id", "1"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
            sumLRF.makeRequest("tv_text:vector"),
            "//lst[@name='highlighting']/lst[@name='1']",
            "//lst[@name='1']/arr[@name='tv_text']/str[.=' fast <b>vector</b> highlighter test']"
            );
  }
}
