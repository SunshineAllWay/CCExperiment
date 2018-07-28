package org.apache.solr.request;
import org.apache.solr.util.AbstractSolrTestCase;
public class SimpleFacetsLegacySortTest extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema.xml"; }
  public String getSolrConfigFile() { return "solrconfig-facet-sort.xml"; }
  public String getCoreName() { return "basic"; }
  public void testFacetSortLegacy() {
    String f = "t_s1";
    String pre = "//lst[@name='"+f+"']";
    assertU(adoc("id", "1",  f, "A"));
    assertU(adoc("id", "2",  f, "B"));
    assertU(adoc("id", "3",  f, "C"));
    assertU(adoc("id", "4",  f, "C"));
    assertU(adoc("id", "5",  f, "D"));
    assertU(adoc("id", "6",  f, "E"));
    assertU(adoc("id", "7",  f, "E"));
    assertU(adoc("id", "8",  f, "E"));
    assertU(adoc("id", "9",  f, "F"));
    assertU(adoc("id", "10", f, "G"));
    assertU(adoc("id", "11", f, "G"));
    assertU(adoc("id", "12", f, "G"));
    assertU(adoc("id", "13", f, "G"));
    assertU(adoc("id", "14", f, "G"));
    assertU(commit());
    assertQ("check for facet.sort=true",
            req("q", "id:[* TO *]"
               ,"facet", "true"
               ,"facet.field", f
               ,"facet.sort", "true"
               )
            ,"*[count(//lst[@name='facet_fields']/lst/int)=7]"
            ,pre+"/int[1][@name='G'][.='5']"
            ,pre+"/int[2][@name='E'][.='3']"
            ,pre+"/int[3][@name='C'][.='2']"
            ,pre+"/int[4][@name='A'][.='1']"
            ,pre+"/int[5][@name='B'][.='1']"
            ,pre+"/int[6][@name='D'][.='1']"
            ,pre+"/int[7][@name='F'][.='1']"
            );
    assertQ("check for facet.sort=false",
            req("q", "id:[* TO *]"
               ,"facet", "true"
               ,"facet.field", f
               ,"facet.sort", "false"
               )
            ,"*[count(//lst[@name='facet_fields']/lst/int)=7]"
            ,pre+"/int[1][@name='A'][.='1']"
            ,pre+"/int[2][@name='B'][.='1']"
            ,pre+"/int[3][@name='C'][.='2']"
            ,pre+"/int[4][@name='D'][.='1']"
            ,pre+"/int[5][@name='E'][.='3']"
            ,pre+"/int[6][@name='F'][.='1']"
            ,pre+"/int[7][@name='G'][.='5']"
            );
    assertQ("check for solrconfig default (false)",
            req("q", "id:[* TO *]"
               ,"facet", "true"
               ,"facet.field", f
               )
            ,"*[count(//lst[@name='facet_fields']/lst/int)=7]"
            ,pre+"/int[1][@name='A'][.='1']"
            ,pre+"/int[2][@name='B'][.='1']"
            ,pre+"/int[3][@name='C'][.='2']"
            ,pre+"/int[4][@name='D'][.='1']"
            ,pre+"/int[5][@name='E'][.='3']"
            ,pre+"/int[6][@name='F'][.='1']"
            ,pre+"/int[7][@name='G'][.='5']"
            );
  }
}
