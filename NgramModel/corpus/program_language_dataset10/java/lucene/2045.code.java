package org.apache.solr.handler.clustering;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.clustering.carrot2.CarrotClusteringEngine;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
public class ClusteringComponent extends SearchComponent implements SolrCoreAware {
  private transient static Logger log = LoggerFactory.getLogger(ClusteringComponent.class);
  private Map<String, SearchClusteringEngine> searchClusteringEngines = new HashMap<String, SearchClusteringEngine>();
  private Map<String, DocumentClusteringEngine> documentClusteringEngines = new HashMap<String, DocumentClusteringEngine>();
  public static final String COMPONENT_NAME = "clustering";
  private NamedList initParams;
  public void prepare(ResponseBuilder rb) throws IOException {
    SolrParams params = rb.req.getParams();
    if (!params.getBool(COMPONENT_NAME, false)) {
      return;
    }
  }
  public void process(ResponseBuilder rb) throws IOException {
    SolrParams params = rb.req.getParams();
    if (!params.getBool(COMPONENT_NAME, false)) {
      return;
    }
    String name = params.get(ClusteringParams.ENGINE_NAME, ClusteringEngine.DEFAULT_ENGINE_NAME);
    boolean useResults = params.getBool(ClusteringParams.USE_SEARCH_RESULTS, false);
    if (useResults == true) {
      SearchClusteringEngine engine = searchClusteringEngines.get(name);
      if (engine != null) {
        DocListAndSet results = rb.getResults();
        Object clusters = engine.cluster(rb.getQuery(), results.docList, rb.req);
        rb.rsp.add("clusters", clusters);
      } else {
        log.warn("No engine for: " + name);
      }
    }
    boolean useCollection = params.getBool(ClusteringParams.USE_COLLECTION, false);
    if (useCollection == true) {
      DocumentClusteringEngine engine = documentClusteringEngines.get(name);
      if (engine != null) {
        boolean useDocSet = params.getBool(ClusteringParams.USE_DOC_SET, false);
        NamedList nl = null;
        if (useDocSet == true) {
          nl = engine.cluster(rb.getResults().docSet, params);
        } else {
          nl = engine.cluster(params);
        }
        rb.rsp.add("clusters", nl);
      } else {
        log.warn("No engine for " + name);
      }
    }
  }
  @Override
  @SuppressWarnings("unchecked")
  public void init(NamedList args) {
    super.init(args);
    this.initParams = args;
  }
  public void inform(SolrCore core) {
    if (initParams != null) {
      log.info("Initializing Clustering Engines");
      boolean searchHasDefault = false;
      boolean documentHasDefault = false;
      for (int i = 0; i < initParams.size(); i++) {
        if (initParams.getName(i).equals("engine")) {
          NamedList engineNL = (NamedList) initParams.getVal(i);
          String className = (String) engineNL.get("classname");
          if (className == null) {
            className = CarrotClusteringEngine.class.getName();
          }
          SolrResourceLoader loader = core.getResourceLoader();
          ClusteringEngine clusterer = (ClusteringEngine) loader.newInstance(className);
          if (clusterer != null) {
            String name = clusterer.init(engineNL, core);
            if (name != null) {
              boolean isDefault = name.equals(ClusteringEngine.DEFAULT_ENGINE_NAME);
              if (clusterer instanceof SearchClusteringEngine) {
                if (isDefault == true && searchHasDefault == false) {
                  searchHasDefault = true;
                } else if (isDefault == true && searchHasDefault == true) {
                  throw new RuntimeException("More than one engine is missing name: " + engineNL);
                }
                searchClusteringEngines.put(name, (SearchClusteringEngine) clusterer);
              } else if (clusterer instanceof DocumentClusteringEngine) {
                if (isDefault == true && documentHasDefault == false) {
                  searchHasDefault = true;
                } else if (isDefault == true && documentHasDefault == true) {
                  throw new RuntimeException("More than one engine is missing name: " + engineNL);
                }
                documentClusteringEngines.put(name, (DocumentClusteringEngine) clusterer);
              }
            } else {
              if (clusterer instanceof SearchClusteringEngine && searchHasDefault == false) {
                searchClusteringEngines.put(ClusteringEngine.DEFAULT_ENGINE_NAME, (SearchClusteringEngine) clusterer);
                searchHasDefault = true;
              } else if (clusterer instanceof DocumentClusteringEngine && documentHasDefault == false) {
                documentClusteringEngines.put(ClusteringEngine.DEFAULT_ENGINE_NAME, (DocumentClusteringEngine) clusterer);
                documentHasDefault = true;
              } else {
                throw new RuntimeException("More than one engine is missing name: " + engineNL);
              }
            }
          }
        }
      }
      log.info("Finished Initializing Clustering Engines");
    }
  }
  public Map<String, SearchClusteringEngine> getSearchClusteringEngines() {
    return Collections.unmodifiableMap(searchClusteringEngines);
  }
  @Override
  public String getDescription() {
    return "A Clustering component";
  }
  @Override
  public String getVersion() {
    return "$Revision:$";
  }
  @Override
  public String getSourceId() {
    return "$Id:$";
  }
  @Override
  public String getSource() {
    return "$URL:$";
  }
}
