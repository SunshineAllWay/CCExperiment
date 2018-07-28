package org.apache.solr.util.plugin;
import org.apache.solr.common.util.NamedList;
public interface NamedListInitializedPlugin {
  void init( NamedList args );
}
