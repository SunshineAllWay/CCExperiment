package org.apache.solr.handler;
import java.util.HashMap;
import java.util.Map;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.util.AbstractSolrTestCase;
public class StandardRequestHandlerTest extends AbstractSolrTestCase {
  @Override public String getSchemaFile() { return "schema.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig.xml"; }
  @Override public void setUp() throws Exception {
    super.setUp();
    lrf = h.getRequestFactory("standard", 0, 20 );
  }
  public void testSorting() throws Exception {
    SolrCore core = h.getCore();
    assertU(adoc("id", "10", "title", "test", "val_s", "aaa"));
    assertU(adoc("id", "11", "title", "test", "val_s", "bbb"));
    assertU(adoc("id", "12", "title", "test", "val_s", "ccc"));
    assertU(commit());
    Map<String,String> args = new HashMap<String, String>();
    args.put( CommonParams.Q, "title:test" );
    args.put( "indent", "true" );
    SolrQueryRequest req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
    assertQ("Make sure they got in", req
            ,"//*[@numFound='3']"
            );
    args.put( CommonParams.SORT, "val_s asc" );
    assertQ("with sort param [asc]", req
            ,"//*[@numFound='3']"
            ,"//result/doc[1]/int[@name='id'][.='10']"
            ,"//result/doc[2]/int[@name='id'][.='11']"
            ,"//result/doc[3]/int[@name='id'][.='12']"
            );
    args.put( CommonParams.SORT, "val_s desc" );
    assertQ("with sort param [desc]", req
            ,"//*[@numFound='3']"
            ,"//result/doc[1]/int[@name='id'][.='12']"
            ,"//result/doc[2]/int[@name='id'][.='11']"
            ,"//result/doc[3]/int[@name='id'][.='10']"
            );
    args.put( CommonParams.SORT, "score desc" );
    assertQ("with sort param [desc]", req,"//*[@numFound='3']" );
    args.put( CommonParams.SORT, "score asc" );
    assertQ("with sort param [desc]", req,"//*[@numFound='3']" );
    args.remove( CommonParams.SORT );
    args.put( QueryParsing.DEFTYPE, "lucenePlusSort" );
    args.put( CommonParams.Q, "title:test; val_s desc" );
    assertQ("with sort param [desc]", req
            ,"//*[@numFound='3']"
            ,"//result/doc[1]/int[@name='id'][.='12']"
            ,"//result/doc[2]/int[@name='id'][.='11']"
            ,"//result/doc[3]/int[@name='id'][.='10']"
            );
    args.put( CommonParams.Q, "title:test; val_s asc" );
    assertQ("with sort param [desc]", req
            ,"//*[@numFound='3']"
            ,"//result/doc[1]/int[@name='id'][.='10']"
            ,"//result/doc[2]/int[@name='id'][.='11']"
            ,"//result/doc[3]/int[@name='id'][.='12']"
            );
  }
}
