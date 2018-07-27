package org.apache.solr.util.plugin;
import org.apache.solr.core.PluginInfo;
public interface PluginInfoInitialized {
  public void init(PluginInfo info);
}
