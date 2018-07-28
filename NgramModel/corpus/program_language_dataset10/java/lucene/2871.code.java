package org.apache.solr.update;
import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.schema.FieldType;
public class DocumentBuilderTest extends AbstractSolrTestCase {
  @Override public String getSchemaFile() { return "schema.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig.xml"; }
  public void testBuildDocument() throws Exception 
  {
    SolrCore core = h.getCore();
    try {
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "unknown field", 12345, 1.0f );
      DocumentBuilder.toDocument( doc, core.getSchema() );
      fail( "should throw an error" );
    }
    catch( SolrException ex ) {
      assertEquals( "should be bad request", 400, ex.code() );
    }
  }
  public void testNullField() 
  {
    SolrCore core = h.getCore();
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField( "name", null, 1.0f );
    Document out = DocumentBuilder.toDocument( doc, core.getSchema() );
    assertNull( out.get( "name" ) );
  }
  public void testMultiField() throws Exception {
    SolrCore core = h.getCore();
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField( "home", "2.2,3.3", 1.0f );
    Document out = DocumentBuilder.toDocument( doc, core.getSchema() );
    assertNotNull( out.get( "home" ) );
    assertNotNull( out.getField( "home_0" + FieldType.POLY_FIELD_SEPARATOR + "double" ) );
    assertNotNull( out.getField( "home_1" + FieldType.POLY_FIELD_SEPARATOR + "double" ) );
  }
}
