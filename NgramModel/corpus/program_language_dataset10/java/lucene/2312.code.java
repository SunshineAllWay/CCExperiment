package org.apache.solr.core;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Directory;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.CommonParams.EchoParamStyle;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.admin.ShowFileRequestHandler;
import org.apache.solr.handler.component.*;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.*;
import org.apache.solr.response.BinaryResponseWriter;
import org.apache.solr.response.JSONResponseWriter;
import org.apache.solr.response.PHPResponseWriter;
import org.apache.solr.response.PHPSerializedResponseWriter;
import org.apache.solr.response.PythonResponseWriter;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.RawResponseWriter;
import org.apache.solr.response.RubyResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SolrFieldCacheMBean;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.ValueSourceParser;
import org.apache.solr.update.DirectUpdateHandler2;
import org.apache.solr.update.SolrIndexWriter;
import org.apache.solr.update.UpdateHandler;
import org.apache.solr.update.processor.LogUpdateProcessorFactory;
import org.apache.solr.update.processor.RunUpdateProcessorFactory;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.solr.util.RefCounted;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.lang.reflect.Constructor;
public final class SolrCore implements SolrInfoMBean {
  public static final String version="1.0";  
  public static Logger log = LoggerFactory.getLogger(SolrCore.class);
  private String name;
  private String logid; 
  private final CoreDescriptor coreDescriptor;
  private final SolrConfig solrConfig;
  private final SolrResourceLoader resourceLoader;
  private final IndexSchema schema;
  private final String dataDir;
  private final UpdateHandler updateHandler;
  private final long startTime;
  private final RequestHandlers reqHandlers;
  private final Map<String,SearchComponent> searchComponents;
  private final Map<String,UpdateRequestProcessorChain> updateProcessorChains;
  private final Map<String, SolrInfoMBean> infoRegistry;
  private IndexDeletionPolicyWrapper solrDelPolicy;
  private DirectoryFactory directoryFactory;
  private IndexReaderFactory indexReaderFactory;
  public long getStartTime() { return startTime; }
  @Deprecated
  private static SolrCore instance;
  static int boolean_query_max_clause_count = Integer.MIN_VALUE;
  void booleanQueryMaxClauseCount()  {
    synchronized(SolrCore.class) {
      if (boolean_query_max_clause_count == Integer.MIN_VALUE) {
        boolean_query_max_clause_count = solrConfig.booleanQueryMaxClauseCount;
        BooleanQuery.setMaxClauseCount(boolean_query_max_clause_count);
      } else if (boolean_query_max_clause_count != solrConfig.booleanQueryMaxClauseCount ) {
        log.debug("BooleanQuery.maxClauseCount= " +boolean_query_max_clause_count+ ", ignoring " +solrConfig.booleanQueryMaxClauseCount);
      }
    }
  }
  public SolrResourceLoader getResourceLoader() {
    return resourceLoader;
  }
  public String getConfigResource() {
    return solrConfig.getResourceName();
  }
  @Deprecated
  public String getConfigFile() {
    return solrConfig.getResourceName();
  }
  public SolrConfig getSolrConfig() {
    return solrConfig;
  }
  public String getSchemaResource() {
    return schema.getResourceName();
  }
  @Deprecated
  public String getSchemaFile() {
    return schema.getResourceName();
  }
  public IndexSchema getSchema() { 
    return schema;
  }
  public String getDataDir() {
    return dataDir;
  }
  public String getIndexDir() {
    if (_searcher == null)
      return dataDir + "index/";
    SolrIndexSearcher searcher = _searcher.get();
    return searcher.getIndexDir() == null ? dataDir + "index/" : searcher.getIndexDir();
  }
  public String getNewIndexDir() {
    String result = dataDir + "index/";
    File propsFile = new File(dataDir + "index.properties");
    if (propsFile.exists()) {
      Properties p = new Properties();
      InputStream is = null;
      try {
        is = new FileInputStream(propsFile);
        p.load(is);
      } catch (IOException e) {
      } finally {
        IOUtils.closeQuietly(is);
      }
      String s = p.getProperty("index");
      if (s != null && s.trim().length() > 0) {
        File tmp = new File(dataDir + s);
        if (tmp.exists() && tmp.isDirectory())
          result = dataDir + s;
      }
    }
    return result;
  }
  public DirectoryFactory getDirectoryFactory() {
    return directoryFactory;
  }
  public IndexReaderFactory getIndexReaderFactory() {
    return indexReaderFactory;
  }
  public String getName() {
    return name;
  }
  public void setName(String v) {
    this.name = v;
    this.logid = (v==null)?"":("["+v+"] ");
  }
  public String getLogId()
  {
    return this.logid;
  }
  public Map<String, SolrInfoMBean> getInfoRegistry() {
    return infoRegistry;
  }
   private void initDeletionPolicy() {
     PluginInfo info = solrConfig.getPluginInfo(IndexDeletionPolicy.class.getName());
     IndexDeletionPolicy delPolicy = null;
     if(info != null){
       delPolicy = createInstance(info.className,IndexDeletionPolicy.class,"Deletion Policy for SOLR");
       if (delPolicy instanceof NamedListInitializedPlugin) {
         ((NamedListInitializedPlugin) delPolicy).init(info.initArgs);
       }
     } else {
       delPolicy = new SolrDeletionPolicy();
     }     
     solrDelPolicy = new IndexDeletionPolicyWrapper(delPolicy);
   }
  private void initListeners() {
    for (PluginInfo info : solrConfig.getPluginInfos(SolrEventListener.class.getName())) {
      SolrEventListener listener = createInitInstance(info, SolrEventListener.class,"Event Listener",null);      
      String event = info.attributes.get("event");
      if("firstSearcher".equals(event) ){
        firstSearcherListeners.add(listener);
      } else if("newSearcher".equals(event) ){
        newSearcherListeners.add(listener);
      }
      log.info(logid + "Added SolrEventListener: " + listener);
    }
  }
  final List<SolrEventListener> firstSearcherListeners = new ArrayList<SolrEventListener>();
  final List<SolrEventListener> newSearcherListeners = new ArrayList<SolrEventListener>();
  public void registerFirstSearcherListener( SolrEventListener listener )
  {
    firstSearcherListeners.add( listener );
  }
  public void registerNewSearcherListener( SolrEventListener listener )
  {
    newSearcherListeners.add( listener );
  }
  public void registerResponseWriter( String name, QueryResponseWriter responseWriter ){
    responseWriters.put(name, responseWriter);
  }
  public SolrIndexSearcher newSearcher(String name) throws IOException {
    return newSearcher(name, false);
  }
  public SolrIndexSearcher newSearcher(String name, boolean readOnly) throws IOException {
    return new SolrIndexSearcher(this, schema, name, directoryFactory.open(getIndexDir()), readOnly, false);
  }
   private void initDirectoryFactory() {
    DirectoryFactory dirFactory;
    PluginInfo info = solrConfig.getPluginInfo(DirectoryFactory.class.getName());
    if (info != null) {
      dirFactory = (DirectoryFactory) getResourceLoader().newInstance(info.className);
      dirFactory.init(info.initArgs);
    } else {
      dirFactory = new StandardDirectoryFactory();
    }
    directoryFactory = dirFactory;
  }
  private void initIndexReaderFactory() {
    IndexReaderFactory indexReaderFactory;
    PluginInfo info = solrConfig.getPluginInfo(IndexReaderFactory.class.getName());
    if (info != null) {
      indexReaderFactory = (IndexReaderFactory) resourceLoader.newInstance(info.className);
      indexReaderFactory.init(info.initArgs);
    } else {
      indexReaderFactory = new StandardIndexReaderFactory();
    } 
    this.indexReaderFactory = indexReaderFactory;
  }
  private static Set<String> dirs = new HashSet<String>();
  void initIndex() {
    try {
      File dirFile = new File(getNewIndexDir());
      boolean indexExists = dirFile.canRead();
      boolean firstTime;
      synchronized (SolrCore.class) {
        firstTime = dirs.add(dirFile.getCanonicalPath());
      }
      boolean removeLocks = solrConfig.unlockOnStartup;
      initDirectoryFactory();
      initIndexReaderFactory();
      if (indexExists && firstTime && removeLocks) {
        Directory dir = SolrIndexWriter.getDirectory(getIndexDir(), getDirectoryFactory(), solrConfig.mainIndexConfig);
        if (dir != null)  {
          if (IndexWriter.isLocked(dir)) {
            log.warn(logid+"WARNING: Solr index directory '" + getIndexDir() + "' is locked.  Unlocking...");
            IndexWriter.unlock(dir);
          }
          dir.close();
        }
      }
      if(!indexExists) {
        log.warn(logid+"Solr index directory '" + dirFile + "' doesn't exist."
                + " Creating new index...");
        SolrIndexWriter writer = new SolrIndexWriter("SolrCore.initIndex", getIndexDir(), getDirectoryFactory(), true, schema, solrConfig.mainIndexConfig, solrDelPolicy);
        writer.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  private <T extends Object> T createInstance(String className, Class<T> cast, String msg) {
    Class clazz = null;
    if (msg == null) msg = "SolrCore Object";
    try {
        clazz = getResourceLoader().findClass(className);
        if (cast != null && !cast.isAssignableFrom(clazz))
          throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,"Error Instantiating "+msg+", "+className+ " is not a " +cast.getName());
        Constructor[] cons =  clazz.getConstructors();
        for (Constructor con : cons) {
          Class[] types = con.getParameterTypes();
          if(types.length == 1 && types[0] == SolrCore.class){
            return (T)con.newInstance(this);
          }
        }
        return (T) getResourceLoader().newInstance(className);
    } catch (SolrException e) {
      throw e;
    } catch (Exception e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,"Error Instantiating "+msg+", "+className+ " failed to instantiate " +cast.getName(), e);
    }
  }
  public <T extends Object> T createInitInstance(PluginInfo info,Class<T> cast, String msg, String defClassName){
    if(info == null) return null;
    T o = createInstance(info.className == null ? defClassName : info.className,cast, msg);
    if (o instanceof PluginInfoInitialized) {
      ((PluginInfoInitialized) o).init(info);
    } else if (o instanceof NamedListInitializedPlugin) {
      ((NamedListInitializedPlugin) o).init(info.initArgs);
    }
    return o;
  }
  public SolrEventListener createEventListener(String className) {
    return createInstance(className, SolrEventListener.class, "Event Listener");
  }
  public SolrRequestHandler createRequestHandler(String className) {
    return createInstance(className, SolrRequestHandler.class, "Request Handler");
  }
  private UpdateHandler createUpdateHandler(String className) {
    return createInstance(className, UpdateHandler.class, "Update Handler");
  }
  @Deprecated
  public static SolrCore getSolrCore() {
    synchronized( SolrCore.class ) {
      if( instance == null ) {
        try {
          CoreContainer.Initializer init = new CoreContainer.Initializer();
          instance = init.initialize().getCore("");
        } catch(Exception xany) {
          throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
              "error creating core", xany );
        }
      }
    }
    return instance;
  }
  public SolrCore(String dataDir, IndexSchema schema) throws ParserConfigurationException, IOException, SAXException {
    this(null, dataDir, new SolrConfig(), schema, null );
  }
  public SolrCore(String name, String dataDir, SolrConfig config, IndexSchema schema, CoreDescriptor cd) {
    coreDescriptor = cd;
    this.setName( name );
    resourceLoader = config.getResourceLoader();
    if (dataDir == null){
      dataDir =  config.getDataDir();
      if(dataDir == null) dataDir = cd.getDataDir();
    }
    dataDir = SolrResourceLoader.normalizeDir(dataDir);
    log.info(logid+"Opening new SolrCore at " + resourceLoader.getInstanceDir() + ", dataDir="+dataDir);
    if (schema==null) {
      schema = new IndexSchema(config, IndexSchema.DEFAULT_SCHEMA_FILE, null);
    }
    if (config.jmxConfig.enabled) {
      infoRegistry = new JmxMonitoredMap<String, SolrInfoMBean>(name, config.jmxConfig);
    } else  {
      log.info("JMX monitoring not detected for core: " + name);
      infoRegistry = new ConcurrentHashMap<String, SolrInfoMBean>();
    }
    infoRegistry.put("fieldCache", new SolrFieldCacheMBean());
    this.schema = schema;
    this.dataDir = dataDir;
    this.solrConfig = config;
    this.startTime = System.currentTimeMillis();
    this.maxWarmingSearchers = config.maxWarmingSearchers;
    booleanQueryMaxClauseCount();
    initListeners();
    initDeletionPolicy();
    initIndex();
    initWriters();
    initQParsers();
    initValueSourceParsers();
    this.searchComponents = loadSearchComponents();
    updateProcessorChains = loadUpdateProcessorChains();
    reqHandlers = new RequestHandlers(this);
    reqHandlers.initHandlersFromConfig( solrConfig );
    initDeprecatedSupport();
    final CountDownLatch latch = new CountDownLatch(1);
    try {
      searcherExecutor.submit(new Callable() {
        public Object call() throws Exception {
          latch.await();
          return null;
        }
      });
      getSearcher(false,false,null);
      String updateHandlerClass = solrConfig.getUpdateHandlerInfo().className;
      updateHandler = createUpdateHandler(updateHandlerClass == null ?  DirectUpdateHandler2.class.getName():updateHandlerClass); 
      infoRegistry.put("updateHandler", updateHandler);
      resourceLoader.inform( resourceLoader );
      resourceLoader.inform( this );
      instance = this;   
    } catch (IOException e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
    } finally {
      latch.countDown();
    }
    infoRegistry.put("core", this);
    resourceLoader.inform(infoRegistry);
  }
   private Map<String,UpdateRequestProcessorChain> loadUpdateProcessorChains() {
    Map<String, UpdateRequestProcessorChain> map = new HashMap<String, UpdateRequestProcessorChain>();
    UpdateRequestProcessorChain def = initPlugins(map,UpdateRequestProcessorChain.class, UpdateRequestProcessorChain.class.getName());
    if(def == null){
      def = map.get(null);
    } 
    if (def == null) {
      UpdateRequestProcessorFactory[] factories = new UpdateRequestProcessorFactory[]{
              new RunUpdateProcessorFactory(),
              new LogUpdateProcessorFactory()
      };
      def = new UpdateRequestProcessorChain(factories, this);
    }
    map.put(null, def);
    map.put("", def);
    return map;
  }
  public UpdateRequestProcessorChain getUpdateProcessingChain( final String name )
  {
    UpdateRequestProcessorChain chain = updateProcessorChains.get( name );
    if( chain == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
          "unknown UpdateRequestProcessorChain: "+name );
    }
    return chain;
  }
  private final AtomicInteger refCount = new AtomicInteger(1);
  final void open() {
    refCount.incrementAndGet();
  }
  public void close() {
    int count = refCount.decrementAndGet();
    if (count > 0) return; 
    if (count < 0) {
      log.error("Too many close [count:{}] on {}. Please report this exception to solr-user@lucene.apache.org", count, this );
      return;
    }
    log.info(logid+" CLOSING SolrCore " + this);
    try {
      infoRegistry.clear();
    } catch (Exception e) {
      SolrException.log(log, e);
    }
    try {
      closeSearcher();
    } catch (Exception e) {
      SolrException.log(log,e);
    }
    try {
      searcherExecutor.shutdown();
    } catch (Exception e) {
      SolrException.log(log,e);
    }
    try {
      updateHandler.close();
    } catch (Exception e) {
      SolrException.log(log,e);
    }
    if( closeHooks != null ) {
       for( CloseHook hook : closeHooks ) {
         hook.close( this );
      }
    }
  }
  public int getOpenCount() {
    return refCount.get();
  }
  public boolean isClosed() {
      return refCount.get() <= 0;
  }
  protected void finalize() throws Throwable {
    try {
      if (getOpenCount() != 0) {
        log.error("REFCOUNT ERROR: unreferenced " + this + " (" + getName()
            + ") has a reference count of " + getOpenCount());
      }
    } finally {
      super.finalize();
    }
  }
  private Collection<CloseHook> closeHooks = null;
   public void addCloseHook( CloseHook hook )
   {
     if( closeHooks == null ) {
       closeHooks = new ArrayList<CloseHook>();
     }
     closeHooks.add( hook );
   }
  @Deprecated
  public SolrQueryRequest getPingQueryRequest() {
    return solrConfig.getPingQueryRequest(this);
  }
  public SolrRequestHandler getRequestHandler(String handlerName) {
    return reqHandlers.get(handlerName);
  }
  public Map<String,SolrRequestHandler> getRequestHandlers(Class clazz) {
    return reqHandlers.getAll(clazz);
  }
  public Map<String,SolrRequestHandler> getRequestHandlers() {
    return reqHandlers.getRequestHandlers();
  }
  @Deprecated
  public SolrHighlighter getHighlighter() {
    HighlightComponent hl = (HighlightComponent) searchComponents.get(HighlightComponent.COMPONENT_NAME);
    return hl==null? null: hl.getHighlighter();
  }
  public SolrRequestHandler registerRequestHandler(String handlerName, SolrRequestHandler handler) {
    return reqHandlers.register(handlerName,handler);
  }
  private Map<String, SearchComponent> loadSearchComponents()
  {
    Map<String, SearchComponent> components = new HashMap<String, SearchComponent>();
    initPlugins(components,SearchComponent.class);
    for (Map.Entry<String, SearchComponent> e : components.entrySet()) {
      SearchComponent c = e.getValue();
      if (c instanceof HighlightComponent) {
        HighlightComponent hl = (HighlightComponent) c;
        if(!HighlightComponent.COMPONENT_NAME.equals(e.getKey())){
          components.put(HighlightComponent.COMPONENT_NAME,hl);
        }
        break;
      }
    }
    addIfNotPresent(components,HighlightComponent.COMPONENT_NAME,HighlightComponent.class);
    addIfNotPresent(components,QueryComponent.COMPONENT_NAME,QueryComponent.class);
    addIfNotPresent(components,FacetComponent.COMPONENT_NAME,FacetComponent.class);
    addIfNotPresent(components,MoreLikeThisComponent.COMPONENT_NAME,MoreLikeThisComponent.class);
    addIfNotPresent(components,StatsComponent.COMPONENT_NAME,StatsComponent.class);
    addIfNotPresent(components,DebugComponent.COMPONENT_NAME,DebugComponent.class);
    return components;
  }
  private <T> void addIfNotPresent(Map<String ,T> registry, String name, Class<? extends  T> c){
    if(!registry.containsKey(name)){
      T searchComp = (T) resourceLoader.newInstance(c.getName());
      registry.put(name, searchComp);
      if (searchComp instanceof SolrInfoMBean){
        infoRegistry.put(((SolrInfoMBean)searchComp).getName(), (SolrInfoMBean)searchComp);
      }
    }
  }
  public SearchComponent getSearchComponent( String name )
  {
    SearchComponent component = searchComponents.get( name );
    if( component == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
          "Unknown Search Component: "+name );
    }
    return component;
  }
  public Map<String, SearchComponent> getSearchComponents() {
    return Collections.unmodifiableMap(searchComponents);
  }
  public UpdateHandler getUpdateHandler() {
    return updateHandler;
  }
  private RefCounted<SolrIndexSearcher> _searcher;
  private final LinkedList<RefCounted<SolrIndexSearcher>> _searchers = new LinkedList<RefCounted<SolrIndexSearcher>>();
  final ExecutorService searcherExecutor = Executors.newSingleThreadExecutor();
  private int onDeckSearchers;  
  private Object searcherLock = new Object();  
  private final int maxWarmingSearchers;  
  public RefCounted<SolrIndexSearcher> getSearcher() {
    try {
      return getSearcher(false,true,null);
    } catch (IOException e) {
      SolrException.log(log,null,e);
      return null;
    }
  }
  public RefCounted<SolrIndexSearcher> getNewestSearcher(boolean openNew) {
    synchronized (searcherLock) {
      if (_searchers.isEmpty()) {
        if (!openNew) return null;
        throw new UnsupportedOperationException();
      }
      RefCounted<SolrIndexSearcher> newest = _searchers.getLast();
      newest.incref();
      return newest;
    }
  }
  public RefCounted<SolrIndexSearcher> getSearcher(boolean forceNew, boolean returnSearcher, final Future[] waitSearcher) throws IOException {
    synchronized (searcherLock) {
      if (_searcher!=null && !forceNew) {
        if (returnSearcher) {
          _searcher.incref();
          return _searcher;
        } else {
          return null;
        }
      }
      if (onDeckSearchers>0 && !forceNew && _searcher==null) {
        try {
          searcherLock.wait();
        } catch (InterruptedException e) {
          log.info(SolrException.toStr(e));
        }
      }
      if (_searcher!=null && !forceNew) {
        if (returnSearcher) {
          _searcher.incref();
          return _searcher;
        } else {
          return null;
        }
      }
      onDeckSearchers++;
      if (onDeckSearchers < 1) {
        log.error(logid+"ERROR!!! onDeckSearchers is " + onDeckSearchers);
        onDeckSearchers=1;  
      } else if (onDeckSearchers > maxWarmingSearchers) {
        onDeckSearchers--;
        String msg="Error opening new searcher. exceeded limit of maxWarmingSearchers="+maxWarmingSearchers + ", try again later.";
        log.warn(logid+""+ msg);
        throw new SolrException(SolrException.ErrorCode.SERVICE_UNAVAILABLE,msg,true);
      } else if (onDeckSearchers > 1) {
        log.info(logid+"PERFORMANCE WARNING: Overlapping onDeckSearchers=" + onDeckSearchers);
      }
    }
    SolrIndexSearcher tmp;
    RefCounted<SolrIndexSearcher> newestSearcher = null;
    try {
      newestSearcher = getNewestSearcher(false);
      String newIndexDir = getNewIndexDir();
      File indexDirFile = new File(getIndexDir()).getCanonicalFile();
      File newIndexDirFile = new File(newIndexDir).getCanonicalFile();
      if (newestSearcher != null && solrConfig.reopenReaders
          && indexDirFile.equals(newIndexDirFile)) {
        IndexReader currentReader = newestSearcher.get().getReader();
        IndexReader newReader = currentReader.reopen();
        if (newReader == currentReader) {
          currentReader.incRef();
        }
        tmp = new SolrIndexSearcher(this, schema, "main", newReader, true, true);
      } else {
        IndexReader reader = getIndexReaderFactory().newReader(getDirectoryFactory().open(newIndexDir), true);
        tmp = new SolrIndexSearcher(this, schema, "main", reader, true, true);
      }
    } catch (Throwable th) {
      synchronized(searcherLock) {
        onDeckSearchers--;
        searcherLock.notify();
      }
      throw new RuntimeException(th);
    } finally {
      if (newestSearcher != null) {
        newestSearcher.decref();
      }
    }
    final SolrIndexSearcher newSearcher=tmp;
    RefCounted<SolrIndexSearcher> currSearcherHolder=null;
    final RefCounted<SolrIndexSearcher> newSearchHolder=newHolder(newSearcher);
    if (returnSearcher) newSearchHolder.incref();
    final boolean[] decrementOnDeckCount=new boolean[1];
    decrementOnDeckCount[0]=true;
    try {
      boolean alreadyRegistered = false;
      synchronized (searcherLock) {
        _searchers.add(newSearchHolder);
        if (_searcher == null) {
          if (solrConfig.useColdSearcher) {
            registerSearcher(newSearchHolder);
            decrementOnDeckCount[0]=false;
            alreadyRegistered=true;
          }
        } else {
          currSearcherHolder=_searcher;
          currSearcherHolder.incref();
        }
      }
      final SolrIndexSearcher currSearcher = currSearcherHolder==null ? null : currSearcherHolder.get();
      Future future=null;
      if (currSearcher != null) {
        future = searcherExecutor.submit(
                new Callable() {
                  public Object call() throws Exception {
                    try {
                      newSearcher.warm(currSearcher);
                    } catch (Throwable e) {
                      SolrException.logOnce(log,null,e);
                    }
                    return null;
                  }
                }
        );
      }
      if (currSearcher==null && firstSearcherListeners.size() > 0) {
        future = searcherExecutor.submit(
                new Callable() {
                  public Object call() throws Exception {
                    try {
                      for (SolrEventListener listener : firstSearcherListeners) {
                        listener.newSearcher(newSearcher,null);
                      }
                    } catch (Throwable e) {
                      SolrException.logOnce(log,null,e);
                    }
                    return null;
                  }
                }
        );
      }
      if (currSearcher!=null && newSearcherListeners.size() > 0) {
        future = searcherExecutor.submit(
                new Callable() {
                  public Object call() throws Exception {
                    try {
                      for (SolrEventListener listener : newSearcherListeners) {
                        listener.newSearcher(newSearcher, currSearcher);
                      }
                    } catch (Throwable e) {
                      SolrException.logOnce(log,null,e);
                    }
                    return null;
                  }
                }
        );
      }
      final RefCounted<SolrIndexSearcher> currSearcherHolderF = currSearcherHolder;
      if (!alreadyRegistered) {
        future = searcherExecutor.submit(
                new Callable() {
                  public Object call() throws Exception {
                    try {
                      decrementOnDeckCount[0]=false;
                      registerSearcher(newSearchHolder);
                    } catch (Throwable e) {
                      SolrException.logOnce(log,null,e);
                    } finally {
                      if (currSearcherHolderF!=null) currSearcherHolderF.decref();
                    }
                    return null;
                  }
                }
        );
      }
      if (waitSearcher != null) {
        waitSearcher[0] = future;
      }
      return returnSearcher ? newSearchHolder : null;
    } catch (Exception e) {
      SolrException.logOnce(log,null,e);
      if (currSearcherHolder != null) currSearcherHolder.decref();
      synchronized (searcherLock) {
        if (decrementOnDeckCount[0]) {
          onDeckSearchers--;
        }
        if (onDeckSearchers < 0) {
          log.error(logid+"ERROR!!! onDeckSearchers after decrement=" + onDeckSearchers);
          onDeckSearchers=0; 
        }
        searcherLock.notify();
      }
      return returnSearcher ? newSearchHolder : null;
    }
  }
  private RefCounted<SolrIndexSearcher> newHolder(SolrIndexSearcher newSearcher) {
    RefCounted<SolrIndexSearcher> holder = new RefCounted<SolrIndexSearcher>(newSearcher) {
      public void close() {
        try {
          synchronized(searcherLock) {
            if (refcount.get() > 0) return;
            _searchers.remove(this);
          }
          resource.close();
        } catch (IOException e) {
          log.error("Error closing searcher:" + SolrException.toStr(e));
        }
      }
    };
    holder.incref();  
    return holder;
  }
  private void registerSearcher(RefCounted<SolrIndexSearcher> newSearcherHolder) throws IOException {
    synchronized (searcherLock) {
      try {
        if (_searcher != null) {
          _searcher.decref();   
          _searcher=null;
        }
        _searcher = newSearcherHolder;
        SolrIndexSearcher newSearcher = newSearcherHolder.get();
        newSearcher.register(); 
        log.info(logid+"Registered new searcher " + newSearcher);
      } catch (Throwable e) {
        log(e);
      } finally {
        onDeckSearchers--;
        searcherLock.notifyAll();
      }
    }
  }
  public void closeSearcher() {
    log.info(logid+"Closing main searcher on request.");
    synchronized (searcherLock) {
      if (_searcher != null) {
        _searcher.decref();   
        _searcher=null; 
        infoRegistry.remove("currentSearcher");
      }
    }
  }
  public void execute(SolrRequestHandler handler, SolrQueryRequest req, SolrQueryResponse rsp) {
    if (handler==null) {
      String msg = "Null Request Handler '" +
        req.getParams().get(CommonParams.QT) + "'";
      if (log.isWarnEnabled()) log.warn(logid + msg + ":" + req);
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, msg, true);
    }
    final NamedList<Object> responseHeader = new SimpleOrderedMap<Object>();
    rsp.add("responseHeader", responseHeader);
    NamedList toLog = rsp.getToLog();
    toLog.add("webapp", req.getContext().get("webapp"));
    toLog.add("path", req.getContext().get("path"));
    toLog.add("params", "{" + req.getParamString() + "}");
    handler.handleRequest(req,rsp);
    setResponseHeaderValues(handler,req,rsp);
    if (log.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder(logid);
      for (int i=0; i<toLog.size(); i++) {
        String name = toLog.getName(i);
        Object val = toLog.getVal(i);
        sb.append(name).append("=").append(val).append(" ");
      }
      log.info(sb.toString());
    }
  }
  @Deprecated
  public void execute(SolrQueryRequest req, SolrQueryResponse rsp) {
    SolrRequestHandler handler = getRequestHandler(req.getQueryType());
    if (handler==null) {
      log.warn(logid+"Unknown Request Handler '" + req.getQueryType() +"' :" + req);
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Unknown Request Handler '" + req.getQueryType() + "'", true);
    }
    execute(handler, req, rsp);
  }
  public static void setResponseHeaderValues(SolrRequestHandler handler, SolrQueryRequest req, SolrQueryResponse rsp) {
	NamedList responseHeader = rsp.getResponseHeader();
    final int qtime=(int)(rsp.getEndTime() - req.getStartTime());
    int status = 0;
    Exception exception = rsp.getException();
    if( exception != null ){
      if( exception instanceof SolrException )
        status = ((SolrException)exception).code();
      else
        status = 500;
    }
    responseHeader.add("status",status);
    responseHeader.add("QTime",qtime);
    rsp.getToLog().add("status",status);
    rsp.getToLog().add("QTime",qtime);
    SolrParams params = req.getParams();
    if( params.getBool(CommonParams.HEADER_ECHO_HANDLER, false) ) {
      responseHeader.add("handler", handler.getName() );
    }
    String ep = params.get( CommonParams.HEADER_ECHO_PARAMS, null );
    if( ep != null ) {
      EchoParamStyle echoParams = EchoParamStyle.get( ep );
      if( echoParams == null ) {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Invalid value '" + ep + "' for " + CommonParams.HEADER_ECHO_PARAMS 
            + " parameter, use '" + EchoParamStyle.EXPLICIT + "' or '" + EchoParamStyle.ALL + "'" );
      }
      if( echoParams == EchoParamStyle.EXPLICIT ) {
        responseHeader.add("params", req.getOriginalParams().toNamedList());
      } else if( echoParams == EchoParamStyle.ALL ) {
        responseHeader.add("params", req.getParams().toNamedList());
      }
    }
  }
  final public static void log(Throwable e) {
    SolrException.logOnce(log,null,e);
  }
  private QueryResponseWriter defaultResponseWriter;
  private final Map<String, QueryResponseWriter> responseWriters = new HashMap<String, QueryResponseWriter>();
  public static final Map<String ,QueryResponseWriter> DEFAULT_RESPONSE_WRITERS ;
  static{
    HashMap<String, QueryResponseWriter> m= new HashMap<String, QueryResponseWriter>();
    m.put("xml", new XMLResponseWriter());
    m.put("standard", m.get("xml"));
    m.put("json", new JSONResponseWriter());
    m.put("python", new PythonResponseWriter());
    m.put("php", new PHPResponseWriter());
    m.put("phps", new PHPSerializedResponseWriter());
    m.put("ruby", new RubyResponseWriter());
    m.put("raw", new RawResponseWriter());
    m.put("javabin", new BinaryResponseWriter());
    DEFAULT_RESPONSE_WRITERS = Collections.unmodifiableMap(m);
  }
  private void initWriters() {
    defaultResponseWriter = initPlugins(responseWriters, QueryResponseWriter.class);
    for (Map.Entry<String, QueryResponseWriter> entry : DEFAULT_RESPONSE_WRITERS.entrySet()) {
      if(responseWriters.get(entry.getKey()) == null) responseWriters.put(entry.getKey(), entry.getValue());
    }
    if (defaultResponseWriter == null) {
      defaultResponseWriter = responseWriters.get("standard");
    }
  }
  public final QueryResponseWriter getQueryResponseWriter(String writerName) {
    if (writerName != null) {
        QueryResponseWriter writer = responseWriters.get(writerName);
        if (writer != null) {
            return writer;
        }
    }
    return defaultResponseWriter;
  }
  public final QueryResponseWriter getQueryResponseWriter(SolrQueryRequest request) {
    return getQueryResponseWriter(request.getParams().get(CommonParams.WT)); 
  }
  private final Map<String, QParserPlugin> qParserPlugins = new HashMap<String, QParserPlugin>();
  private void initQParsers() {
    initPlugins(qParserPlugins,QParserPlugin.class);
    for (int i=0; i<QParserPlugin.standardPlugins.length; i+=2) {
     try {
       String name = (String)QParserPlugin.standardPlugins[i];
       if (null == qParserPlugins.get(name)) {
         Class<QParserPlugin> clazz = (Class<QParserPlugin>)QParserPlugin.standardPlugins[i+1];
         QParserPlugin plugin = clazz.newInstance();
         qParserPlugins.put(name, plugin);
         plugin.init(null);
       }
     } catch (Exception e) {
       throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
     }
    }
  }
  public QParserPlugin getQueryPlugin(String parserName) {
    QParserPlugin plugin = qParserPlugins.get(parserName);
    if (plugin != null) return plugin;
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unknown query type '"+parserName+"'");
  }
  private final HashMap<String, ValueSourceParser> valueSourceParsers = new HashMap<String, ValueSourceParser>();
  private void initValueSourceParsers() {
    initPlugins(valueSourceParsers,ValueSourceParser.class);
    for (Map.Entry<String, ValueSourceParser> entry : ValueSourceParser.standardValueSourceParsers.entrySet()) {
      try {
        String name = entry.getKey();
        if (null == valueSourceParsers.get(name)) {
          ValueSourceParser valueSourceParser = entry.getValue();
          valueSourceParsers.put(name, valueSourceParser);
          valueSourceParser.init(null);
        }
      } catch (Exception e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      }
    }
  }
  public <T> T initPlugins(Map<String ,T> registry, Class<T> type, String defClassName){
    return initPlugins(solrConfig.getPluginInfos(type.getName()), registry, type, defClassName);
  }
  public <T> T initPlugins(List<PluginInfo> pluginInfos, Map<String, T> registry, Class<T> type, String defClassName) {
    T def = null;
    for (PluginInfo info : pluginInfos) {
      T o = createInitInstance(info,type, type.getSimpleName(), defClassName);
      registry.put(info.name, o);
      if(info.isDefault()){
        def = o;
      }
    }
    return def;
  }
  public <T> List<T> initPlugins(List<PluginInfo> pluginInfos, Class<T> type, String defClassName) {
    if(pluginInfos.isEmpty()) return Collections.emptyList();
    List<T> result = new ArrayList<T>();
    for (PluginInfo info : pluginInfos) result.add(createInitInstance(info,type, type.getSimpleName(), defClassName));
    return result;
  }
  public <T> T initPlugins(Map<String, T> registry, Class<T> type) {
    return initPlugins(registry, type, null);
  }
  public ValueSourceParser getValueSourceParser(String parserName) {
    return valueSourceParsers.get(parserName);
  }
  private void initDeprecatedSupport()
  {
    String gettable = solrConfig.get("admin/gettableFiles", null );
    if( gettable != null ) {
      log.warn( 
          "solrconfig.xml uses deprecated <admin/gettableFiles>, Please "+
          "update your config to use the ShowFileRequestHandler." );
      if( getRequestHandler( "/admin/file" ) == null ) {
        NamedList<String> invariants = new NamedList<String>();
        Set<String> hide = new HashSet<String>();
        File configdir = new File( solrConfig.getResourceLoader().getConfigDir() ); 
        if( configdir.exists() && configdir.isDirectory() ) {
          for( String file : configdir.list() ) {
            hide.add( file.toUpperCase() );
          }
        }
        StringTokenizer st = new StringTokenizer( gettable );
        while( st.hasMoreTokens() ) {
          hide.remove( st.nextToken().toUpperCase() );
        }
        for( String s : hide ) {
          invariants.add( ShowFileRequestHandler.HIDDEN, s );
        }
        NamedList<Object> args = new NamedList<Object>();
        args.add( "invariants", invariants );
        ShowFileRequestHandler handler = new ShowFileRequestHandler();
        handler.init( args );
        reqHandlers.register("/admin/file", handler);
        log.warn( "adding ShowFileRequestHandler with hidden files: "+hide );
      }
    }
    String facetSort = solrConfig.get("//bool[@name='facet.sort']", null);
    if (facetSort != null) {
      log.warn( 
          "solrconfig.xml uses deprecated <bool name='facet.sort'>. Please "+
          "update your config to use <string name='facet.sort'>.");
    }
  } 
  public CoreDescriptor getCoreDescriptor() {
    return coreDescriptor;
  }
  public IndexDeletionPolicyWrapper getDeletionPolicy(){
    return solrDelPolicy;
  }
  public String getVersion() {
    return SolrCore.version;
  }
  public String getDescription() {
    return "SolrCore";
  }
  public Category getCategory() {
    return Category.CORE;
  }
  public String getSourceId() {
    return "$Id: SolrCore.java 911216 2010-02-17 23:09:12Z hossman $";
  }
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/core/SolrCore.java $";
  }
  public URL[] getDocs() {
    return null;
  }
  public NamedList getStatistics() {
    NamedList lst = new SimpleOrderedMap();
    lst.add("coreName", name==null ? "(null)" : name);
    lst.add("startTime", new Date(startTime));
    lst.add("refCount", getOpenCount());
    lst.add("aliases", getCoreDescriptor().getCoreContainer().getCoreNames(this));
    return lst;
  }
}
