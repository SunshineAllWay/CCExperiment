package org.apache.solr.core;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.solr.analysis.KeywordTokenizerFactory;
import org.apache.solr.analysis.NGramFilterFactory;
import org.apache.solr.common.SolrException;
import org.apache.solr.handler.admin.LukeRequestHandler;
import org.apache.solr.handler.component.FacetComponent;
import org.apache.solr.response.JSONResponseWriter;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import org.apache.solr.util.plugin.SolrCoreAware;
import java.io.File;
public class ResourceLoaderTest extends TestCase 
{
  public void testInstanceDir() throws Exception {
    SolrResourceLoader loader = new SolrResourceLoader(null);
    String instDir = loader.getInstanceDir();
    assertTrue(instDir + " is not equal to " + "solr/", instDir.equals("solr/") == true);
    loader = new SolrResourceLoader("solr");
    instDir = loader.getInstanceDir();
    assertTrue(instDir + " is not equal to " + "solr/", instDir.equals("solr" + File.separator) == true);
  }
  public void testAwareCompatibility() 
  {
    SolrResourceLoader loader = new SolrResourceLoader( "." );
    Class clazz = ResourceLoaderAware.class;
    loader.assertAwareCompatibility( clazz, new NGramFilterFactory() );
    loader.assertAwareCompatibility( clazz, new KeywordTokenizerFactory() );
    Object[] invalid = new Object[] {
        "hello",  new Float( 12.3f ),
        new LukeRequestHandler(),
        new JSONResponseWriter()
    };
    for( Object obj : invalid ) {
      try {
        loader.assertAwareCompatibility( clazz, obj );
        Assert.fail( "Should be invalid class: "+obj + " FOR " + clazz );
      }
      catch( SolrException ex ) { } 
    }
    clazz = SolrCoreAware.class;
    loader.assertAwareCompatibility( clazz, new LukeRequestHandler() );
    loader.assertAwareCompatibility( clazz, new FacetComponent() );
    loader.assertAwareCompatibility( clazz, new JSONResponseWriter() );
    invalid = new Object[] {
        new NGramFilterFactory(),
        "hello",  new Float( 12.3f ),
        new KeywordTokenizerFactory()
    };
    for( Object obj : invalid ) {
      try {
        loader.assertAwareCompatibility( clazz, obj );
        Assert.fail( "Should be invalid class: "+obj + " FOR " + clazz );
      }
      catch( SolrException ex ) { } 
    }
  }
}