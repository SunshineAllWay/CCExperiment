package org.apache.solr.search.function;
import org.apache.solr.util.AbstractSolrTestCase;
public class SortByFunctionTest extends AbstractSolrTestCase {
  public String getSchemaFile() {
    return "schema.xml";
  }
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  public void test() throws Exception {
    assertU(adoc("id", "1", "x_td", "0", "y_td", "2", "w_td", "25", "z_td", "5", "f_t", "ipod"));
    assertU(adoc("id", "2", "x_td", "2", "y_td", "2", "w_td", "15", "z_td", "5", "f_t", "ipod ipod ipod ipod ipod"));
    assertU(adoc("id", "3", "x_td", "3", "y_td", "2", "w_td", "55", "z_td", "5", "f_t", "ipod ipod ipod ipod ipod ipod ipod ipod ipod"));
    assertU(adoc("id", "4", "x_td", "4", "y_td", "2", "w_td", "45", "z_td", "5", "f_t", "ipod ipod ipod ipod ipod ipod ipod"));
    assertU(commit());
    assertQ(req("fl", "*,score", "q", "*:*"),
            "//*[@numFound='4']",
            "//float[@name='score']='1.0'",
            "//result/doc[1]/int[@name='id'][.='1']",
            "//result/doc[2]/int[@name='id'][.='2']",
            "//result/doc[3]/int[@name='id'][.='3']",
            "//result/doc[4]/int[@name='id'][.='4']"
    );
    assertQ(req("fl", "*,score", "q", "*:*", "sort", "score desc"),
            "//*[@numFound='4']",
            "//float[@name='score']='1.0'",
            "//result/doc[1]/int[@name='id'][.='1']",
            "//result/doc[2]/int[@name='id'][.='2']",
            "//result/doc[3]/int[@name='id'][.='3']",
            "//result/doc[4]/int[@name='id'][.='4']"
    );
    assertQ(req("fl", "id,score", "q", "f_t:ipod", "sort", "score desc"),
            "//*[@numFound='4']",
            "//result/doc[1]/int[@name='id'][.='1']",
            "//result/doc[2]/int[@name='id'][.='4']",
            "//result/doc[3]/int[@name='id'][.='2']",
            "//result/doc[4]/int[@name='id'][.='3']"
    );
    assertQ(req("fl", "*,score", "q", "*:*", "sort", "sum(x_td, y_td) desc"),
            "//*[@numFound='4']",
            "//float[@name='score']='1.0'",
            "//result/doc[1]/int[@name='id'][.='4']",
            "//result/doc[2]/int[@name='id'][.='3']",
            "//result/doc[3]/int[@name='id'][.='2']",
            "//result/doc[4]/int[@name='id'][.='1']"
    );
    assertQ(req("fl", "*,score", "q", "*:*", "sort", "sum(x_td, y_td) asc"),
            "//*[@numFound='4']",
            "//float[@name='score']='1.0'",
            "//result/doc[1]/int[@name='id'][.='1']",
            "//result/doc[2]/int[@name='id'][.='2']",
            "//result/doc[3]/int[@name='id'][.='3']",
            "//result/doc[4]/int[@name='id'][.='4']"
    );
    assertQ(req("q", "*:*", "fl", "id", "sort", "sum(z_td, y_td) asc, w_td asc"),
            "//*[@numFound='4']",
            "//result/doc[1]/int[@name='id'][.='2']",
            "//result/doc[2]/int[@name='id'][.='1']",
            "//result/doc[3]/int[@name='id'][.='4']",
            "//result/doc[4]/int[@name='id'][.='3']"
    );
  }
}
