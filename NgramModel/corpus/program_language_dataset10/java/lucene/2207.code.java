package org.apache.solr.analysis;
import org.apache.solr.core.Config;
import org.apache.solr.common.SolrException;
import org.apache.solr.schema.IndexSchema;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.util.Version;
abstract class BaseTokenStreamFactory {
  protected Map<String,String> args;
  protected Version luceneMatchVersion = null;
  public void init(Map<String,String> args) {
    this.args=args;
    String matchVersion = args.get(IndexSchema.LUCENE_MATCH_VERSION_PARAM);
    if (matchVersion != null) {
      luceneMatchVersion = Config.parseLuceneVersionString(matchVersion);
    }
  }
  public Map<String,String> getArgs() {
    return args;
  }
  protected final void assureMatchVersion() {
    if (luceneMatchVersion == null) {
      throw new RuntimeException("Configuration Error: Factory '" + this.getClass().getName() +
        "' needs a 'luceneMatchVersion' parameter");
    }
  }
  protected int getInt(String name) {
    return getInt(name,-1,false);
  }
  protected int getInt(String name, int defaultVal) {
    return getInt(name,defaultVal,true);
  }
  protected int getInt(String name, int defaultVal, boolean useDefault) {
    String s = args.get(name);
    if (s==null) {
      if (useDefault) return defaultVal;
      throw new RuntimeException("Configuration Error: missing parameter '" + name + "'");
    }
    return Integer.parseInt(s);
  }
  protected boolean getBoolean(String name, boolean defaultVal) {
    return getBoolean(name,defaultVal,true);
  }
  protected boolean getBoolean(String name, boolean defaultVal, boolean useDefault) {
    String s = args.get(name);
    if (s==null) {
      if (useDefault) return defaultVal;
      throw new RuntimeException("Configuration Error: missing parameter '" + name + "'");
    }
    return Boolean.parseBoolean(s);
  }
}
