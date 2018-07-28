package org.apache.solr.update.processor;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.apache.solr.util.AbstractSolrTestCase;
public class UpdateRequestProcessorFactoryTest extends AbstractSolrTestCase {
  @Override public String getSchemaFile()     { return "schema.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig-transformers.xml"; }
  public void testConfiguration() throws Exception 
  {
    SolrCore core = h.getCore();
    UpdateRequestProcessorChain chained = core.getUpdateProcessingChain( "standard" );
    assertEquals( 3, chained.getFactories().length );
    LogUpdateProcessorFactory log = (LogUpdateProcessorFactory)chained.getFactories()[0];
    assertEquals( 100, log.maxNumToLog );
    UpdateRequestProcessorChain custom = core.getUpdateProcessingChain( null );
    CustomUpdateRequestProcessorFactory link = (CustomUpdateRequestProcessorFactory) custom.getFactories()[0];
    assertEquals( custom, core.getUpdateProcessingChain( "" ) );
    assertEquals( custom, core.getUpdateProcessingChain( "custom" ) );
    assertEquals( "{name={n8=88,n9=99}}", link.args.toString() );
  }
}
