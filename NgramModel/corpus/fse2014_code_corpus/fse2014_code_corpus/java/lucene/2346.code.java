package org.apache.solr.handler.admin;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.handler.RequestHandlerUtils;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
public class PluginInfoHandler extends RequestHandlerBase
{
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception 
  {
    SolrParams params = req.getParams();
    boolean stats = params.getBool( "stats", false );
    rsp.add( "plugins", getSolrInfoBeans( req.getCore(), stats ) );
    rsp.setHttpCaching(false);
  }
  private static SimpleOrderedMap<Object> getSolrInfoBeans( SolrCore core, boolean stats )
  {
    SimpleOrderedMap<Object> list = new SimpleOrderedMap<Object>();
    for (SolrInfoMBean.Category cat : SolrInfoMBean.Category.values()) 
    {
      SimpleOrderedMap<Object> category = new SimpleOrderedMap<Object>();
      list.add( cat.name(), category );
      Map<String, SolrInfoMBean> reg = core.getInfoRegistry();
      for (Map.Entry<String,SolrInfoMBean> entry : reg.entrySet()) {
        SolrInfoMBean m = entry.getValue();
        if (m.getCategory() != cat) continue;
        String na = "Not Declared";
        SimpleOrderedMap<Object> info = new SimpleOrderedMap<Object>();
        category.add( entry.getKey(), info );
        info.add( "name",        (m.getName()       !=null ? m.getName()        : na) );
        info.add( "version",     (m.getVersion()    !=null ? m.getVersion()     : na) );
        info.add( "description", (m.getDescription()!=null ? m.getDescription() : na) );
        info.add( "sourceId",    (m.getSourceId()   !=null ? m.getSourceId()    : na) );
        info.add( "source",      (m.getSource()     !=null ? m.getSource()      : na) );
        URL[] urls = m.getDocs();
        if ((urls != null) && (urls.length > 0)) {
          ArrayList<String> docs = new ArrayList<String>(urls.length);
          for( URL u : urls ) {
            docs.add( u.toExternalForm() );
          }
          info.add( "docs", docs );
        }
        if( stats ) {
          info.add( "stats", m.getStatistics() );
        }
      }
    }
    return list;
  }
  @Override
  public String getDescription() {
    return "Registry";
  }
  @Override
  public String getVersion() {
      return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: PluginInfoHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/admin/PluginInfoHandler.java $";
  }
}
