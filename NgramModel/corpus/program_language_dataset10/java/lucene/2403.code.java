package org.apache.solr.request;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.core.SolrCore;
import java.util.Map;
public interface SolrQueryRequest {
  public SolrParams getParams();
  public void setParams(SolrParams params);
  public Iterable<ContentStream> getContentStreams();
  public SolrParams getOriginalParams();
  public Map<Object,Object> getContext();
  public void close();
  @Deprecated
  public String getParam(String name);
  @Deprecated
  public String[] getParams(String name);
  @Deprecated
  public String getQueryString();
  @Deprecated
  public String getQueryType();
  @Deprecated
  public int getStart();
  @Deprecated
  public int getLimit();
  public long getStartTime();
  public SolrIndexSearcher getSearcher();
  public SolrCore getCore();
  public IndexSchema getSchema();
  public String getParamString();
}
