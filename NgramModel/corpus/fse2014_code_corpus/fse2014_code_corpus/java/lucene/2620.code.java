package org.apache.solr.util.plugin;
import java.util.Map;
public interface MapInitializedPlugin {
  void init( Map<String,String> args );
}
