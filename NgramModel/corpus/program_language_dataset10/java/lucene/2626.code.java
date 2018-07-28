package org.apache.solr.util.plugin;
import org.apache.solr.core.SolrCore;
public interface SolrCoreAware 
{
  void inform( SolrCore core );
}
