package org.apache.solr.search;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Map;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.DOMUtil;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrResourceLoader;
import javax.xml.xpath.XPathConstants;
public class CacheConfig {
  private String nodeName;
  private Class clazz;
  private Map<String,String> args;
  private CacheRegenerator regenerator;
  private String cacheImpl;
  private Object[] persistence = new Object[1];
  private String regenImpl;
  public CacheConfig() {}
  public CacheConfig(Class clazz, Map<String,String> args, CacheRegenerator regenerator) {
    this.clazz = clazz;
    this.args = args;
    this.regenerator = regenerator;
  }
  public CacheRegenerator getRegenerator() {
    return regenerator;
  }
  public void setRegenerator(CacheRegenerator regenerator) {
    this.regenerator = regenerator;
  }
  public static CacheConfig[] getMultipleConfigs(SolrConfig solrConfig, String configPath) {
    NodeList nodes = (NodeList)solrConfig.evaluate(configPath, XPathConstants.NODESET);
    if (nodes==null || nodes.getLength()==0) return null;
    CacheConfig[] configs = new CacheConfig[nodes.getLength()];
    for (int i=0; i<nodes.getLength(); i++) {
      configs[i] = getConfig(solrConfig, nodes.item(i));
    }
    return configs;
  }
  public static CacheConfig getConfig(SolrConfig solrConfig, String xpath) {
    Node node = solrConfig.getNode(xpath, false);
    return getConfig(solrConfig, node);
  }
  public static CacheConfig getConfig(SolrConfig solrConfig, Node node) {
    if (node==null) return null;
    CacheConfig config = new CacheConfig();
    config.nodeName = node.getNodeName();
    config.args = DOMUtil.toMap(node.getAttributes());
    String nameAttr = config.args.get("name");  
    if (nameAttr==null) {
      config.args.put("name",config.nodeName);
    }
    SolrResourceLoader loader = solrConfig.getResourceLoader();
    config.cacheImpl = config.args.get("class");
    config.regenImpl = config.args.get("regenerator");
    config.clazz = loader.findClass(config.cacheImpl);
    if (config.regenImpl != null) {
      config.regenerator = (CacheRegenerator) loader.newInstance(config.regenImpl);
    }
    return config;
  }
  public SolrCache newInstance() {
    try {
      SolrCache cache = (SolrCache)clazz.newInstance();
      persistence[0] = cache.init(args, persistence[0], regenerator);
      return cache;
    } catch (Exception e) {
      SolrException.log(SolrCache.log,"Error instantiating cache",e);
      return null;
    }
  }
}
