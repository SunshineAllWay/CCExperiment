package org.apache.solr.schema;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.util.AbstractSolrTestCase;
public class NotRequiredUniqueKeyTest extends AbstractSolrTestCase {
  @Override public String getSchemaFile() { return "schema-not-required-unique-key.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig.xml"; }
  @Override 
  public void setUp() throws Exception {
    super.setUp();
  }
  @Override 
  public void tearDown() throws Exception {
    super.tearDown();
  }
  public void testSchemaLoading() 
  {
    SolrCore core = h.getCore();
    IndexSchema schema = core.getSchema();
    SchemaField uniqueKey = schema.getUniqueKeyField();
    assertFalse( uniqueKey.isRequired() );
    assertFalse( schema.getRequiredFields().contains( uniqueKey ) );
  }
}
