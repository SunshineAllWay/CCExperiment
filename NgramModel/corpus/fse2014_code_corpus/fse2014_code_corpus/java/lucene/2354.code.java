package org.apache.solr.handler.component;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.Collections;
public class HighlightComponent extends SearchComponent implements PluginInfoInitialized, SolrCoreAware
{
  public static final String COMPONENT_NAME = "highlight";
  private PluginInfo info = PluginInfo.EMPTY_INFO;
  private SolrHighlighter highlighter;
  public void init(PluginInfo info) {
    this.info = info;
  }
  @Override
  public void prepare(ResponseBuilder rb) throws IOException {
    rb.doHighlights = highlighter.isHighlightingEnabled(rb.req.getParams());
  }
  public void inform(SolrCore core) {
    List<PluginInfo> children = info.getChildren("highlighting");
    if(children.isEmpty()) {
      PluginInfo pluginInfo = core.getSolrConfig().getPluginInfo(SolrHighlighter.class.getName()); 
      if (pluginInfo != null) {
        highlighter = core.createInitInstance(pluginInfo, SolrHighlighter.class, null, DefaultSolrHighlighter.class.getName());
        highlighter.initalize(core.getSolrConfig());
      } else {
        DefaultSolrHighlighter defHighlighter = new DefaultSolrHighlighter(core);
        defHighlighter.init(PluginInfo.EMPTY_INFO);
        highlighter = defHighlighter;
      }
    } else {
      highlighter = core.createInitInstance(children.get(0),SolrHighlighter.class,null, DefaultSolrHighlighter.class.getName());
    }
  }
  @Override
  public void process(ResponseBuilder rb) throws IOException {
    SolrQueryRequest req = rb.req;
    if (rb.doHighlights) {
      SolrParams params = req.getParams();
      String[] defaultHighlightFields;  
      if (rb.getQparser() != null) {
        defaultHighlightFields = rb.getQparser().getDefaultHighlightFields();
      } else {
        defaultHighlightFields = params.getParams(CommonParams.DF);
      }
      Query highlightQuery = rb.getHighlightQuery();
      if(highlightQuery==null) {
        if (rb.getQparser() != null) {
          try {
            highlightQuery = rb.getQparser().getHighlightQuery();
            rb.setHighlightQuery( highlightQuery );
          } catch (Exception e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
          }
        } else {
          highlightQuery = rb.getQuery();
          rb.setHighlightQuery( highlightQuery );
        }
      }
      if(highlightQuery != null) {
        boolean rewrite = !(Boolean.valueOf(req.getParams().get(HighlightParams.USE_PHRASE_HIGHLIGHTER, "true")) && Boolean.valueOf(req.getParams().get(HighlightParams.HIGHLIGHT_MULTI_TERM, "true")));
        highlightQuery = rewrite ?  highlightQuery.rewrite(req.getSearcher().getReader()) : highlightQuery;
      }
      if( highlightQuery != null ) {
        NamedList sumData = highlighter.doHighlighting(
                rb.getResults().docList,
                highlightQuery,
                req, defaultHighlightFields );
        if(sumData != null) {
          rb.rsp.add("highlighting", sumData);
        }
      }
    }
  }
  public void modifyRequest(ResponseBuilder rb, SearchComponent who, ShardRequest sreq) {
    if (!rb.doHighlights) return;
    if ((sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS) != 0) {
        sreq.purpose |= ShardRequest.PURPOSE_GET_HIGHLIGHTS;
        sreq.params.set(HighlightParams.HIGHLIGHT, "true");      
    } else {
      sreq.params.set(HighlightParams.HIGHLIGHT, "false");      
    }
  }
  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
  }
  @Override
  public void finishStage(ResponseBuilder rb) {
    if (rb.doHighlights && rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
      Map.Entry<String, Object>[] arr = new NamedList.NamedListEntry[rb.resultIds.size()];
      for (ShardRequest sreq : rb.finished) {
        if ((sreq.purpose & ShardRequest.PURPOSE_GET_HIGHLIGHTS) == 0) continue;
        for (ShardResponse srsp : sreq.responses) {
          NamedList hl = (NamedList)srsp.getSolrResponse().getResponse().get("highlighting");
          for (int i=0; i<hl.size(); i++) {
            String id = hl.getName(i);
            ShardDoc sdoc = rb.resultIds.get(id);
            int idx = sdoc.positionInResponse;
            arr[idx] = new NamedList.NamedListEntry<Object>(id, hl.getVal(i));
          }
        }
      }
      rb.rsp.add("highlighting", removeNulls(new SimpleOrderedMap(arr)));      
    }
  }
  static NamedList removeNulls(NamedList nl) {
    for (int i=0; i<nl.size(); i++) {
      if (nl.getName(i)==null) {
        NamedList newList = nl instanceof SimpleOrderedMap ? new SimpleOrderedMap() : new NamedList();
        for (int j=0; j<nl.size(); j++) {
          String n = nl.getName(j);
          if (n != null) {
            newList.add(n, nl.getVal(j));
          }
        }
        return newList;
      }
    }
    return nl;
  }
  public SolrHighlighter getHighlighter() {
    return highlighter;
  }
  @Override
  public String getDescription() {
    return "Highlighting";
  }
  @Override
  public String getVersion() {
    return "$Revision: 899572 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: HighlightComponent.java 899572 2010-01-15 09:43:50Z noble $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/component/HighlightComponent.java $";
  }
  @Override
  public URL[] getDocs() {
    return null;
  }
}
