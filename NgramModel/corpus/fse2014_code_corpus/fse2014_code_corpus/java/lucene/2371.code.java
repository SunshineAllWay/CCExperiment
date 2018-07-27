package org.apache.solr.highlight;
import java.net.URL;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrInfoMBean;
public abstract class HighlightingPluginBase implements SolrInfoMBean
{
  protected long numRequests;
  protected SolrParams defaults;
  public void init(NamedList args) {
    if( args != null ) {
      Object o = args.get("defaults");
      if (o != null && o instanceof NamedList ) {
        defaults = SolrParams.toSolrParams((NamedList)o);
      }
    }
  }
  public String getName() {
    return this.getClass().getName();
  }
  public abstract String getDescription();
  public abstract String getSourceId();
  public abstract String getSource();
  public abstract String getVersion();
  public Category getCategory()
  {
    return Category.HIGHLIGHTING;
  }
  public URL[] getDocs() {
    return null;  
  }
  public NamedList getStatistics() {
    NamedList<Long> lst = new SimpleOrderedMap<Long>();
    lst.add("requests", numRequests);
    return lst;
  }
}
