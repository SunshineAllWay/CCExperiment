package org.apache.solr.schema;
import java.util.HashMap;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.lucene.search.Similarity;
public class IndexSchemaTest extends AbstractSolrTestCase {
  @Override public String getSchemaFile() { return "schema.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig.xml"; }
  @Override 
  public void setUp() throws Exception {
    super.setUp();
  }
  @Override 
  public void tearDown() throws Exception {
    super.tearDown();
  }
  public void testDynamicCopy() 
  {
    SolrCore core = h.getCore();
    assertU(adoc("id", "10", "title", "test", "aaa_dynamic", "aaa"));
    assertU(commit());
    Map<String,String> args = new HashMap<String, String>();
    args.put( CommonParams.Q, "title:test" );
    args.put( "indent", "true" );
    SolrQueryRequest req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
    assertQ("Make sure they got in", req
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/int[@name='id'][.='10']"
            );
    args = new HashMap<String, String>();
    args.put( CommonParams.Q, "aaa_dynamic:aaa" );
    args.put( "indent", "true" );
    req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
    assertQ("dynamic source", req
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/int[@name='id'][.='10']"
            );
    args = new HashMap<String, String>();
    args.put( CommonParams.Q, "dynamic_aaa:aaa" );
    args.put( "indent", "true" );
    req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
    assertQ("dynamic destination", req
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/int[@name='id'][.='10']"
            );
  }
  public void testSimilarityFactory() {
    SolrCore core = h.getCore();
    Similarity similarity = core.getSchema().getSimilarity();
    assertTrue("wrong class", similarity instanceof MockConfigurableSimilarity);
    assertEquals("is there an echo?", ((MockConfigurableSimilarity)similarity).getPassthrough());
  }
  public void testRuntimeFieldCreation()
  {
    SolrCore core = h.getCore();
    IndexSchema schema = core.getSchema();
    final String fieldName = "runtimefield";
    SchemaField sf = new SchemaField( fieldName, schema.getFieldTypes().get( "string" ) );
    schema.getFields().put( fieldName, sf );
    schema.registerCopyField( fieldName, "dynamic_runtime" );
    schema.refreshAnalyzers();
    assertU(adoc("id", "10", "title", "test", fieldName, "aaa"));
    assertU(commit());
    SolrQuery query = new SolrQuery( fieldName+":aaa" );
    query.set( "indent", "true" );
    SolrQueryRequest req = new LocalSolrQueryRequest( core, query );
    assertQ("Make sure they got in", req
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/int[@name='id'][.='10']"
            );
    query.setQuery( "dynamic_runtime:aaa" );
    assertQ("Make sure they got in", req
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/int[@name='id'][.='10']"
            );
  }
  public void testIsDynamicField() throws Exception {
    SolrCore core = h.getCore();
    IndexSchema schema = core.getSchema();
    assertFalse( schema.isDynamicField( "id" ) );
    assertTrue( schema.isDynamicField( "aaa_i" ) );
    assertFalse( schema.isDynamicField( "no_such_field" ) );
  }
  public void testProperties() throws Exception{
    SolrCore core = h.getCore();
    IndexSchema schema = core.getSchema();
    assertFalse(schema.getField("id").multiValued());
  }
}
