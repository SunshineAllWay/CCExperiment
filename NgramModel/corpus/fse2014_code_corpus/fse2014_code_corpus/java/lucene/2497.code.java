package org.apache.solr.search;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.request.SolrQueryRequest;
import java.util.*;
public abstract class QParser {
  protected String qstr;
  protected SolrParams params;
  protected SolrParams localParams;
  protected SolrQueryRequest req;
  protected int recurseCount;
  protected Query query;
  public QParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    this.qstr = qstr;
    this.localParams = localParams;
    if (localParams != null) {
      String tagStr = localParams.get(CommonParams.TAG);
      if (tagStr != null) {
        Map context = req.getContext();
        Map<String,Collection<Object>> tagMap = (Map<String, Collection<Object>>)req.getContext().get("tags");
        if (tagMap == null) {
          tagMap = new HashMap<String,Collection<Object>>();
          context.put("tags", tagMap);          
        }
        if (tagStr.indexOf(',') >= 0) {
          List<String> tags = StrUtils.splitSmart(tagStr, ',');
          for (String tag : tags) {
            addTag(tagMap, tag, this);
          }
        } else {
          addTag(tagMap, tagStr, this);
        }
      }
    }
    this.params = params;
    this.req = req;
  }
  private static void addTag(Map tagMap, Object key, Object val) {
    Collection lst = (Collection)tagMap.get(key);
    if (lst == null) {
      lst = new ArrayList(2);
      tagMap.put(key, lst);
    }
    lst.add(val);
  }
  public abstract Query parse() throws ParseException;
  public SolrParams getLocalParams() {
    return localParams;
  }
  public void setLocalParams(SolrParams localParams) {
    this.localParams = localParams;
  }
  public SolrParams getParams() {
    return params;
  }
  public void setParams(SolrParams params) {
    this.params = params;
  }
  public SolrQueryRequest getReq() {
    return req;
  }
  public void setReq(SolrQueryRequest req) {
    this.req = req;
  }
  public String getString() {
    return qstr;
  }
  public void setString(String s) {
    this.qstr = s;
  }
  public Query getQuery() throws ParseException {
    if (query==null) {
      query=parse();
    }
    return query;
  }
  private void checkRecurse() throws ParseException {
    if (recurseCount++ >= 100) {
      throw new ParseException("Infinite Recursion detected parsing query '" + qstr + "'");
    }
  }
  protected String getParam(String name) {
    String val;
    if (localParams != null) {
      val = localParams.get(name);
      if (val != null) return val;
    }
    return params.get(name);
  }
  public QParser subQuery(String q, String defaultType) throws ParseException {
    checkRecurse();
    if (defaultType == null && localParams != null) {
      defaultType = localParams.get(QueryParsing.DEFTYPE);
    }
    QParser nestedParser = getParser(q, defaultType, getReq());
    nestedParser.recurseCount = recurseCount;
    recurseCount--;
    return nestedParser;
  }
  public SortSpec getSort(boolean useGlobalParams) throws ParseException {
    getQuery(); 
    String sortStr = null;
    String startS = null;
    String rowsS = null;
    if (localParams != null) {
      sortStr = localParams.get(CommonParams.SORT);
      startS = localParams.get(CommonParams.START);
      rowsS = localParams.get(CommonParams.ROWS);
      if (sortStr != null || startS != null || rowsS != null) {
        useGlobalParams = false;
      }
    }
    if (useGlobalParams) {
      if (sortStr ==null) {
          sortStr = params.get(CommonParams.SORT);
      }
      if (startS==null) {
        startS = params.get(CommonParams.START);
      }
      if (rowsS==null) {
        rowsS = params.get(CommonParams.ROWS);
      }
    }
    int start = startS != null ? Integer.parseInt(startS) : 0;
    int rows = rowsS != null ? Integer.parseInt(rowsS) : 10;
    Sort sort = null;
    if( sortStr != null ) {
      sort = QueryParsing.parseSort(sortStr, req.getSchema());
    }
    return new SortSpec( sort, start, rows );
  }
  public String[] getDefaultHighlightFields() {
    return new String[]{};
  }
  public Query getHighlightQuery() throws ParseException {
    return getQuery();
  }
  public void addDebugInfo(NamedList<Object> debugInfo) {
    debugInfo.add("QParser", this.getClass().getSimpleName());
  }
  public static QParser getParser(String qstr, String defaultType, SolrQueryRequest req) throws ParseException {
    SolrParams localParams = QueryParsing.getLocalParams(qstr, req.getParams());
    String type;
    if (localParams == null) {
      type = defaultType;
    } else {
      String localType = localParams.get(QueryParsing.TYPE);
      type = localType == null ? defaultType : localType;
      qstr = localParams.get("v");
    }
    type = type==null ? QParserPlugin.DEFAULT_QTYPE : type;
    QParserPlugin qplug = req.getCore().getQueryPlugin(type);
    return qplug.createParser(qstr, localParams, req.getParams(), req);
  }                            
}
