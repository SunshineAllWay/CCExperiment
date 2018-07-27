package org.apache.solr.handler.component;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.params.ModifiableSolrParams;
public class ShardRequest {
  public final static String[] ALL_SHARDS = null;
  public final static int PURPOSE_PRIVATE         = 0x01;
  public final static int PURPOSE_GET_TERM_DFS    = 0x02;
  public final static int PURPOSE_GET_TOP_IDS     = 0x04;
  public final static int PURPOSE_REFINE_TOP_IDS  = 0x08;
  public final static int PURPOSE_GET_FACETS      = 0x10;
  public final static int PURPOSE_REFINE_FACETS   = 0x20;
  public final static int PURPOSE_GET_FIELDS      = 0x40;
  public final static int PURPOSE_GET_HIGHLIGHTS  = 0x80;
  public final static int PURPOSE_GET_DEBUG       =0x100;
  public final static int PURPOSE_GET_STATS       =0x200;
  public final static int PURPOSE_GET_TERMS       =0x400;
  public int purpose;  
  public String[] shards;  
  public ModifiableSolrParams params;
  public List<ShardResponse> responses = new ArrayList<ShardResponse>();
  public String[] actualShards;
  public String toString() {
    return "ShardRequest:{params=" + params
            + ", purpose=" + Integer.toHexString(purpose)
            + ", nResponses =" + responses.size()
            + "}";
  }
}
