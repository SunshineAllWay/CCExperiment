package org.apache.solr.core;
import org.apache.solr.common.util.DOMUtil;
import org.apache.solr.common.util.RegexFileFilter;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.PingRequestHandler;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.search.CacheConfig;
import org.apache.solr.search.FastLRUCache;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.ValueSourceParser;
import org.apache.solr.update.SolrIndexConfig;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.apache.solr.spelling.QueryConverter;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
public class SolrConfig extends Config {
  public static final Logger log = LoggerFactory.getLogger(SolrConfig.class);
  public static final String DEFAULT_CONF_FILE = "solrconfig.xml";
  @Deprecated
  public static SolrConfig config = null; 
  public static final Collection<Throwable> severeErrors = new HashSet<Throwable>();
  public SolrConfig()
  throws ParserConfigurationException, IOException, SAXException {
    this( (SolrResourceLoader) null, DEFAULT_CONF_FILE, null );
  }
  public SolrConfig(String name)
  throws ParserConfigurationException, IOException, SAXException {
    this( (SolrResourceLoader) null, name, null);
  }
  public SolrConfig(String name, InputStream is)
  throws ParserConfigurationException, IOException, SAXException {
    this( (SolrResourceLoader) null, name, is );
  }
  public SolrConfig(String instanceDir, String name, InputStream is)
  throws ParserConfigurationException, IOException, SAXException {
    this(new SolrResourceLoader(instanceDir), name, is);
  }
  SolrConfig(SolrResourceLoader loader, String name, InputStream is)
  throws ParserConfigurationException, IOException, SAXException {
    super(loader, name, is, "/config/");
    initLibs();
    defaultIndexConfig = new SolrIndexConfig(this, null, null);
    mainIndexConfig = new SolrIndexConfig(this, "mainIndex", defaultIndexConfig);
    reopenReaders = getBool("mainIndex/reopenReaders", true);
    booleanQueryMaxClauseCount = getInt("query/maxBooleanClauses", BooleanQuery.getMaxClauseCount());
    luceneMatchVersion = getLuceneVersion("luceneMatchVersion", Version.LUCENE_24);
    log.info("Using Lucene MatchVersion: " + luceneMatchVersion);
    filtOptEnabled = getBool("query/boolTofilterOptimizer/@enabled", false);
    filtOptCacheSize = getInt("query/boolTofilterOptimizer/@cacheSize",32);
    filtOptThreshold = getFloat("query/boolTofilterOptimizer/@threshold",.05f);
    useFilterForSortedQuery = getBool("query/useFilterForSortedQuery", false);
    queryResultWindowSize = getInt("query/queryResultWindowSize", 1);
    queryResultMaxDocsCached = getInt("query/queryResultMaxDocsCached", Integer.MAX_VALUE);
    enableLazyFieldLoading = getBool("query/enableLazyFieldLoading", false);
    filterCacheConfig = CacheConfig.getConfig(this, "query/filterCache");
    queryResultCacheConfig = CacheConfig.getConfig(this, "query/queryResultCache");
    documentCacheConfig = CacheConfig.getConfig(this, "query/documentCache");
    CacheConfig conf = CacheConfig.getConfig(this, "query/fieldValueCache");
    if (conf == null) {
      Map<String,String> args = new HashMap<String,String>();
      args.put("name","fieldValueCache");
      args.put("size","10000");
      args.put("initialSize","10");
      args.put("showItems","-1");
      conf = new CacheConfig(FastLRUCache.class, args, null);
    }
    fieldValueCacheConfig = conf;
    unlockOnStartup = getBool("mainIndex/unlockOnStartup", false);
    useColdSearcher = getBool("query/useColdSearcher",false);
    dataDir = get("dataDir", null);
    if (dataDir != null && dataDir.length()==0) dataDir=null;
    userCacheConfigs = CacheConfig.getMultipleConfigs(this, "query/cache");
    org.apache.solr.search.SolrIndexSearcher.initRegenerators(this);
    hashSetInverseLoadFactor = 1.0f / getFloat("//HashDocSet/@loadFactor",0.75f);
    hashDocSetMaxSize= getInt("//HashDocSet/@maxSize",3000);
    pingQueryParams = readPingQueryParams(this);
    httpCachingConfig = new HttpCachingConfig(this);
    Node jmx = (Node) getNode("jmx", false);
    if (jmx != null) {
      jmxConfig = new JmxConfiguration(true, get("jmx/@agentId", null), get(
          "jmx/@serviceUrl", null));
    } else {
      jmxConfig = new JmxConfiguration(false, null, null);
    }
     maxWarmingSearchers = getInt("query/maxWarmingSearchers",Integer.MAX_VALUE);
     loadPluginInfo(SolrRequestHandler.class,"requestHandler",true, true);
     loadPluginInfo(QParserPlugin.class,"queryParser",true, true);
     loadPluginInfo(QueryResponseWriter.class,"queryResponseWriter",true, true);
     loadPluginInfo(ValueSourceParser.class,"valueSourceParser",true, true);
     loadPluginInfo(SearchComponent.class,"searchComponent",true, true);
     loadPluginInfo(QueryConverter.class,"queryConverter",true, true);
     loadPluginInfo(SolrEventListener.class, "//listener",false, true);
     loadPluginInfo(DirectoryFactory.class,"directoryFactory",false, true);
     loadPluginInfo(IndexDeletionPolicy.class,"mainIndex/deletionPolicy",false, true);
     loadPluginInfo(IndexReaderFactory.class,"indexReaderFactory",false, true);
     loadPluginInfo(UpdateRequestProcessorChain.class,"updateRequestProcessorChain",false, false);
     loadPluginInfo(SolrHighlighter.class,"highlighting",false, false);
     if( pluginStore.containsKey( SolrHighlighter.class.getName() ) )
       log.warn( "Deprecated syntax found. <highlighting/> should move to <searchComponent/>" );
     updateHandlerInfo = loadUpdatehandlerInfo();
    Config.log.info("Loaded SolrConfig: " + name);
    config = this;
  }
  protected UpdateHandlerInfo loadUpdatehandlerInfo() {
    return new UpdateHandlerInfo(get("updateHandler/@class",null),
            getInt("updateHandler/autoCommit/maxDocs",-1),
            getInt("updateHandler/autoCommit/maxTime",-1),
            getInt("updateHandler/commitIntervalLowerBound",-1));
  }
  private void loadPluginInfo(Class clazz, String tag, boolean requireName, boolean requireClass) {
    List<PluginInfo> result = readPluginInfos(tag, requireName, requireClass);
    if(!result.isEmpty()) pluginStore.put(clazz.getName(),result);
  }
  public List<PluginInfo> readPluginInfos(String tag, boolean requireName, boolean requireClass) {
    ArrayList<PluginInfo> result = new ArrayList<PluginInfo>();
    NodeList nodes = (NodeList) evaluate(tag, XPathConstants.NODESET);
    for (int i=0; i<nodes.getLength(); i++) {
      PluginInfo pluginInfo = new PluginInfo(nodes.item(i), "[solrconfig.xml] " + tag, requireName, requireClass);
      if(pluginInfo.isEnabled()) result.add(pluginInfo);
    }
    return result;
  }
  public final int booleanQueryMaxClauseCount;
  public final boolean filtOptEnabled;
  public final int filtOptCacheSize;
  public final float filtOptThreshold;
  public final CacheConfig filterCacheConfig ;
  public final CacheConfig queryResultCacheConfig;
  public final CacheConfig documentCacheConfig;
  public final CacheConfig fieldValueCacheConfig;
  public final CacheConfig[] userCacheConfigs;
  public final boolean useFilterForSortedQuery;
  public final int queryResultWindowSize;
  public final int queryResultMaxDocsCached;
  public final boolean enableLazyFieldLoading;
  public final boolean reopenReaders;
  public final float hashSetInverseLoadFactor;
  public final int hashDocSetMaxSize;
  public final SolrIndexConfig defaultIndexConfig;
  public final SolrIndexConfig mainIndexConfig;
  protected UpdateHandlerInfo updateHandlerInfo ;
  private Map<String, List<PluginInfo>> pluginStore = new LinkedHashMap<String, List<PluginInfo>>();
  public final int maxWarmingSearchers;
  public final boolean unlockOnStartup;
  public final boolean useColdSearcher;
  public final Version luceneMatchVersion;
  protected String dataDir;
  public final JmxConfiguration jmxConfig;
  private final HttpCachingConfig httpCachingConfig;
  public HttpCachingConfig getHttpCachingConfig() {
    return httpCachingConfig;
  }
  @Deprecated
  private final NamedList pingQueryParams;
  static private NamedList readPingQueryParams(SolrConfig config) {  
    String urlSnippet = config.get("admin/pingQuery", "").trim();
    StringTokenizer qtokens = new StringTokenizer(urlSnippet,"&");
    String tok;
    NamedList params = new NamedList();
    while (qtokens.hasMoreTokens()) {
      tok = qtokens.nextToken();
      String[] split = tok.split("=", 2);
      params.add(split[0], split[1]);
    }
    if (0 < params.size()) {
      log.warn("The <pingQuery> syntax is deprecated, " +
               "please use PingRequestHandler instead");
    }
    return params;
  }
  @Deprecated
  public SolrQueryRequest getPingQueryRequest(SolrCore core) {
    if(pingQueryParams.size() == 0) {
      throw new IllegalStateException
        ("<pingQuery> not configured (consider registering " +
         "PingRequestHandler with the name '/admin/ping' instead)");
    }
    return new LocalSolrQueryRequest(core, pingQueryParams);
  }
  public static class JmxConfiguration {
    public boolean enabled = false;
    public String agentId;
    public String serviceUrl;
    public JmxConfiguration(boolean enabled, String agentId, String serviceUrl) {
      this.enabled = enabled;
      this.agentId = agentId;
      this.serviceUrl = serviceUrl;
    }
  }
  public static class HttpCachingConfig {
    private final static String CACHE_PRE
      = "requestDispatcher/httpCaching/";
    private final static Pattern MAX_AGE
      = Pattern.compile("\\bmax-age=(\\d+)");
    public static enum LastModFrom {
      OPENTIME, DIRLASTMOD, BOGUS;
      public static LastModFrom parse(final String s) {
        try {
          return valueOf(s.toUpperCase());
        } catch (Exception e) {
          log.warn( "Unrecognized value for lastModFrom: " + s, e);
          return BOGUS;
        }
      }
    }
    private final boolean never304;
    private final String etagSeed;
    private final String cacheControlHeader;
    private final Long maxAge;
    private final LastModFrom lastModFrom;
    private HttpCachingConfig(SolrConfig conf) {
      never304 = conf.getBool(CACHE_PRE+"@never304", false);
      etagSeed = conf.get(CACHE_PRE+"@etagSeed", "Solr");
      lastModFrom = LastModFrom.parse(conf.get(CACHE_PRE+"@lastModFrom",
                                               "openTime"));
      cacheControlHeader = conf.get(CACHE_PRE+"cacheControl",null);
      Long tmp = null; 
      if (null != cacheControlHeader) {
        try { 
          final Matcher ttlMatcher = MAX_AGE.matcher(cacheControlHeader);
          final String ttlStr = ttlMatcher.find() ? ttlMatcher.group(1) : null;
          tmp = (null != ttlStr && !"".equals(ttlStr))
            ? Long.valueOf(ttlStr)
            : null;
        } catch (Exception e) {
          log.warn( "Ignoring exception while attempting to " +
                    "extract max-age from cacheControl config: " +
                    cacheControlHeader, e);
        }
      }
      maxAge = tmp;
    }
    public boolean isNever304() { return never304; }
    public String getEtagSeed() { return etagSeed; }
    public String getCacheControlHeader() { return cacheControlHeader; }
    public Long getMaxAge() { return maxAge; }
    public LastModFrom getLastModFrom() { return lastModFrom; }
  }
  public static class UpdateHandlerInfo{
    public final String className;
    public final int autoCommmitMaxDocs,autoCommmitMaxTime,commitIntervalLowerBound;
    public UpdateHandlerInfo(String className, int autoCommmitMaxDocs, int autoCommmitMaxTime, int commitIntervalLowerBound) {
      this.className = className;
      this.autoCommmitMaxDocs = autoCommmitMaxDocs;
      this.autoCommmitMaxTime = autoCommmitMaxTime;
      this.commitIntervalLowerBound = commitIntervalLowerBound;
    } 
  }
  public UpdateHandlerInfo getUpdateHandlerInfo() { return updateHandlerInfo; }
  public String getDataDir() { return dataDir; }
  public List<PluginInfo> getPluginInfos(String  type){
    List<PluginInfo> result = pluginStore.get(type);
    return result == null ?
            (List<PluginInfo>) Collections.EMPTY_LIST:
            result; 
  }
  public PluginInfo getPluginInfo(String  type){
    List<PluginInfo> result = pluginStore.get(type);
    return result == null || result.isEmpty() ? null: result.get(0);
  }
  private void initLibs() {
    NodeList nodes = (NodeList) evaluate("lib", XPathConstants.NODESET);
    if (nodes==null || nodes.getLength()==0)
      return;
    log.info("Adding specified lib dirs to ClassLoader");
     for (int i=0; i<nodes.getLength(); i++) {
       Node node = nodes.item(i);
       String baseDir = DOMUtil.getAttr(node, "dir");
       String path = DOMUtil.getAttr(node, "path");
       if (null != baseDir) {
         String regex = DOMUtil.getAttr(node, "regex");
         FileFilter filter = (null == regex) ? null : new RegexFileFilter(regex);
         getResourceLoader().addToClassLoader(baseDir, filter);
       } else if (null != path) {
         getResourceLoader().addToClassLoader(path);
       } else {
         throw new RuntimeException
           ("lib: missing mandatory attributes: 'dir' or 'path'");
       }
     }
  }
}
