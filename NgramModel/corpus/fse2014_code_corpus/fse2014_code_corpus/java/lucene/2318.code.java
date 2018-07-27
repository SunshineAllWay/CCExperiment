package org.apache.solr.core;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.Charset;
import java.lang.reflect.Constructor;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import org.apache.solr.analysis.CharFilterFactory;
import org.apache.solr.analysis.TokenFilterFactory;
import org.apache.solr.analysis.TokenizerFactory;
import org.apache.solr.common.util.FileUtils;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import org.apache.solr.util.plugin.SolrCoreAware;
public class SolrResourceLoader implements ResourceLoader
{
  public static final Logger log = LoggerFactory.getLogger(SolrResourceLoader.class);
  static final String project = "solr";
  static final String base = "org.apache" + "." + project;
  static final String[] packages = {"","analysis.","schema.","handler.","search.","update.","core.","response.","request.","update.processor.","util.", "spelling.", "handler.component.", "handler.dataimport." };
  private URLClassLoader classLoader;
  private final String instanceDir;
  private String dataDir;
  private final List<SolrCoreAware> waitingForCore = new ArrayList<SolrCoreAware>();
  private final List<SolrInfoMBean> infoMBeans = new ArrayList<SolrInfoMBean>();
  private final List<ResourceLoaderAware> waitingForResources = new ArrayList<ResourceLoaderAware>();
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  private final Properties coreProperties;
  public SolrResourceLoader( String instanceDir, ClassLoader parent, Properties coreProperties )
  {
    if( instanceDir == null ) {
      this.instanceDir = SolrResourceLoader.locateSolrHome();
    } else{
      this.instanceDir = normalizeDir(instanceDir);
    }
    log.info("Solr home set to '" + this.instanceDir + "'");
    this.classLoader = createClassLoader(null, parent);
    addToClassLoader("./lib/", null);
    this.coreProperties = coreProperties;
  }
  public SolrResourceLoader( String instanceDir, ClassLoader parent )
  {
    this(instanceDir, parent, null);
  }
  void addToClassLoader(final String baseDir, final FileFilter filter) {
    File base = FileUtils.resolvePath(new File(getInstanceDir()), baseDir);
    this.classLoader = replaceClassLoader(classLoader, base, filter);
  }
  void addToClassLoader(final String path) {
    final File file = FileUtils.resolvePath(new File(getInstanceDir()), path);
    if (file.canRead()) {
      this.classLoader = replaceClassLoader(classLoader, file.getParentFile(),
                                            new FileFilter() {
                                              public boolean accept(File pathname) {
                                                return pathname.equals(file);
                                              }
                                            });
    } else {
      log.error("Can't find (or read) file to add to classloader: " + file);
    }
  }
  private static URLClassLoader replaceClassLoader(final URLClassLoader oldLoader,
                                                   final File base,
                                                   final FileFilter filter) {
    if (null != base && base.canRead() && base.isDirectory()) {
      File[] files = base.listFiles(filter);
      if (null == files || 0 == files.length) return oldLoader;
      URL[] oldElements = oldLoader.getURLs();
      URL[] elements = new URL[oldElements.length + files.length];
      System.arraycopy(oldElements, 0, elements, 0, oldElements.length);
      for (int j = 0; j < files.length; j++) {
        try {
          URL element = files[j].toURI().normalize().toURL();
          log.info("Adding '" + element.toString() + "' to classloader");
          elements[oldElements.length + j] = element;
        } catch (MalformedURLException e) {
          SolrException.log(log, "Can't add element to classloader: " + files[j], e);
        }
      }
      return URLClassLoader.newInstance(elements, oldLoader.getParent());
    }
    return oldLoader;
  }
  static URLClassLoader createClassLoader(final File libDir, ClassLoader parent) {
    if ( null == parent ) {
      parent = Thread.currentThread().getContextClassLoader();
    }
    return replaceClassLoader(URLClassLoader.newInstance(new URL[0], parent),
                              libDir, null);
  }
  public SolrResourceLoader( String instanceDir )
  {
    this( instanceDir, null, null );
  }
  public  static String normalizeDir(String path) {
    return ( path != null && (!(path.endsWith("/") || path.endsWith("\\"))) )? path + File.separator : path;
  }
  public String getConfigDir() {
    return instanceDir + "conf/";
  }
  public String getDataDir()    {
    return dataDir;
  }
  public Properties getCoreProperties() {
    return coreProperties;
  }
  public InputStream openSchema(String name) {
    return openResource(name);
  }
  public InputStream openConfig(String name) {
    return openResource(name);
  }
  public InputStream openResource(String resource) {
    InputStream is=null;
    try {
      File f0 = new File(resource);
      File f = f0;
      if (!f.isAbsolute()) {
        f = new File(getConfigDir() + resource);
      }
      if (f.isFile() && f.canRead()) {
        return new FileInputStream(f);
      } else if (f != f0) { 
        if (f0.isFile() && f0.canRead())
          return new FileInputStream(f0);
      }
      is = classLoader.getResourceAsStream(resource);
    } catch (Exception e) {
      throw new RuntimeException("Error opening " + resource, e);
    }
    if (is==null) {
      throw new RuntimeException("Can't find resource '" + resource + "' in classpath or '" + getConfigDir() + "', cwd="+System.getProperty("user.dir"));
    }
    return is;
  }
  public List<String> getLines(String resource) throws IOException {
    return getLines(resource, UTF_8);
  }
  public List<String> getLines(String resource,
      String encoding) throws IOException {
    return getLines(resource, Charset.forName(encoding));
  }
  public List<String> getLines(String resource, Charset charset) throws IOException{
    BufferedReader input = null;
    ArrayList<String> lines;
    try {
      input = new BufferedReader(new InputStreamReader(openResource(resource),
          charset));
      lines = new ArrayList<String>();
      for (String word=null; (word=input.readLine())!=null;) {
        if (word.startsWith("#")) continue;
        word=word.trim();
        if (word.length()==0) continue;
        lines.add(word);
      }
    } finally {
      if (input != null)
        input.close();
    }
    return lines;
  }
  private static Map<String, String> classNameCache = new ConcurrentHashMap<String, String>();
  public Class findClass(String cname, String... subpackages) {
    if (subpackages == null || subpackages.length == 0 || subpackages == packages) {
      subpackages = packages;
      String  c = classNameCache.get(cname);
      if(c != null) {
        try {
          return Class.forName(c, true, classLoader);
        } catch (ClassNotFoundException e) {
          log.error("Unable to load cached class-name :  "+ c +" for shortname : "+cname + e);
        }
      }
    }
    Class clazz = null;
    try {
      return Class.forName(cname, true, classLoader);
    } catch (ClassNotFoundException e) {
      String newName=cname;
      if (newName.startsWith(project)) {
        newName = cname.substring(project.length()+1);
      }
      for (String subpackage : subpackages) {
        try {
          String name = base + '.' + subpackage + newName;
          log.trace("Trying class name " + name);
          return clazz = Class.forName(name,true,classLoader);
        } catch (ClassNotFoundException e1) {
        }
      }
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "Error loading class '" + cname + "'", e, false);
    }finally{
      if ( clazz != null &&
              clazz.getClassLoader() == SolrResourceLoader.class.getClassLoader() &&
              !cname.equals(clazz.getName()) &&
              (subpackages.length == 0 || subpackages == packages)) {
        classNameCache.put(cname, clazz.getName());
      }
    }
  }
  public Object newInstance(String cname, String ... subpackages) {
    Class clazz = findClass(cname,subpackages);
    if( clazz == null ) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
          "Can not find class: "+cname + " in " + classLoader, false);
    }
    Object obj = null;
    try {
      obj = clazz.newInstance();
    } 
    catch (Exception e) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
          "Error instantiating class: '" + clazz.getName()+"'", e, false );
    }
    if( obj instanceof SolrCoreAware ) {
      assertAwareCompatibility( SolrCoreAware.class, obj );
      waitingForCore.add( (SolrCoreAware)obj );
    }
    if( obj instanceof ResourceLoaderAware ) {
      assertAwareCompatibility( ResourceLoaderAware.class, obj );
      waitingForResources.add( (ResourceLoaderAware)obj );
    }
    if (obj instanceof SolrInfoMBean){
      infoMBeans.add((SolrInfoMBean) obj);
    }
    return obj;
  }
  public Object newAdminHandlerInstance(final CoreContainer coreContainer, String cname, String ... subpackages) {
    Class clazz = findClass(cname,subpackages);
    if( clazz == null ) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
          "Can not find class: "+cname + " in " + classLoader, false);
    }
    Object obj = null;
    try {
      Constructor ctor = clazz.getConstructor(CoreContainer.class);
       obj = ctor.newInstance(coreContainer);
    } 
    catch (Exception e) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
          "Error instantiating class: '" + clazz.getName()+"'", e, false );
    }
    if( obj instanceof ResourceLoaderAware ) {
      assertAwareCompatibility( ResourceLoaderAware.class, obj );
      waitingForResources.add( (ResourceLoaderAware)obj );
    }
    return obj;
  }
  public Object newInstance(String cName, String [] subPackages, Class[] params, Object[] args){
    Class clazz = findClass(cName,subPackages);
    if( clazz == null ) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
          "Can not find class: "+cName + " in " + classLoader, false);
    }
    Object obj = null;
    try {
      Constructor constructor = clazz.getConstructor(params);
      obj = constructor.newInstance(args);
    }
    catch (Exception e) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
          "Error instantiating class: '" + clazz.getName()+"'", e, false );
    }
    if( obj instanceof SolrCoreAware ) {
      assertAwareCompatibility( SolrCoreAware.class, obj );
      waitingForCore.add( (SolrCoreAware)obj );
    }
    if( obj instanceof ResourceLoaderAware ) {
      assertAwareCompatibility( ResourceLoaderAware.class, obj );
      waitingForResources.add( (ResourceLoaderAware)obj );
    }
    if (obj instanceof SolrInfoMBean){
      infoMBeans.add((SolrInfoMBean) obj);
    }
    return obj;
  }
  public void inform(SolrCore core) 
  {
    this.dataDir = core.getDataDir();
    for( SolrCoreAware aware : waitingForCore ) {
      aware.inform( core );
    }
    waitingForCore.clear();
  }
  public void inform( ResourceLoader loader ) 
  {
    for( ResourceLoaderAware aware : waitingForResources ) {
      aware.inform( loader );
    }
    waitingForResources.clear();
  }
  public void inform(Map<String, SolrInfoMBean> infoRegistry) {
    for (SolrInfoMBean bean : infoMBeans) {
      infoRegistry.put(bean.getName(), bean);
    }
  }
  public static String locateSolrHome() {
    String home = null;
    try {
      Context c = new InitialContext();
      home = (String)c.lookup("java:comp/env/"+project+"/home");
      log.info("Using JNDI solr.home: "+home );
    } catch (NoInitialContextException e) {
      log.info("JNDI not configured for "+project+" (NoInitialContextEx)");
    } catch (NamingException e) {
      log.info("No /"+project+"/home in JNDI");
    } catch( RuntimeException ex ) {
      log.warn("Odd RuntimeException while testing for JNDI: " + ex.getMessage());
    } 
    if( home == null ) {
      String prop = project + ".solr.home";
      home = System.getProperty(prop);
      if( home != null ) {
        log.info("using system property "+prop+": " + home );
      }
    }
    if( home == null ) {
      home = project + '/';
      log.info(project + " home defaulted to '" + home + "' (could not find system property or JNDI)");
    }
    return normalizeDir( home );
  }
  @Deprecated
  public static String locateInstanceDir() {
    return locateSolrHome();
  }
  public String getInstanceDir() {
    return instanceDir;
  }
  private static final Map<Class, Class[]> awareCompatibility;
  static {
    awareCompatibility = new HashMap<Class, Class[]>();
    awareCompatibility.put( 
      SolrCoreAware.class, new Class[] {
        SolrRequestHandler.class,
        QueryResponseWriter.class,
        SearchComponent.class,
        UpdateRequestProcessorFactory.class
      }
    );
    awareCompatibility.put(
      ResourceLoaderAware.class, new Class[] {
        CharFilterFactory.class,
        TokenFilterFactory.class,
        TokenizerFactory.class,
        FieldType.class
      }
    );
  }
  void assertAwareCompatibility( Class aware, Object obj )
  {
    Class[] valid = awareCompatibility.get( aware );
    if( valid == null ) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
          "Unknown Aware interface: "+aware );
    }
    for( Class v : valid ) {
      if( v.isInstance( obj ) ) {
        return;
      }
    }
    StringBuilder builder = new StringBuilder();
    builder.append( "Invalid 'Aware' object: " ).append( obj );
    builder.append( " -- ").append( aware.getName() );
    builder.append(  " must be an instance of: " );
    for( Class v : valid ) {
      builder.append( "[" ).append( v.getName() ).append( "] ") ;
    }
    throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, builder.toString() );
  }
}
