package org.apache.solr.util.plugin;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.DOMUtil;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrResourceLoader;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public abstract class AbstractPluginLoader<T>
{
  public static Logger log = LoggerFactory.getLogger(AbstractPluginLoader.class);
  private final String type;
  private final boolean preRegister;
  private final boolean requireName;
  public AbstractPluginLoader( String type, boolean preRegister, boolean requireName )
  {
    this.type = type;
    this.preRegister = preRegister;
    this.requireName = requireName;
  }
  public AbstractPluginLoader( String type )
  {
    this( type, false, true );
  }
  protected String[] getDefaultPackages()
  {
    return new String[]{};
  }
  @SuppressWarnings("unchecked")
  protected T create( ResourceLoader loader, String name, String className, Node node ) throws Exception
  {
    return (T) loader.newInstance( className, getDefaultPackages() );
  }
  abstract protected T register( String name, T plugin ) throws Exception;
  abstract protected void init( T plugin, Node node ) throws Exception;
  public T load( ResourceLoader loader, NodeList nodes )
  {
    List<PluginInitInfo> info = new ArrayList<PluginInitInfo>();
    T defaultPlugin = null;
    if (nodes !=null ) {
      for (int i=0; i<nodes.getLength(); i++) {
        Node node = nodes.item(i);
        try {
          String name       = DOMUtil.getAttr(node,"name", requireName?type:null);
          String className  = DOMUtil.getAttr(node,"class", type);
          String defaultStr = DOMUtil.getAttr(node,"default", null );
          T plugin = create(loader, name, className, node );
          log.info("created " + ((name != null) ? name : "") + ": " + plugin.getClass().getName());
          if( preRegister ) {
            info.add( new PluginInitInfo( plugin, node ) );
          }
          else {
            init( plugin, node );
          }
          T old = register( name, plugin );
          if( old != null && !( name == null && !requireName ) ) {
            throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, 
                "Multiple "+type+" registered to the same name: "+name+" ignoring: "+old );
          }
          if( defaultStr != null && Boolean.parseBoolean( defaultStr ) ) {
            if( defaultPlugin != null ) {
              throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, 
                "Multiple default "+type+" plugins: "+defaultPlugin + " AND " + name );
            }
            defaultPlugin = plugin;
          }
        }
        catch (Exception e) {
          SolrConfig.severeErrors.add( e );
          SolrException.logOnce(log,null,e);
        }
      }
    }
    for( PluginInitInfo pinfo : info ) {
      try {
        init( pinfo.plugin, pinfo.node );
      }
      catch( Exception ex ) {
        SolrConfig.severeErrors.add( ex );
        SolrException.logOnce(log,null,ex);
      }
    }
    return defaultPlugin;
  }
  public T loadSingle(ResourceLoader loader, Node node) {
    List<PluginInitInfo> info = new ArrayList<PluginInitInfo>();
    T plugin = null;
    try {
      String name = DOMUtil.getAttr(node, "name", requireName ? type : null);
      String className = DOMUtil.getAttr(node, "class", type);
      plugin = create(loader, name, className, node);
      log.info("created " + name + ": " + plugin.getClass().getName());
      if (preRegister) {
        info.add(new PluginInitInfo(plugin, node));
      } else {
        init(plugin, node);
      }
      T old = register(name, plugin);
      if (old != null && !(name == null && !requireName)) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
            "Multiple " + type + " registered to the same name: " + name
                + " ignoring: " + old);
      }
    } catch (Exception e) {
      SolrConfig.severeErrors.add(e);
      SolrException.logOnce(log, null, e);
    }
    for (PluginInitInfo pinfo : info) {
      try {
        init(pinfo.plugin, pinfo.node);
      } catch (Exception ex) {
        SolrConfig.severeErrors.add(ex);
        SolrException.logOnce(log, null, ex);
      }
    }
    return plugin;
  }
  private class PluginInitInfo
  {
    final T plugin;
    final Node node;
    PluginInitInfo( T plugin, Node node )
    {
      this.plugin = plugin;
      this.node = node;
    }
  }
}
