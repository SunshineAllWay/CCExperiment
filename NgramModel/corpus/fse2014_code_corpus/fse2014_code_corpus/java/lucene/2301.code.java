package org.apache.solr.core;
import java.util.Properties;
import java.io.File;
public class CoreDescriptor {
  protected String name;
  protected String instanceDir;
  protected String dataDir;
  protected String configName;
  protected String propertiesName;
  protected String schemaName;
  private final CoreContainer coreContainer;
  private Properties coreProperties;
  public CoreDescriptor(CoreContainer coreContainer, String name, String instanceDir) {
    this.coreContainer = coreContainer;
    this.name = name;
    if (name == null) {
      throw new RuntimeException("Core needs a name");
    }
    if (instanceDir == null) {
      throw new NullPointerException("Missing required \'instanceDir\'");
    }
    instanceDir = SolrResourceLoader.normalizeDir(instanceDir);
    this.instanceDir = instanceDir;
    this.configName = getDefaultConfigName();
    this.schemaName = getDefaultSchemaName();
  }
  public CoreDescriptor(CoreDescriptor descr) {
    this.instanceDir = descr.instanceDir;
    this.configName = descr.configName;
    this.schemaName = descr.schemaName;
    this.name = descr.name;
    this.dataDir = descr.dataDir;
    coreContainer = descr.coreContainer;
  }
  private Properties initImplicitProperties() {
    Properties implicitProperties = new Properties(coreContainer.getContainerProperties());
    implicitProperties.setProperty("solr.core.name", name);
    implicitProperties.setProperty("solr.core.instanceDir", instanceDir);
    implicitProperties.setProperty("solr.core.dataDir", getDataDir());
    implicitProperties.setProperty("solr.core.configName", configName);
    implicitProperties.setProperty("solr.core.schemaName", schemaName);
    return implicitProperties;
  }
  public String getDefaultConfigName() {
    return "solrconfig.xml";
  }
  public String getDefaultSchemaName() {
    return "schema.xml";
  }
  public String getDefaultDataDir() {
    return "data" + File.separator;
  }
  public String getPropertiesName() {
    return propertiesName;
  }
  public void setPropertiesName(String propertiesName) {
    this.propertiesName = propertiesName;
  }
  public String getDataDir() {
    String dataDir = this.dataDir;
    if (dataDir == null) dataDir = getDefaultDataDir();
    if (new File(dataDir).isAbsolute()) {
      return dataDir;
    } else {
      if (new File(instanceDir).isAbsolute()) {
        return SolrResourceLoader.normalizeDir(SolrResourceLoader.normalizeDir(instanceDir) + dataDir);
      } else  {
        return SolrResourceLoader.normalizeDir(coreContainer.getSolrHome() +
                SolrResourceLoader.normalizeDir(instanceDir) + dataDir);
      }
    }
  }
  public void setDataDir(String s) {
    dataDir = s;
    if (dataDir != null && dataDir.length()==0) dataDir=null;
  }
  public String getInstanceDir() {
    return instanceDir;
  }
  public void setConfigName(String name) {
    if (name == null || name.length() == 0)
      throw new IllegalArgumentException("name can not be null or empty");
    this.configName = name;
  }
  public String getConfigName() {
    return this.configName;
  }
  public void setSchemaName(String name) {
    if (name == null || name.length() == 0)
      throw new IllegalArgumentException("name can not be null or empty");
    this.schemaName = name;
  }
  public String getSchemaName() {
    return this.schemaName;
  }
  public String getName() {
    return this.name;
  }
  public CoreContainer getCoreContainer() {
    return coreContainer;
  }
  Properties getCoreProperties() {
    return coreProperties;
  }
  public void setCoreProperties(Properties coreProperties) {
    if (this.coreProperties == null) {
      Properties p = initImplicitProperties();
      this.coreProperties = new Properties(p);
      if(coreProperties != null)
        this.coreProperties.putAll(coreProperties);
    }
  }
}
