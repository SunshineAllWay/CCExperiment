package org.apache.solr.handler.component;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.RTimer;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SortSpec;
import org.apache.solr.search.SolrIndexSearcher;
import java.util.List;
import java.util.Map;
public class ResponseBuilder
{
  public SolrQueryRequest req;
  public SolrQueryResponse rsp;
  public boolean doHighlights;
  public boolean doFacets;
  public boolean doStats;
  public boolean doTerms;
  private boolean needDocList = false;
  private boolean needDocSet = false;
  private int fieldFlags = 0;
  private boolean debug = false;
  private QParser qparser = null;
  private String queryString = null;
  private Query query = null;
  private List<Query> filters = null;
  private SortSpec sortSpec = null;
  private DocListAndSet results = null;
  private NamedList<Object> debugInfo = null;
  private RTimer timer = null;
  private Query highlightQuery = null;
  public List<SearchComponent> components;
  public static final String FIELD_SORT_VALUES = "fsv";
  public static final String SHARDS = "shards";
  public static final String IDS = "ids";
  public static int STAGE_START           = 0;
  public static int STAGE_PARSE_QUERY     = 1000;
  public static int STAGE_EXECUTE_QUERY   = 2000;
  public static int STAGE_GET_FIELDS      = 3000;
  public static int STAGE_DONE            = Integer.MAX_VALUE;
  public int stage;  
  public String[] shards;
  public int shards_rows = -1;
  public int shards_start = -1;
  public List<ShardRequest> outgoing;  
  public List<ShardRequest> finished;  
  public int getShardNum(String shard) {
    for (int i=0; i<shards.length; i++) {
      if (shards[i]==shard || shards[i].equals(shard)) return i;
    }
    return -1;
  }
  public void addRequest(SearchComponent me, ShardRequest sreq) {
    outgoing.add(sreq);
    if ((sreq.purpose & ShardRequest.PURPOSE_PRIVATE)==0) {
      for (SearchComponent component : components) {
        if (component != me) {
          component.modifyRequest(this, me, sreq);
        }
      }
    }
  }
  public GlobalCollectionStat globalCollectionStat;
  Map<Object, ShardDoc> resultIds;
  public FacetComponent.FacetInfo _facetInfo;
  SolrDocumentList _responseDocs;
  StatsInfo _statsInfo;
  TermsComponent.TermsHelper _termsHelper;
  public void addDebugInfo( String name, Object val )
  {
    if( debugInfo == null ) {
      debugInfo = new SimpleOrderedMap<Object>();
    }
    debugInfo.add( name, val );
  }
  public boolean isDebug() {
    return debug;
  }
  public void setDebug(boolean debug) {
    this.debug = debug;
  }
  public NamedList<Object> getDebugInfo() {
    return debugInfo;
  }
  public void setDebugInfo(NamedList<Object> debugInfo) {
    this.debugInfo = debugInfo;
  }
  public int getFieldFlags() {
    return fieldFlags;
  }
  public void setFieldFlags(int fieldFlags) {
    this.fieldFlags = fieldFlags;
  }
  public List<Query> getFilters() {
    return filters;
  }
  public void setFilters(List<Query> filters) {
    this.filters = filters;
  }
  public Query getHighlightQuery() {
    return highlightQuery;
  }
  public void setHighlightQuery(Query highlightQuery) {
    this.highlightQuery = highlightQuery;
  }
  public boolean isNeedDocList() {
    return needDocList;
  }
  public void setNeedDocList(boolean needDocList) {
    this.needDocList = needDocList;
  }
  public boolean isNeedDocSet() {
    return needDocSet;
  }
  public void setNeedDocSet(boolean needDocSet) {
    this.needDocSet = needDocSet;
  }
  public QParser getQparser() {
    return qparser;
  }
  public void setQparser(QParser qparser) {
    this.qparser = qparser;
  }
  public String getQueryString() {
    return queryString;
  }
  public void setQueryString(String qstr) {
    this.queryString = qstr;
  }
  public Query getQuery() {
    return query;
  }
  public void setQuery(Query query) {
    this.query = query;
  }
  public DocListAndSet getResults() {
    return results;
  }
  public void setResults(DocListAndSet results) {
    this.results = results;
  }
  public SortSpec getSortSpec() {
    return sortSpec;
  }
  public void setSortSpec(SortSpec sort) {
    this.sortSpec = sort;
  }
  public RTimer getTimer() {
    return timer;
  }
  public void setTimer(RTimer timer) {
    this.timer = timer;
  }
  public static class GlobalCollectionStat {
    public final long numDocs;
    public final Map<String, Long> dfMap;
    public GlobalCollectionStat(int numDocs, Map<String, Long> dfMap) {
      this.numDocs = numDocs;
      this.dfMap = dfMap;
    }
  }
  public SolrIndexSearcher.QueryCommand getQueryCommand() {
    SolrIndexSearcher.QueryCommand cmd = new SolrIndexSearcher.QueryCommand();
    cmd.setQuery( getQuery() )
      .setFilterList( getFilters() )
      .setSort( getSortSpec().getSort() )
      .setOffset( getSortSpec().getOffset() )
      .setLen( getSortSpec().getCount() )
      .setFlags( getFieldFlags() )
      .setNeedDocSet( isNeedDocSet() );
    return cmd;
  }
  public void setResult( SolrIndexSearcher.QueryResult result ) {
    setResults( result.getDocListAndSet() );
    if( result.isPartialResults() ) {
      rsp.getResponseHeader().add( "partialResults", Boolean.TRUE );
    }
  }
}
