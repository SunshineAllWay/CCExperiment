package org.apache.solr.analysis;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public abstract class BaseCharFilterFactory implements CharFilterFactory {
  public static final Logger log = LoggerFactory.getLogger(BaseCharFilterFactory.class);
  protected Map<String,String> args;
  public Map<String, String> getArgs() {
    return args;
  }
  public void init(Map<String, String> args) {
    this.args = args;
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
}
