package org.apache.solr.client.solrj;
import java.io.IOException;
import java.io.StringWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.Assert;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.request.SolrPing;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.XML;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.params.FacetParams;
abstract public class SolrExampleTests extends SolrExampleTestBase
{
  public void testExampleConfig() throws Exception
  {    
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    SolrInputDocument doc = new SolrInputDocument();
    String docID = "1112211111";
    doc.addField( "id", docID, 1.0f );
    doc.addField( "name", "my name!", 1.0f );
    Assert.assertEquals( null, doc.getField("foo") );
    Assert.assertTrue(doc.getField("name").getValue() != null );
    UpdateResponse upres = server.add( doc ); 
    Assert.assertEquals(0, upres.getStatus());
    upres = server.commit( true, true );
    Assert.assertEquals(0, upres.getStatus());
    upres = server.optimize( true, true );
    Assert.assertEquals(0, upres.getStatus());
    SolrQuery query = new SolrQuery();
    query.setQuery( "id:"+docID );
    QueryResponse response = server.query( query );
    Assert.assertEquals(docID, response.getResults().get(0).getFieldValue("id") );
    List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
    SolrInputDocument doc2 = new SolrInputDocument();
    doc2.addField( "id", "2", 1.0f );
    doc2.addField( "inStock", true, 1.0f );
    doc2.addField( "price", 2, 1.0f );
    doc2.addField( "timestamp_dt", new java.util.Date(), 1.0f );
    docs.add(doc2);
    SolrInputDocument doc3 = new SolrInputDocument();
    doc3.addField( "id", "3", 1.0f );
    doc3.addField( "inStock", false, 1.0f );
    doc3.addField( "price", 3, 1.0f );
    doc3.addField( "timestamp_dt", new java.util.Date(), 1.0f );
    docs.add(doc3);
    SolrInputDocument doc4 = new SolrInputDocument();
    doc4.addField( "id", "4", 1.0f );
    doc4.addField( "inStock", true, 1.0f );
    doc4.addField( "price", 4, 1.0f );
    doc4.addField( "timestamp_dt", new java.util.Date(), 1.0f );
    docs.add(doc4);
    SolrInputDocument doc5 = new SolrInputDocument();
    doc5.addField( "id", "5", 1.0f );
    doc5.addField( "inStock", false, 1.0f );
    doc5.addField( "price", 5, 1.0f );
    doc5.addField( "timestamp_dt", new java.util.Date(), 1.0f );
    docs.add(doc5);
    upres = server.add( docs ); 
    Assert.assertEquals(0, upres.getStatus());
    upres = server.commit( true, true );
    Assert.assertEquals(0, upres.getStatus());
    upres = server.optimize( true, true );
    Assert.assertEquals(0, upres.getStatus());
    query = new SolrQuery("*:*");
    query.addFacetQuery("price:[* TO 2]");
    query.addFacetQuery("price:[2 TO 4]");
    query.addFacetQuery("price:[5 TO *]");
    query.addFacetField("inStock");
    query.addFacetField("price");
    query.addFacetField("timestamp_dt");
    query.removeFilterQuery("inStock:true");
    response = server.query( query );
    Assert.assertEquals(0, response.getStatus());
    Assert.assertEquals(5, response.getResults().getNumFound() );
    Assert.assertEquals(3, response.getFacetQuery().size());    
    Assert.assertEquals(2, response.getFacetField("inStock").getValueCount());
    Assert.assertEquals(4, response.getFacetField("price").getValueCount());
    SolrQuery query2 = query.getCopy();
    query2.addFilterQuery("inStock:true");
    response = server.query( query2 );
    Assert.assertEquals(1, query2.getFilterQueries().length);
    Assert.assertEquals(0, response.getStatus());
    Assert.assertEquals(2, response.getResults().getNumFound() );
    Assert.assertFalse(query.getFilterQueries() == query2.getFilterQueries());
  }
  public void testAddRetrieve() throws Exception
  {    
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    SolrInputDocument doc1 = new SolrInputDocument();
    doc1.addField( "id", "id1", 1.0f );
    doc1.addField( "name", "doc1", 1.0f );
    doc1.addField( "price", 10 );
    SolrInputDocument doc2 = new SolrInputDocument();
    doc2.addField( "id", "id2", 1.0f );
    doc2.addField( "name", "h\u1234llo", 1.0f );
    doc2.addField( "price", 20 );
    Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
    docs.add( doc1 );
    docs.add( doc2 );
    server.add( docs );
    server.commit();
    SolrQuery query = new SolrQuery();
    query.setQuery( "*:*" );
    query.addSortField( "price", SolrQuery.ORDER.asc );
    QueryResponse rsp = server.query( query );
    assertEquals( 2, rsp.getResults().getNumFound() );
    server.add( docs );
    server.commit();
    rsp = server.query( query );
    assertEquals( 2, rsp.getResults().getNumFound() );
    query.setQuery("name:h\u1234llo");
    rsp = server.query( query );
    assertEquals( 1, rsp.getResults().getNumFound() );
  }
  public void testCommitWithin() throws Exception
  {    
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    server.commit();
    QueryResponse rsp = server.query( new SolrQuery( "*:*") );
    Assert.assertEquals( 0, rsp.getResults().getNumFound() );
    SolrInputDocument doc3 = new SolrInputDocument();
    doc3.addField( "id", "id3", 1.0f );
    doc3.addField( "name", "doc3", 1.0f );
    doc3.addField( "price", 10 );
    UpdateRequest up = new UpdateRequest();
    up.add( doc3 );
    up.setCommitWithin( 500 );  
    up.process( server );
    rsp = server.query( new SolrQuery( "*:*") );
    Assert.assertEquals( 0, rsp.getResults().getNumFound() );
    Thread.sleep( 1000 ); 
    rsp = server.query( new SolrQuery( "id:id3") );
    if(rsp.getResults().getNumFound() == 0) {
      Thread.sleep( 2000 ); 
      rsp = server.query( new SolrQuery( "id:id3") );
    }
    Assert.assertEquals( 1, rsp.getResults().getNumFound() );
  }
  public void testContentStreamRequest() throws Exception {
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    server.commit();
    QueryResponse rsp = server.query( new SolrQuery( "*:*") );
    Assert.assertEquals( 0, rsp.getResults().getNumFound() );
    ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/csv");
    up.addFile(new File("books.csv"));
    up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
    NamedList<Object> result = server.request(up);
    assertNotNull("Couldn't upload books.csv", result);
    rsp = server.query( new SolrQuery( "*:*") );
    Assert.assertEquals( 10, rsp.getResults().getNumFound() );
    server.deleteByQuery( "*:*" );
    server.commit();
    rsp = server.query( new SolrQuery( "*:*") );
    Assert.assertEquals( 0, rsp.getResults().getNumFound() );
    up = new ContentStreamUpdateRequest("/update/extract");
    up.addFile(new File("mailing_lists.pdf"));
    up.setParam("literal.id", "mailing_lists.pdf");
    up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
    result = server.request(up);
    assertNotNull("Couldn't upload mailing_lists.pdf", result);
    rsp = server.query( new SolrQuery( "*:*") );
    Assert.assertEquals( 1, rsp.getResults().getNumFound() );
  }
  protected void assertNumFound( String query, int num ) throws SolrServerException, IOException
  {
    QueryResponse rsp = getSolrServer().query( new SolrQuery( query ) );
    if( num != rsp.getResults().getNumFound() ) {
      fail( "expected: "+num +" but had: "+rsp.getResults().getNumFound() + " :: " + rsp.getResults() );
    }
  }
  public void testAddDelete() throws Exception
  {    
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    SolrInputDocument[] doc = new SolrInputDocument[3];
    for( int i=0; i<3; i++ ) {
      doc[i] = new SolrInputDocument();
      doc[i].setField( "id", i + " & 222", 1.0f );
    }
    String id = (String) doc[0].getField( "id" ).getFirstValue();
    server.add( doc[0] );
    server.commit();
    assertNumFound( "*:*", 1 ); 
    server.deleteById( id );
    server.commit();
    assertNumFound( "*:*", 0 ); 
    server.add( doc[0] );
    server.commit();
    assertNumFound( "*:*", 1 ); 
    server.deleteByQuery( "id:\""+ClientUtils.escapeQueryChars(id)+"\"" );
    server.commit();
    assertNumFound( "*:*", 0 ); 
    for( SolrInputDocument d : doc ) {
      server.add( d );
    }
    server.commit();
    assertNumFound( "*:*", 3 ); 
    StringWriter xml = new StringWriter();
    xml.append( "<delete>" );
    for( SolrInputDocument d : doc ) {
      xml.append( "<id>" );
      XML.escapeCharData( (String)d.getField( "id" ).getFirstValue(), xml );
      xml.append( "</id>" );
    }
    xml.append( "</delete>" );
    DirectXmlRequest up = new DirectXmlRequest( "/update", xml.toString() );
    server.request( up );
    server.commit();
    assertNumFound( "*:*", 0 ); 
  }
  public void testLukeHandler() throws Exception
  {    
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    SolrInputDocument[] doc = new SolrInputDocument[5];
    for( int i=0; i<doc.length; i++ ) {
      doc[i] = new SolrInputDocument();
      doc[i].setField( "id", "ID"+i, 1.0f );
      server.add( doc[i] );
    }
    server.commit();
    assertNumFound( "*:*", doc.length ); 
    LukeRequest luke = new LukeRequest();
    luke.setShowSchema( false );
    LukeResponse rsp = luke.process( server );
    assertNull( rsp.getFieldTypeInfo() ); 
    luke.setShowSchema( true );
    rsp = luke.process( server );
    assertNotNull( rsp.getFieldTypeInfo() ); 
  }
  public void testStatistics() throws Exception
  {    
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    server.commit();
    assertNumFound( "*:*", 0 ); 
    String f = "val_pi";
    int i=0;               
    int[] nums = new int[] { 23, 26, 38, 46, 55, 63, 77, 84, 92, 94 };
    for( int num : nums ) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", "doc"+i++ );
      doc.setField( "name", "doc: "+num );
      doc.setField( f, num );
      server.add( doc );
    }
    server.commit();
    assertNumFound( "*:*", nums.length ); 
    SolrQuery query = new SolrQuery( "*:*" );
    query.setRows( 0 );
    query.setGetFieldStatistics( f );
    QueryResponse rsp = server.query( query );
    FieldStatsInfo stats = rsp.getFieldStatsInfo().get( f );
    assertNotNull( stats );
    assertEquals( 23.0, stats.getMin() );
    assertEquals( 94.0, stats.getMax() );
    assertEquals( new Long(nums.length), stats.getCount() );
    assertEquals( new Long(0), stats.getMissing() );
    assertEquals( "26.4", stats.getStddev().toString().substring(0,4) );
    server.deleteByQuery( "*:*" );
    server.commit();
    assertNumFound( "*:*", 0 ); 
    nums = new int[] { 5, 7, 10, 19, 20 };
    for( int num : nums ) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", "doc"+i++ );
      doc.setField( "name", "doc: "+num );
      doc.setField( f, num );
      server.add( doc );
    }
    server.commit();
    assertNumFound( "*:*", nums.length ); 
    rsp = server.query( query );
    stats = rsp.getFieldStatsInfo().get( f );
    assertNotNull( stats );
    assertEquals( 5.0, stats.getMin() );
    assertEquals( 20.0, stats.getMax() );
    assertEquals( new Long(nums.length), stats.getCount() );
    assertEquals( new Long(0), stats.getMissing() );
    server.deleteByQuery( "*:*" );
    server.commit();
    assertNumFound( "*:*", 0 ); 
    nums = new int[] { 1, 2, 3, 4, 5, 10, 11, 12, 13, 14 };
    for( i=0; i<nums.length; i++ ) {
      int num = nums[i];
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", "doc"+i );
      doc.setField( "name", "doc: "+num );
      doc.setField( f, num );
      doc.setField( "inStock", i < 5 );
      server.add( doc );
    }
    server.commit();
    assertNumFound( "inStock:true",  5 ); 
    assertNumFound( "inStock:false", 5 ); 
    query.addStatsFieldFacets( f, "inStock" );
    rsp = server.query( query );
    stats = rsp.getFieldStatsInfo().get( f );
    assertNotNull( stats );
    List<FieldStatsInfo> facets = stats.getFacets().get( "inStock" );
    assertNotNull( facets );
    assertEquals( 2, facets.size() );
    FieldStatsInfo inStockF = facets.get(0);
    FieldStatsInfo inStockT = facets.get(1);
    if( "true".equals( inStockF.getName() ) ) {
      FieldStatsInfo tmp = inStockF;
      inStockF = inStockT;
      inStockT = tmp;
    }
    assertEquals( inStockF.getCount(), inStockT.getCount() );
    assertEquals( stats.getCount().longValue(), inStockF.getCount()+inStockT.getCount() );
    assertTrue( "check that min max faceted ok", inStockF.getMin() > inStockT.getMax() );
    assertEquals( "they have the same distribution", inStockF.getStddev(), inStockT.getStddev() );
  }
  public void testPingHandler() throws Exception
  {    
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    server.commit();
    assertNumFound( "*:*", 0 ); 
    server.ping();
    try {
      SolrPing ping = new SolrPing();
      ping.getParams().set( "qt", "unknown handler!" );
      ping.process( server );
      fail( "sent unknown query type!" );
    }
    catch( Exception ex ) {
    }
  }
  public void testFaceting() throws Exception
  {    
    SolrServer server = getSolrServer();
    server.deleteByQuery( "*:*" );
    server.commit();
    assertNumFound( "*:*", 0 ); 
    ArrayList<SolrInputDocument> docs = new ArrayList<SolrInputDocument>(10);
    for( int i=1; i<=10; i++ ) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", i+"", 1.0f );
      if( (i%2)==0 ) {
        doc.addField( "features", "two" );
      }
      if( (i%3)==0 ) {
        doc.addField( "features", "three" );
      }
      if( (i%4)==0 ) {
        doc.addField( "features", "four" );
      }
      if( (i%5)==0 ) {
        doc.addField( "features", "five" );
      }
      docs.add( doc );
    }
    server.add( docs );
    server.commit();
    SolrQuery query = new SolrQuery( "*:*" );
    query.remove( FacetParams.FACET_FIELD );
    query.addFacetField( "features" );
    query.setFacetMinCount( 0 );
    query.setFacet( true );
    query.setRows( 0 );
    QueryResponse rsp = server.query( query );
    assertEquals( docs.size(), rsp.getResults().getNumFound() );
    List<FacetField> facets = rsp.getFacetFields();
    assertEquals( 1, facets.size() );
    FacetField ff = facets.get( 0 );
    assertEquals( "features", ff.getName() );
    assertEquals( "[two (5), three (3), five (2), four (2)]", ff.getValues().toString() );
    query.setFilterQueries( "features:two" );
    rsp = server.query( query );
    ff = rsp.getFacetField( "features" );
    assertEquals( "[two (5), four (2), five (1), three (1)]", ff.getValues().toString() );
    query.setFacetMinCount( 4 );
    rsp = server.query( query );
    ff = rsp.getFacetField( "features" );
    assertEquals( "[two (5)]", ff.getValues().toString() );
    query.setFacetMinCount( -1 );
    rsp = server.query( query );
    ff = rsp.getFacetField( "features" );
  }
}
