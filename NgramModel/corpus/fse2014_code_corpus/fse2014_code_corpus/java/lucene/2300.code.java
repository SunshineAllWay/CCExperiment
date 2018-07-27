package org.apache.solr.core;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.util.DOMUtil;
import org.apache.solr.common.util.XML;
import org.apache.solr.common.util.FileUtils;
import org.apache.solr.handler.admin.CoreAdminHandler;
import org.apache.solr.schema.IndexSchema;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
public class CoreContainer 
{
  private static final String DEFAULT_DEFAULT_CORE_NAME = "collection1";
  protected static Logger log = LoggerFactory.getLogger(CoreContainer.class);
  protected final Map<String, SolrCore> cores = new LinkedHashMap<String, SolrCore>();
  protected boolean persistent = false;
  protected String adminPath = null;
  protected String managementPath = null;
  protected CoreAdminHandler coreAdminHandler = null;
  protected File configFile = null;
  protected String libDir = null;
  protected ClassLoader libLoader = null;
  protected SolrResourceLoader loader = null;
  protected Properties containerProperties;
  protected Map<String ,IndexSchema> indexSchemaCache;
  protected String adminHandler;
  protected boolean shareSchema;
  protected String solrHome;
  protected String solrConfigFilenameOverride;
  private String defaultCoreName = "";
  private boolean defaultAbortOnConfigError = false;
  private int numCoresAbortOnConfigError = 0;
  public CoreContainer() {
    solrHome = SolrResourceLoader.locateSolrHome();
  }
  public Properties getContainerProperties() {
    return containerProperties;
  }
  public static class Initializer {
    protected String solrConfigFilename = null;
    protected boolean abortOnConfigurationError = true;
    public boolean isAbortOnConfigurationError() {
      return abortOnConfigurationError;
    }
    public void setAbortOnConfigurationError(boolean abortOnConfigurationError) {
      this.abortOnConfigurationError = abortOnConfigurationError;
    }
    public String getSolrConfigFilename() {
      return solrConfigFilename;
    }
   @Deprecated
    public void setSolrConfigFilename(String solrConfigFilename) {
      this.solrConfigFilename = solrConfigFilename;
    }
    public CoreContainer initialize() throws IOException,
        ParserConfigurationException, SAXException {
      CoreContainer cores = null;
      String solrHome = SolrResourceLoader.locateSolrHome();
      File fconf = new File(solrHome, solrConfigFilename == null ? "solr.xml"
          : solrConfigFilename);
      log.info("looking for solr.xml: " + fconf.getAbsolutePath());
      cores = new CoreContainer();
      cores.solrConfigFilenameOverride = solrConfigFilename;
      if (fconf.exists()) {
        cores.defaultAbortOnConfigError = false;
        cores.load(solrHome, fconf);
      } else {
        cores.defaultAbortOnConfigError = abortOnConfigurationError;
        cores.load(solrHome, new ByteArrayInputStream(DEF_SOLR_XML.getBytes()));
        cores.configFile = fconf;
      }
      setAbortOnConfigurationError(0 < cores.numCoresAbortOnConfigError);
      solrConfigFilename = cores.getConfigFile().getName();
      return cores;
    }
  }
  private static Properties getCoreProps(String instanceDir, String file, Properties defaults) {
    if(file == null) file = "conf"+File.separator+ "solrcore.properties";
    File corePropsFile = new File(file);
    if(!corePropsFile.isAbsolute()){
      corePropsFile = new File(instanceDir, file);
    }
    Properties p = defaults;
    if (corePropsFile.exists() && corePropsFile.isFile()) {
      p = new Properties(defaults);
      InputStream is = null;
      try {
        is = new FileInputStream(corePropsFile);
        p.load(is);
      } catch (IOException e) {
        log.warn("Error loading properties ",e);
      } finally{
        IOUtils.closeQuietly(is);        
      }
    }
    return p;
  }
  public CoreContainer(String dir, File configFile) throws ParserConfigurationException, IOException, SAXException 
  {
    this.load(dir, configFile);
  }
  public CoreContainer(SolrResourceLoader loader) {
    this.loader = loader;
    this.solrHome = loader.getInstanceDir();
  }
  public CoreContainer(String solrHome) {
    this.solrHome = solrHome;
  }
  public void load(String dir, File configFile ) throws ParserConfigurationException, IOException, SAXException {
    this.configFile = configFile;
    this.load(dir, new FileInputStream(configFile));
  } 
  public void load(String dir, InputStream cfgis)
      throws ParserConfigurationException, IOException, SAXException {
    this.loader = new SolrResourceLoader(dir);
    solrHome = loader.getInstanceDir();
    try {
      Config cfg = new Config(loader, null, cfgis, null);
      String dcoreName = cfg.get("solr/cores/@defaultCoreName", null);
      if(dcoreName != null) {
        defaultCoreName = dcoreName;
      }
      persistent = cfg.getBool( "solr/@persistent", false );
      libDir     = cfg.get(     "solr/@sharedLib", null);
      adminPath  = cfg.get(     "solr/cores/@adminPath", null );
      shareSchema = cfg.getBool("solr/cores/@shareSchema", false );
      if(shareSchema){
        indexSchemaCache = new ConcurrentHashMap<String ,IndexSchema>();
      }
      adminHandler  = cfg.get("solr/cores/@adminHandler", null );
      managementPath  = cfg.get("solr/cores/@managementPath", null );
      if (libDir != null) {
        File f = FileUtils.resolvePath(new File(dir), libDir);
        log.info( "loading shared library: "+f.getAbsolutePath() );
        libLoader = SolrResourceLoader.createClassLoader(f, null);
      }
      if (adminPath != null) {
        if (adminHandler == null) {
          coreAdminHandler = new CoreAdminHandler(this);
        } else {
          coreAdminHandler = this.createMultiCoreHandler(adminHandler);
        }
      }
      try {
        containerProperties = readProperties(cfg, ((NodeList) cfg.evaluate("solr", XPathConstants.NODESET)).item(0));
      } catch (Throwable e) {
        SolrConfig.severeErrors.add(e);
        SolrException.logOnce(log,null,e);
      }
      NodeList nodes = (NodeList)cfg.evaluate("solr/cores/core", XPathConstants.NODESET);
      boolean defaultCoreFound = false;
      for (int i=0; i<nodes.getLength(); i++) {
        Node node = nodes.item(i);
        try {
          String name = DOMUtil.getAttr(node, "name", null);
          if(name.equals(defaultCoreName)){
            if(defaultCoreFound) throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,"Only one 'DEFAULT_CORE' is allowed ");            
            defaultCoreFound = true;
            name="";
          }
          CoreDescriptor p = new CoreDescriptor(this, name, DOMUtil.getAttr(node, "instanceDir", null));
          String opt = DOMUtil.getAttr(node, "config", null);
          if(solrConfigFilenameOverride != null && name.equals("")) {
            p.setConfigName(solrConfigFilenameOverride);
          } else if (opt != null) {
            p.setConfigName(opt);
          }
          opt = DOMUtil.getAttr(node, "schema", null);
          if (opt != null) {
            p.setSchemaName(opt);
          }
          opt = DOMUtil.getAttr(node, "properties", null);
          if (opt != null) {
            p.setPropertiesName(opt);
          }
          opt = DOMUtil.getAttr(node, CoreAdminParams.DATA_DIR, null);
          if (opt != null) {
            p.setDataDir(opt);
          }
          p.setCoreProperties(readProperties(cfg, node));
          SolrCore core = create(p);
          register(name, core, false);
        }
        catch (Throwable ex) {
          SolrConfig.severeErrors.add( ex );
          SolrException.logOnce(log,null,ex);
        }
      }
    }
    finally {
      if (cfgis != null) {
        try { cfgis.close(); } catch (Exception xany) {}
      }
    }
  }
  private Properties readProperties(Config cfg, Node node) throws XPathExpressionException {
    XPath xpath = cfg.getXPath();
    NodeList props = (NodeList) xpath.evaluate("property", node, XPathConstants.NODESET);
    Properties properties = new Properties();
    for (int i=0; i<props.getLength(); i++) {
      Node prop = props.item(i);
      properties.setProperty(DOMUtil.getAttr(prop, "name"), DOMUtil.getAttr(prop, "value"));
    }
    return properties;
  }
  private boolean isShutDown = false;
  public void shutdown() {
    synchronized(cores) {
      try {
        for(SolrCore core : cores.values()) {
          core.close();
        }
        cores.clear();
      } finally {
        isShutDown = true;
      }
    }
  }
  @Override
  protected void finalize() throws Throwable {
    try {
      if(!isShutDown){
        log.error("CoreContainer was not shutdown prior to finalize(), indicates a bug -- POSSIBLE RESOURCE LEAK!!!");
        shutdown();
      }
    } finally {
      super.finalize();
    }
  }
  public SolrCore register(String name, SolrCore core, boolean returnPrevNotClosed) {
    if( core == null ) {
      throw new RuntimeException( "Can not register a null core." );
    }
    if( name == null ||
        name.indexOf( '/'  ) >= 0 ||
        name.indexOf( '\\' ) >= 0 ){
      throw new RuntimeException( "Invalid core name: "+name );
    }
    SolrCore old = null;
    synchronized (cores) {
      old = cores.put(name, core);
      core.setName(name);
    }
    if( old == null || old == core) {
      log.info( "registering core: "+name );
      return null;
    }
    else {
      log.info( "replacing core: "+name );
      if (!returnPrevNotClosed) {
        old.close();
      }
      return old;
    }
  }
  public SolrCore register(SolrCore core, boolean returnPrev) {
    return register(core.getName(), core, returnPrev);
  }
  public SolrCore create(CoreDescriptor dcore)  throws ParserConfigurationException, IOException, SAXException {
    File idir = new File(dcore.getInstanceDir());
    if (!idir.isAbsolute()) {
      idir = new File(solrHome, dcore.getInstanceDir());
    }
    String instanceDir = idir.getPath();
    SolrResourceLoader solrLoader = new SolrResourceLoader(instanceDir, libLoader, getCoreProps(instanceDir, dcore.getPropertiesName(),dcore.getCoreProperties()));
    SolrConfig config = new SolrConfig(solrLoader, dcore.getConfigName(), null);
    if (config.getBool("abortOnConfigurationError",defaultAbortOnConfigError)) {
      numCoresAbortOnConfigError++;
    }
    IndexSchema schema = null;
    if(indexSchemaCache != null){
      File schemaFile = new File(dcore.getSchemaName());
      if (!schemaFile.isAbsolute()) {
        schemaFile = new File(solrLoader.getInstanceDir() + "conf" + File.separator + dcore.getSchemaName());
      }
      if(schemaFile. exists()){
        String key = schemaFile.getAbsolutePath()+":"+new SimpleDateFormat("yyyyMMddhhmmss").format(new Date(schemaFile.lastModified()));
        schema = indexSchemaCache.get(key);
        if(schema == null){
          log.info("creating new schema object for core: " + dcore.name);
          schema = new IndexSchema(config, dcore.getSchemaName(), null);
          indexSchemaCache.put(key,schema);
        } else {
          log.info("re-using schema object for core: " + dcore.name);
        }
      }
    }
    if(schema == null){
      schema = new IndexSchema(config, dcore.getSchemaName(), null);
    }
    SolrCore core = new SolrCore(dcore.getName(), null, config, schema, dcore);
    return core;
  }
  public Collection<SolrCore> getCores() {
    List<SolrCore> lst = new ArrayList<SolrCore>();
    synchronized (cores) {
      lst.addAll(this.cores.values());
    }
    return lst;
  }
  public Collection<String> getCoreNames() {
    List<String> lst = new ArrayList<String>();
    synchronized (cores) {
      lst.addAll(this.cores.keySet());
    }
    return lst;
  }
  public Collection<String> getCoreNames(SolrCore core) {
    List<String> lst = new ArrayList<String>();
    synchronized (cores) {
      for (Map.Entry<String,SolrCore> entry : cores.entrySet()) {
        if (core == entry.getValue()) {
          lst.add(entry.getKey());
        }
      }
    }
    return lst;
  }
  public void reload(String name) throws ParserConfigurationException, IOException, SAXException {
    name= checkDefault(name);
    SolrCore core;
    synchronized(cores) {
      core = cores.get(name);
    }
    if (core == null)
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "No such core: " + name );
    SolrCore newCore = create(core.getCoreDescriptor());
    register(name, newCore, false);
  }
  private String checkDefault(String name) {
    return name.length() == 0  || defaultCoreName.equals(name) || name.trim().length() == 0 ? "" : name;
  } 
  public void swap(String n0, String n1) {
    if( n0 == null || n1 == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Can not swap unnamed cores." );
    }
    n0 = checkDefault(n0);
    n1 = checkDefault(n1);
    synchronized( cores ) {
      SolrCore c0 = cores.get(n0);
      SolrCore c1 = cores.get(n1);
      if (c0 == null)
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "No such core: " + n0 );
      if (c1 == null)
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "No such core: " + n1 );
      cores.put(n0, c1);
      cores.put(n1, c0);
      c0.setName(n1);
      c0.getCoreDescriptor().name = n1;
      c1.setName(n0);
      c1.getCoreDescriptor().name = n0;
    }
    log.info("swaped: "+n0 + " with " + n1);
  }
  public SolrCore remove( String name ) {
    name = checkDefault(name);    
    synchronized(cores) {
      return cores.remove( name );
    }
  }
  public SolrCore getCore(String name) {
    name= checkDefault(name);
    synchronized(cores) {
      SolrCore core = cores.get(name);
      if (core != null)
        core.open();  
      return core;
    }
  }
  protected CoreAdminHandler createMultiCoreHandler(final String adminHandlerClass) {
    SolrResourceLoader loader = new SolrResourceLoader(null, libLoader, null);
    Object obj = loader.newAdminHandlerInstance(CoreContainer.this, adminHandlerClass);
    if ( !(obj instanceof CoreAdminHandler))
    {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
          "adminHandlerClass is not of type "+ CoreAdminHandler.class );
    }
    return (CoreAdminHandler) obj;
  }
  public CoreAdminHandler getMultiCoreHandler() {
    return coreAdminHandler;
  }
  public String getDefaultCoreName() {
    return defaultCoreName;
  }
  public boolean isPersistent() {
    return persistent;
  }
  public void setPersistent(boolean persistent) {
    this.persistent = persistent;
  }
  public String getAdminPath() {
    return adminPath;
  }
  public void setAdminPath(String adminPath) {
      this.adminPath = adminPath;
  }
  public String getManagementPath() {
    return managementPath;
  }
  public void setManagementPath(String path) {
    this.managementPath = path;
  }
  public File getConfigFile() {
    return configFile;
  }
  public void persist() {
    persistFile(null);
  }
  public void persistFile(File file) {
    log.info("Persisting cores config to " + (file==null ? configFile : file));
    File tmpFile = null;
    try {
      if (file == null) {
        file = tmpFile = File.createTempFile("solr", ".xml", configFile.getParentFile());
      }
      java.io.FileOutputStream out = new java.io.FileOutputStream(file);
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        persist(writer);
        writer.flush();
        writer.close();
        out.close();
        if (tmpFile != null) {
          if (tmpFile.renameTo(configFile))
            tmpFile = null;
          else
            fileCopy(tmpFile, configFile);
        }
    } 
    catch(java.io.FileNotFoundException xnf) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, xnf);
    } 
    catch(java.io.IOException xio) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, xio);
    } 
    finally {
      if (tmpFile != null) {
        if (!tmpFile.delete())
          tmpFile.deleteOnExit();
      }
    }
  }
  void persist(Writer w) throws IOException {
    w.write("<?xml version='1.0' encoding='UTF-8'?>");
    w.write("<solr");
    if (this.libDir != null) {
      writeAttribute(w,"sharedLib",libDir);
    }
    writeAttribute(w,"persistent",isPersistent());
    w.write(">\n");
    if (containerProperties != null && !containerProperties.isEmpty())  {
      writeProperties(w, containerProperties);
    }
    w.write("<cores");
    writeAttribute(w, "adminPath",adminPath);
    if(adminHandler != null) writeAttribute(w, "adminHandler",adminHandler);
    if(shareSchema) writeAttribute(w, "shareSchema","true");
    w.write(">\n");
    synchronized(cores) {
      for (SolrCore solrCore : cores.values()) {
        persist(w,solrCore.getCoreDescriptor());
      }
    }
    w.write("</cores>\n");
    w.write("</solr>\n");
  }
  private void writeAttribute(Writer w, String name, Object value) throws IOException {
    if (value == null) return;
    w.write(" ");
    w.write(name);
    w.write("=\"");
    XML.escapeAttributeValue(value.toString(), w);
    w.write("\"");
  }
  void persist(Writer w, CoreDescriptor dcore) throws IOException {
    w.write("  <core");
    writeAttribute(w,"name",dcore.name);
    writeAttribute(w,"instanceDir",dcore.getInstanceDir());
    String opt = dcore.getConfigName();
    if (opt != null && !opt.equals(dcore.getDefaultConfigName())) {
      writeAttribute(w, "config",opt);
    }
    opt = dcore.getSchemaName();
    if (opt != null && !opt.equals(dcore.getDefaultSchemaName())) {
      writeAttribute(w,"schema",opt);
    }
    opt = dcore.getPropertiesName();
    if (opt != null) {
      writeAttribute(w,"properties",opt);
    }
    opt = dcore.dataDir;
    if (opt != null) writeAttribute(w,"dataDir",opt);
    if (dcore.getCoreProperties() == null || dcore.getCoreProperties().isEmpty())
      w.write("/>\n"); 
    else  {
      w.write(">\n");
      writeProperties(w, dcore.getCoreProperties());
      w.write("</core>");
    }
  }
  private void writeProperties(Writer w, Properties props) throws IOException {
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      w.write("<property");
      writeAttribute(w,"name",entry.getKey());
      writeAttribute(w,"value",entry.getValue());
      w.write("/>\n");
    }
  }
  public static void fileCopy(File src, File dest) throws IOException {
    IOException xforward = null;
    FileInputStream fis =  null;
    FileOutputStream fos = null;
    FileChannel fcin = null;
    FileChannel fcout = null;
    try {
      fis = new FileInputStream(src);
      fos = new FileOutputStream(dest);
      fcin = fis.getChannel();
      fcout = fos.getChannel();
      final int MB32 = 32*1024*1024;
      long size = fcin.size();
      long position = 0;
      while (position < size) {
        position += fcin.transferTo(position, MB32, fcout);
      }
    } 
    catch(IOException xio) {
      xforward = xio;
    } 
    finally {
      if (fis   != null) try { fis.close(); fis = null; } catch(IOException xio) {}
      if (fos   != null) try { fos.close(); fos = null; } catch(IOException xio) {}
      if (fcin  != null && fcin.isOpen() ) try { fcin.close();  fcin = null;  } catch(IOException xio) {}
      if (fcout != null && fcout.isOpen()) try { fcout.close(); fcout = null; } catch(IOException xio) {}
    }
    if (xforward != null) {
      throw xforward;
    }
  }
  public String getSolrHome() {
    return solrHome;
  }
  private static final String DEF_SOLR_XML ="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
          "<solr persistent=\"false\">\n" +
          "  <cores adminPath=\"/admin/cores\" defaultCoreName=\"" + DEFAULT_DEFAULT_CORE_NAME + "\">\n" +
          "    <core name=\""+ DEFAULT_DEFAULT_CORE_NAME + "\" instanceDir=\".\" />\n" +
          "  </cores>\n" +
          "</solr>";
}
