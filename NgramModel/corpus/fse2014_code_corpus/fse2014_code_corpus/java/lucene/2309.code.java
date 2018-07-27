package org.apache.solr.core;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
final class RequestHandlers {
  public static Logger log = LoggerFactory.getLogger(RequestHandlers.class);
  public static final String DEFAULT_HANDLER_NAME="standard";
  protected final SolrCore core;
  private final Map<String, SolrRequestHandler> handlers =
      new ConcurrentHashMap<String,SolrRequestHandler>() ;
  private static String normalize( String p )
  {
    if(p == null) return "";
    if( p.endsWith( "/" ) && p.length() > 1 )
      return p.substring( 0, p.length()-1 );
    return p;
  }
  public RequestHandlers(SolrCore core) {
      this.core = core;
  }
  public SolrRequestHandler get(String handlerName) {
    return handlers.get(normalize(handlerName));
  }
  public Map<String,SolrRequestHandler> getAll(Class clazz) {
    Map<String,SolrRequestHandler> result 
      = new HashMap<String,SolrRequestHandler>(7);
    for (Map.Entry<String,SolrRequestHandler> e : handlers.entrySet()) {
      if(clazz.isInstance(e.getValue())) result.put(e.getKey(), e.getValue());
    }
    return result;
  }
  public SolrRequestHandler register( String handlerName, SolrRequestHandler handler ) {
    String norm = normalize( handlerName );
    if( handler == null ) {
      return handlers.remove( norm );
    }
    SolrRequestHandler old = handlers.put(norm, handler);
    if (0 != norm.length() && handler instanceof SolrInfoMBean) {
      core.getInfoRegistry().put(handlerName, handler);
    }
    return old;
  }
  public Map<String,SolrRequestHandler> getRequestHandlers() {
    return Collections.unmodifiableMap( handlers );
  }
  void initHandlersFromConfig(SolrConfig config ){
    Map<PluginInfo,SolrRequestHandler> handlers = new HashMap<PluginInfo,SolrRequestHandler>();
    for (PluginInfo info : config.getPluginInfos(SolrRequestHandler.class.getName())) {
      try {
        SolrRequestHandler requestHandler;
        String startup = info.attributes.get("startup") ;
        if( startup != null ) {
          if( "lazy".equals(startup) ) {
            log.info("adding lazy requestHandler: " + info.className);
            requestHandler = new LazyRequestHandlerWrapper( core, info.className, info.initArgs );
          } else {
            throw new Exception( "Unknown startup value: '"+startup+"' for: "+info.className );
          }
        } else {
          requestHandler = core.createRequestHandler(info.className);
        }
        handlers.put(info,requestHandler);
        if (requestHandler instanceof PluginInfoInitialized) {
          ((PluginInfoInitialized) requestHandler).init(info);
        } else{
          requestHandler.init(info.initArgs);
        }
        SolrRequestHandler old = register(info.name, requestHandler);
        if(old != null) {
          log.warn("Multiple requestHandler registered to the same name: " + info.name + " ignoring: " + old.getClass().getName());
        }
        if(info.isDefault()){
          old = register("",requestHandler);
          if(old != null)
            log.warn("Multiple default requestHandler registered" + " ignoring: " + old.getClass().getName()); 
        }
        log.info("created "+info.name+": " + info.className);
      } catch (Exception e) {
          SolrConfig.severeErrors.add( e );
          SolrException.logOnce(log,null,e);
      }
    }
    for (Map.Entry<PluginInfo,SolrRequestHandler> entry : handlers.entrySet()) {
      entry.getValue().init(entry.getKey().initArgs);
    }
    if(get("") == null) register("", get(DEFAULT_HANDLER_NAME));
  }
  private static final class LazyRequestHandlerWrapper implements SolrRequestHandler, SolrInfoMBean
  {
    private final SolrCore core;
    private String _className;
    private NamedList _args;
    private SolrRequestHandler _handler;
    public LazyRequestHandlerWrapper( SolrCore core, String className, NamedList args )
    {
      this.core = core;
      _className = className;
      _args = args;
      _handler = null; 
    }
    public void init(NamedList args) {
    }
    public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp)  {
      SolrRequestHandler handler = _handler;
      if (handler == null) {
        handler = getWrappedHandler();
      }
      handler.handleRequest( req, rsp );
    }
    public synchronized SolrRequestHandler getWrappedHandler()
    {
      if( _handler == null ) {
        try {
          SolrRequestHandler handler = core.createRequestHandler(_className);
          handler.init( _args );
          if( handler instanceof SolrCoreAware ) {
            ((SolrCoreAware)handler).inform( core );
          }
          _handler = handler;
        }
        catch( Exception ex ) {
          throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "lazy loading error", ex );
        }
      }
      return _handler;
    }
    public String getHandlerClass()
    {
      return _className;
    }
    public String getName() {
      return "Lazy["+_className+"]";
    }
    public String getDescription()
    {
      if( _handler == null ) {
        return getName();
      }
      return _handler.getDescription();
    }
    public String getVersion() {
        String rev = "$Revision: 898152 $";
        if( _handler != null ) {
          rev += " :: " + _handler.getVersion();
        }
        return rev;
    }
    public String getSourceId() {
      String rev = "$Id: RequestHandlers.java 898152 2010-01-12 02:19:56Z ryan $";
      if( _handler != null ) {
        rev += " :: " + _handler.getSourceId();
      }
      return rev;
    }
    public String getSource() {
      String rev = "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/core/RequestHandlers.java $";
      if( _handler != null ) {
        rev += "\n" + _handler.getSource();
      }
      return rev;
    }
    public URL[] getDocs() {
      if( _handler == null ) {
        return null;
      }
      return _handler.getDocs();
    }
    public Category getCategory()
    {
      return Category.QUERYHANDLER;
    }
    public NamedList getStatistics() {
      if( _handler != null ) {
        return _handler.getStatistics();
      }
      NamedList<String> lst = new SimpleOrderedMap<String>();
      lst.add("note", "not initialized yet" );
      return lst;
    }
  }
}
