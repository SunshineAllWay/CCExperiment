package org.apache.solr.util.plugin;
import java.util.Map;
import org.apache.solr.common.util.DOMUtil;
import org.w3c.dom.Node;
public class MapPluginLoader<T extends MapInitializedPlugin> extends AbstractPluginLoader<T> 
{
  private final Map<String,T> registry;
  public MapPluginLoader( String name, Map<String,T> map )
  {
    super( name );
    registry = map;
  }
  @Override
  protected void init(T plugin, Node node) throws Exception {
    Map<String,String> params = DOMUtil.toMapExcept( node.getAttributes(), "name","class" );
    plugin.init( params );
  }
  @Override
  protected T register(String name, T plugin) throws Exception {
    if( registry != null ) {
      return registry.put( name, plugin );
    }
    return null;
  }
}
