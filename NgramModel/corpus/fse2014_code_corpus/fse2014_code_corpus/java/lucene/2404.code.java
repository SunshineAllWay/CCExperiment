package org.apache.solr.request;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.core.SolrCore;
import java.util.Map;
import java.util.HashMap;
public abstract class SolrQueryRequestBase implements SolrQueryRequest {
  @Deprecated
  public static final String QUERY_NAME="q";
  @Deprecated
  public static final String START_NAME="start";
  @Deprecated
  public static final String ROWS_NAME="rows";
  @Deprecated
  public static final String XSL_NAME="xsl";
  @Deprecated
  public static final String QUERYTYPE_NAME="qt";
  protected final SolrCore core;
  protected final SolrParams origParams;
  protected SolrParams params;
  protected Map<Object,Object> context;
  protected Iterable<ContentStream> streams;
  public SolrQueryRequestBase(SolrCore core, SolrParams params) {
    this.core = core;
    this.params = this.origParams = params;
  }
  public Map<Object,Object> getContext() {
    if (context==null) context = new HashMap<Object,Object>();
    return context;
  }
  public SolrParams getParams() {
    return params;
  }
  public SolrParams getOriginalParams() {
    return origParams;
  }
  public void setParams(SolrParams params) {
    this.params = params;
  }
  @Deprecated
  public String getParam(String name) {
    return params.get(name);
  }
  @Deprecated
  public String[] getParams(String name) {
    return params.getParams(name);
  }
  @Deprecated
  public int getIntParam(String name) {
    String s = getParam(name);
    if (s==null) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Missing required parameter '"+name+"' from " + this);
    }
    return Integer.parseInt(s);
  }
  @Deprecated
  public int getIntParam(String name, int defval) {
    String s = getParam(name);
    return s==null ? defval : Integer.parseInt(s);
  }
  @Deprecated
  public String getStrParam(String name) {
    String s = getParam(name);
    if (s==null) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Missing required parameter '"+name+"' from " + this);
    }
    return s;
  }
  @Deprecated
  public String getStrParam(String name, String defval) {
    String s = getParam(name);
    return s==null ? defval : s;
  }
  @Deprecated
  public String getQueryString() {
    return params.get(CommonParams.Q);
  }
  @Deprecated
  public String getQueryType() {
    return params.get(CommonParams.QT);
  }
  @Deprecated
  public int getStart() {
    return params.getInt(CommonParams.START, 0);
  }
  @Deprecated
  public int getLimit() {
    return params.getInt(CommonParams.ROWS, 10);
  }
  protected final long startTime=System.currentTimeMillis();
  public long getStartTime() {
    return startTime;
  }
  protected RefCounted<SolrIndexSearcher> searcherHolder;
  public SolrIndexSearcher getSearcher() {
    if(core == null) return null;
    if (searcherHolder==null) {
      searcherHolder = core.getSearcher();
    }
    return searcherHolder.get();
  }
  public SolrCore getCore() {
    return core;
  }
  public IndexSchema getSchema() {
    return core == null? null: core.getSchema();
  }
  public void close() {
    if (searcherHolder!=null) {
      searcherHolder.decref();
      searcherHolder = null;
    }
  }
  public Iterable<ContentStream> getContentStreams() {
    return streams; 
  }
  public void setContentStreams( Iterable<ContentStream> s ) {
    streams = s; 
  }
  public String getParamString() {
    return origParams.toString();
  }
  public String toString() {
    return this.getClass().getSimpleName() + '{' + params + '}';
  }
}
