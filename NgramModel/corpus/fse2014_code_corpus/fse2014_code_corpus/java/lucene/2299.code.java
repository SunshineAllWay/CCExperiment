package org.apache.solr.core;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.DOMUtil;
import javax.xml.parsers.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.util.Version;
public class Config {
  public static final Logger log = LoggerFactory.getLogger(Config.class);
  static final XPathFactory xpathFactory = XPathFactory.newInstance();
  private final Document doc;
  private final String prefix;
  private final String name;
  private final SolrResourceLoader loader;
  @Deprecated
  public Config(String name, InputStream is, String prefix) throws ParserConfigurationException, IOException, SAXException 
  {
    this( null, name, is, prefix );
  }
  public Config(SolrResourceLoader loader, String name) throws ParserConfigurationException, IOException, SAXException 
  {
    this( loader, name, null, null );
  }
  public Config(SolrResourceLoader loader, String name, InputStream is, String prefix) throws ParserConfigurationException, IOException, SAXException 
  {
    if( loader == null ) {
      loader = new SolrResourceLoader( null );
    }
    this.loader = loader;
    this.name = name;
    this.prefix = (prefix != null && !prefix.endsWith("/"))? prefix + '/' : prefix;
    InputStream lis = is;
    try {
      if (lis == null) {
        lis = loader.openConfig(name);
      }
      javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      try {
        dbf.setXIncludeAware(true);
        dbf.setNamespaceAware(true);
      } catch(UnsupportedOperationException e) {
        log.warn(name + " XML parser doesn't support XInclude option");
      }
      doc = dbf.newDocumentBuilder().parse(lis);
        DOMUtil.substituteProperties(doc, loader.getCoreProperties());
    } catch (ParserConfigurationException e)  {
      SolrException.log(log, "Exception during parsing file: " + name, e);
      throw e;
    } catch (SAXException e)  {
      SolrException.log(log, "Exception during parsing file: " + name, e);
      throw e;
    } catch( SolrException e ){
    	SolrException.log(log,"Error in "+name,e);
    	throw e;
    } finally {
      if (lis != is)  lis.close();
    }
  }
  public SolrResourceLoader getResourceLoader()
  {
    return loader;
  }
  public String getResourceName() {
    return name;
  }
  public String getName() {
    return name;
  }
  public Document getDocument() {
    return doc;
  }
  public XPath getXPath() {
    return xpathFactory.newXPath();
  }
  private String normalize(String path) {
    return (prefix==null || path.startsWith("/")) ? path : prefix+path;
  }
  public Object evaluate(String path, QName type) {
    XPath xpath = xpathFactory.newXPath();
    try {
      String xstr=normalize(path);
      Object o = xpath.evaluate(xstr, doc, type);
      return o;
    } catch (XPathExpressionException e) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Error in xpath:" + path +" for " + name,e,false);
    }
  }
  public Node getNode(String path, boolean errIfMissing) {
   XPath xpath = xpathFactory.newXPath();
   Node nd = null;
   String xstr = normalize(path);
    try {
      nd = (Node)xpath.evaluate(xstr, doc, XPathConstants.NODE);
      if (nd==null) {
        if (errIfMissing) {
          throw new RuntimeException(name + " missing "+path);
        } else {
          log.debug(name + " missing optional " + path);
          return null;
        }
      }
      log.trace(name + ":" + path + "=" + nd);
      return nd;
    } catch (XPathExpressionException e) {
      SolrException.log(log,"Error in xpath",e);
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Error in xpath:" + xstr + " for " + name,e,false);
    } catch (SolrException e) {
      throw(e);
    } catch (Throwable e) {
      SolrException.log(log,"Error in xpath",e);
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Error in xpath:" + xstr+ " for " + name,e,false);
    }
  }
  public String getVal(String path, boolean errIfMissing) {
    Node nd = getNode(path,errIfMissing);
    if (nd==null) return null;
    String txt = DOMUtil.getText(nd);
    log.debug(name + ' '+path+'='+txt);
    return txt;
  }
  public String get(String path) {
    return getVal(path,true);
  }
  public String get(String path, String def) {
    String val = getVal(path, false);
    return val!=null ? val : def;
  }
  public int getInt(String path) {
    return Integer.parseInt(getVal(path, true));
  }
  public int getInt(String path, int def) {
    String val = getVal(path, false);
    return val!=null ? Integer.parseInt(val) : def;
  }
  public boolean getBool(String path) {
    return Boolean.parseBoolean(getVal(path, true));
  }
  public boolean getBool(String path, boolean def) {
    String val = getVal(path, false);
    return val!=null ? Boolean.parseBoolean(val) : def;
  }
  public float getFloat(String path) {
    return Float.parseFloat(getVal(path, true));
  }
  public float getFloat(String path, float def) {
    String val = getVal(path, false);
    return val!=null ? Float.parseFloat(val) : def;
  }
  public double getDouble(String path){
     return Double.parseDouble(getVal(path, true));
   }
   public double getDouble(String path, double def) {
     String val = getVal(path, false);
     return val!=null ? Double.parseDouble(val) : def;
   }
   public Version getLuceneVersion(String path) {
     return parseLuceneVersionString(getVal(path, true));
   }
   public Version getLuceneVersion(String path, Version def) {
     String val = getVal(path, false);
     return val!=null ? parseLuceneVersionString(val) : def;
   }
  private static final AtomicBoolean versionWarningAlreadyLogged = new AtomicBoolean(false);
  public static final Version parseLuceneVersionString(final String matchVersion) {
    String parsedMatchVersion = matchVersion.toUpperCase();
    parsedMatchVersion = parsedMatchVersion.replaceFirst("^(\\d)\\.(\\d)$", "LUCENE_$1$2");
    final Version version;
    try {
      version = Version.valueOf(parsedMatchVersion);
    } catch (IllegalArgumentException iae) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
        "Invalid luceneMatchVersion '" + matchVersion +
        "', valid values are: " + Arrays.toString(Version.values()) +
        " or a string in format 'V.V'", iae, false);    
    }
    if (version == Version.LUCENE_CURRENT && !versionWarningAlreadyLogged.getAndSet(true)) {
      log.warn(
        "You should not use LUCENE_CURRENT as luceneMatchVersion property: "+
        "if you use this setting, and then Solr upgrades to a newer release of Lucene, "+
        "sizable changes may happen. If precise back compatibility is important "+
        "then you should instead explicitly specify an actual Lucene version."
      );
    }
    return version;
  }
  @Deprecated
  public String getConfigDir() {
    return loader.getConfigDir();
  }
  @Deprecated
  public InputStream openResource(String resource) {
    return loader.openResource(resource);
  }
  @Deprecated
  public List<String> getLines(String resource) throws IOException {
    return loader.getLines(resource);
  }
  @Deprecated
  public Class findClass(String cname, String... subpackages) {
    return loader.findClass(cname, subpackages);
  }
  @Deprecated
  public Object newInstance(String cname, String ... subpackages) {
    return loader.newInstance(cname, subpackages);
  }
  @Deprecated
  public String getInstanceDir() {
    return loader.getInstanceDir();
  }
}
