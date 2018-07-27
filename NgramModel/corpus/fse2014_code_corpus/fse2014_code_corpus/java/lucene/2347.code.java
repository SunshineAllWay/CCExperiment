package org.apache.solr.handler.admin;
import java.io.IOException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
public class PropertiesRequestHandler extends RequestHandlerBase
{
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws IOException 
  {
    Object props = null;
    String name = req.getParams().get( "name" );
    if( name != null ) {
      NamedList<String> p = new SimpleOrderedMap<String>();
      p.add( name, System.getProperty(name) );
      props = p;
    }
    else {
      props = System.getProperties();
    }
    rsp.add( "system.properties", props );
    rsp.setHttpCaching(false);
  }
  @Override
  public String getDescription() {
    return "Get System Properties";
  }
  @Override
  public String getVersion() {
      return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: PropertiesRequestHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/admin/PropertiesRequestHandler.java $";
  }
}
