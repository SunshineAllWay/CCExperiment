package org.apache.solr.handler.component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.search.FieldCache;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.StatsParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.StatsValues;
import org.apache.solr.handler.component.FieldFacetStats;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.request.UnInvertedField;
import org.apache.solr.core.SolrCore;
public class StatsComponent extends SearchComponent {
  public static final String COMPONENT_NAME = "stats";
  @Override
  public void prepare(ResponseBuilder rb) throws IOException {
    if (rb.req.getParams().getBool(StatsParams.STATS,false)) {
      rb.setNeedDocSet( true );
      rb.doStats = true;
    }
  }
  @Override
  public void process(ResponseBuilder rb) throws IOException {
    if (rb.doStats) {
      SolrParams params = rb.req.getParams();
      SimpleStats s = new SimpleStats(rb.req,
              rb.getResults().docSet,
              params );
      rb.rsp.add( "stats", s.getStatsCounts() );
    }
  }
  @Override
  public int distributedProcess(ResponseBuilder rb) throws IOException {
    return ResponseBuilder.STAGE_DONE;
  }
  @Override
  public void modifyRequest(ResponseBuilder rb, SearchComponent who, ShardRequest sreq) {
    if (!rb.doStats) return;
    if ((sreq.purpose & ShardRequest.PURPOSE_GET_TOP_IDS) != 0) {
        sreq.purpose |= ShardRequest.PURPOSE_GET_STATS;
        StatsInfo si = rb._statsInfo;
        if (si == null) {
          rb._statsInfo = si = new StatsInfo();
          si.parse(rb.req.getParams(), rb);
        }
    } else {
      sreq.params.set(StatsParams.STATS, "false");
    }
  }
  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
    if (!rb.doStats || (sreq.purpose & ShardRequest.PURPOSE_GET_STATS) == 0) return;
    StatsInfo si = rb._statsInfo;
    for (ShardResponse srsp : sreq.responses) {
      NamedList stats = (NamedList) srsp.getSolrResponse().getResponse().get("stats");
      NamedList stats_fields = (NamedList) stats.get("stats_fields");
      if (stats_fields != null) {
        for (int i = 0; i < stats_fields.size(); i++) {
          String field = stats_fields.getName(i);
          StatsValues stv = si.statsFields.get(field);
          NamedList shardStv = (NamedList) stats_fields.get(field);
          stv.accumulate(shardStv);
        }
      }
    }
  }
  @Override
  public void finishStage(ResponseBuilder rb) {
    if (!rb.doStats || rb.stage != ResponseBuilder.STAGE_GET_FIELDS) return;
    StatsInfo si = rb._statsInfo;
    NamedList stats = new SimpleOrderedMap();
    NamedList stats_fields = new SimpleOrderedMap();
    stats.add("stats_fields", stats_fields);
    for (String field : si.statsFields.keySet()) {
      NamedList stv = si.statsFields.get(field).getStatsValues();
      if ((Long) stv.get("count") != 0) {
        stats_fields.add(field, stv);
      } else {
        stats_fields.add(field, null);
      }
    }
    rb.rsp.add("stats", stats);
    rb._statsInfo = null;
  }
  @Override
  public String getDescription() {
    return "Calculate Statistics";
  }
  @Override
  public String getVersion() {
    return "$Revision$";
  }
  @Override
  public String getSourceId() {
    return "$Id$";
  }
  @Override
  public String getSource() {
    return "$URL$";
  }
}
class StatsInfo {
  Map<String, StatsValues> statsFields;
  void parse(SolrParams params, ResponseBuilder rb) {
    statsFields = new HashMap<String, StatsValues>();
    String[] statsFs = params.getParams(StatsParams.STATS_FIELD);
    if (statsFs != null) {
      for (String field : statsFs) {
        statsFields.put(field,new StatsValues());
      }
    }
  }
}
class SimpleStats {
  protected DocSet docs;
  protected SolrParams params;
  protected SolrIndexSearcher searcher;
  protected SolrQueryRequest req;
  public SimpleStats(SolrQueryRequest req,
                      DocSet docs,
                      SolrParams params) {
    this.req = req;
    this.searcher = req.getSearcher();
    this.docs = docs;
    this.params = params;
  }
  public NamedList<Object> getStatsCounts() throws IOException {
    NamedList<Object> res = new SimpleOrderedMap<Object>();
    res.add("stats_fields", getStatsFields());
    return res;
  }
  public NamedList getStatsFields() throws IOException {
    NamedList<NamedList<Number>> res = new SimpleOrderedMap<NamedList<Number>>();
    String[] statsFs = params.getParams(StatsParams.STATS_FIELD);
    boolean isShard = params.getBool(ShardParams.IS_SHARD, false);
    if (null != statsFs) {
      for (String f : statsFs) {
        String[] facets = params.getFieldParams(f, StatsParams.STATS_FACET);
        if (facets == null) {
          facets = new String[0]; 
        }
        SchemaField sf = searcher.getSchema().getField(f);
        FieldType ft = sf.getType();
        NamedList stv;
        String prefix = TrieField.getMainValuePrefix(ft);
        if (sf.multiValued() || ft.multiValuedFieldCache() || prefix!=null) {
          UnInvertedField uif = UnInvertedField.getUnInvertedField(f, searcher);
          stv = uif.getStats(searcher, docs, facets).getStatsValues();
        } else {
          stv = getFieldCacheStats(f, facets);
        }
        if (isShard == true || (Long) stv.get("count") > 0) {
          res.add(f, stv);
        } else {
          res.add(f, null);
        }
      }
    }
    return res;
  }
  public NamedList getFieldCacheStats(String fieldName, String[] facet ) {
    FieldType ft = searcher.getSchema().getFieldType(fieldName);
    FieldCache.StringIndex si = null;
    try {
      si = FieldCache.DEFAULT.getStringIndex(searcher.getReader(), fieldName);
    } 
    catch (IOException e) {
      throw new RuntimeException( "failed to open field cache for: "+fieldName, e );
    }
    FieldFacetStats all = new FieldFacetStats( "all", si, ft, 0 );
    StatsValues allstats = new StatsValues();
    if ( all.nTerms <= 0 || docs.size() <= 0 ) return allstats.getStatsValues();
    int i=0;
    final FieldFacetStats[] finfo = new FieldFacetStats[facet.length];
    for( String f : facet ) {
      ft = searcher.getSchema().getFieldType(f);
      try {
        si = FieldCache.DEFAULT.getStringIndex(searcher.getReader(), f);
      } 
      catch (IOException e) {
        throw new RuntimeException( "failed to open field cache for: "+f, e );
      }
      finfo[i++] = new FieldFacetStats( f, si, ft, 0 );
    }
    DocIterator iter = docs.iterator();
    while (iter.hasNext()) {
      int docID = iter.nextDoc();
      String raw = all.getTermText(docID);
      Double v = null;
      if( raw != null ) {
        v = Double.parseDouble( all.ft.indexedToReadable(raw) );
        allstats.accumulate( v );
      }
      else {
        allstats.missing++;
      }
      for( FieldFacetStats f : finfo ) {
        f.facet(docID, v);
      }
    }
    if( finfo.length > 0 ) {
      allstats.facets = new HashMap<String, Map<String,StatsValues>>();
      for( FieldFacetStats f : finfo ) {
        allstats.facets.put( f.name, f.facetStatsValues );
      }
    }
    return allstats.getStatsValues();
  }
}
