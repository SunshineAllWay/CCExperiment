package org.apache.solr;
import org.apache.solr.util.AbstractSolrTestCase;
public class EchoParamsTest extends AbstractSolrTestCase {
  public String getSchemaFile() { return "solr/crazy-path-to-schema.xml"; }
  public String getSolrConfigFile() { return "solr/crazy-path-to-config.xml"; }
  private static final String HEADER_XPATH = "/response/lst[@name='responseHeader']";
  public void testDefaultEchoParams() {
    lrf.args.put("wt", "xml");
    lrf.args.put("version", "2.2");    
    assertQ(req("foo"),HEADER_XPATH + "/int[@name='status']");
    assertQ(req("foo"),"not(//lst[@name='params'])");
  }
  public void testDefaultEchoParamsDefaultVersion() {
    lrf.args.put("wt", "xml");
    lrf.args.remove("version");    
    assertQ(req("foo"),HEADER_XPATH + "/int[@name='status']");
    assertQ(req("foo"),"not(//lst[@name='params'])");
  }
  public void testExplicitEchoParams() {
    lrf.args.put("wt", "xml");
    lrf.args.put("version", "2.2");
    lrf.args.put("echoParams", "explicit");
    assertQ(req("foo"),HEADER_XPATH + "/int[@name='status']");
    assertQ(req("foo"),HEADER_XPATH + "/lst[@name='params']");
    assertQ(req("foo"),HEADER_XPATH + "/lst[@name='params']/str[@name='wt'][.='xml']");
  }
  public void testAllEchoParams() {
    lrf = h.getRequestFactory
      ("crazy_custom_qt", 0, 20,
       "version","2.2",
       "wt","xml",
       "echoParams", "all",
       "echoHandler","true"
       );
    assertQ(req("foo"),HEADER_XPATH + "/lst[@name='params']/str[@name='fl'][.='implicit']");
    assertQ(req("foo"),HEADER_XPATH + "/str[@name='handler'][.='org.apache.solr.handler.StandardRequestHandler']");
  }
}
