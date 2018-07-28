package org.apache.solr.handler.clustering;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.request.SolrRequestHandler;
public class ClusteringComponentTest extends AbstractClusteringTest {
  public void testComponent() throws Exception {
    SolrCore core = h.getCore();
    SearchComponent sc = core.getSearchComponent("clustering");
    assertTrue("sc is null and it shouldn't be", sc != null);
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(ClusteringComponent.COMPONENT_NAME, "true");
    params.add(CommonParams.Q, "*:*");
    params.add(ClusteringParams.USE_SEARCH_RESULTS, "true");
    SolrRequestHandler handler = core.getRequestHandler("standard");
    SolrQueryResponse rsp;
    rsp = new SolrQueryResponse();
    rsp.add("responseHeader", new SimpleOrderedMap());
    handler.handleRequest(new LocalSolrQueryRequest(core, params), rsp);
    NamedList values = rsp.getValues();
    Object clusters = values.get("clusters");
    assertTrue("clusters is null and it shouldn't be", clusters != null);
    params = new ModifiableSolrParams();
    params.add(ClusteringComponent.COMPONENT_NAME, "true");
    params.add(ClusteringParams.ENGINE_NAME, "mock");
    params.add(ClusteringParams.USE_COLLECTION, "true");
    params.add(QueryComponent.COMPONENT_NAME, "false");
    handler = core.getRequestHandler("docClustering");
    rsp = new SolrQueryResponse();
    rsp.add("responseHeader", new SimpleOrderedMap());
    handler.handleRequest(new LocalSolrQueryRequest(core, params), rsp);
    values = rsp.getValues();
    clusters = values.get("clusters");
    assertTrue("clusters is null and it shouldn't be", clusters != null);
  }
}
