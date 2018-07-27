package org.apache.solr.handler.admin;
import org.apache.solr.util.AbstractSolrTestCase;
public class LukeRequestHandlerTest extends AbstractSolrTestCase {
  @Override
  public String getSchemaFile() {
    return "schema12.xml";
  }
  @Override
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  public void doTestHistogramPowerOfTwoBucket() {
    assertHistoBucket(1,  1);
    assertHistoBucket(2,  2);
    assertHistoBucket(4,  3);
    assertHistoBucket(4,  4);
    assertHistoBucket(8,  5);
    assertHistoBucket(8,  6);
    assertHistoBucket(8,  7);
    assertHistoBucket(8,  8);
    assertHistoBucket(16, 9);
    final int MAX_VALID = ((Integer.MAX_VALUE/2)+1)/2;
    assertHistoBucket(MAX_VALID,   MAX_VALID-1 );
    assertHistoBucket(MAX_VALID,   MAX_VALID   );
    assertHistoBucket(MAX_VALID*2, MAX_VALID+1 );
  }
  private void assertHistoBucket(int expected, int in) {
    assertEquals("histobucket: " + in, expected,
                 LukeRequestHandler.TermHistogram.getPowerOfTwoBucket( in ));
  }
  public void testLuke() {
    doTestHistogramPowerOfTwoBucket();
    assertU(adoc("id","SOLR1000", "name","Apache Solr",
      "solr_si", "10",
      "solr_sl", "10",
      "solr_sf", "10",
      "solr_sd", "10",
      "solr_s", "10",
      "solr_sI", "10",
      "solr_sS", "10",
      "solr_t", "10",
      "solr_tt", "10",
      "solr_b", "true",
      "solr_i", "10",
      "solr_l", "10",
      "solr_f", "10",
      "solr_d", "10",
      "solr_ti", "10",
      "solr_tl", "10",
      "solr_tf", "10",
      "solr_td", "10",
      "solr_pi", "10",
      "solr_pl", "10",
      "solr_pf", "10",
      "solr_pd", "10",
      "solr_dt", "2000-01-01T01:01:01Z",
      "solr_tdt", "2000-01-01T01:01:01Z",
      "solr_pdt", "2000-01-01T01:01:01Z"
    ));
    assertU(commit());
    assertQ(req("qt","/admin/luke", "id","SOLR1000"));
  }
}
