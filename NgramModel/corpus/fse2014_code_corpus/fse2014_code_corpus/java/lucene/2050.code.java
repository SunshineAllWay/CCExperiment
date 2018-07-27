package org.apache.solr.handler.clustering.carrot2;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.clustering.SearchClusteringEngine;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;
import org.apache.solr.util.RefCounted;
import org.carrot2.core.*;
import org.carrot2.core.attribute.AttributeNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Sets;
@SuppressWarnings("unchecked")
public class CarrotClusteringEngine extends SearchClusteringEngine {
  private transient static Logger log = LoggerFactory
          .getLogger(CarrotClusteringEngine.class);
  private CachingController controller = new CachingController();
  private Class<? extends IClusteringAlgorithm> clusteringAlgorithmClass;
  private String idFieldName;
  public Object cluster(Query query, DocList docList, SolrQueryRequest sreq) {
    try {
      Map<String, Object> attributes = new HashMap<String, Object>();
      List<Document> documents = getDocuments(docList, query, sreq);
      attributes.put(AttributeNames.DOCUMENTS, documents);
      attributes.put(AttributeNames.QUERY, query.toString());
      extractCarrotAttributes(sreq.getParams(), attributes);
      return clustersToNamedList(controller.process(attributes,
              clusteringAlgorithmClass).getClusters(), sreq.getParams());
    } catch (Exception e) {
      log.error("Carrot2 clustering failed", e);
      throw new RuntimeException(e);
    }
  }
  @Override
  public String init(NamedList config, final SolrCore core) {
    String result = super.init(config, core);
    SolrParams initParams = SolrParams.toSolrParams(config);
    HashMap<String, Object> initAttributes = new HashMap<String, Object>();
    extractCarrotAttributes(initParams, initAttributes);
    this.controller.init(initAttributes);
    this.idFieldName = core.getSchema().getUniqueKeyField().getName();
    String carrotAlgorithmClassName = initParams.get(CarrotParams.ALGORITHM);
    Class<?> algorithmClass = core.getResourceLoader().findClass(carrotAlgorithmClassName);
    if (!IClusteringAlgorithm.class.isAssignableFrom(algorithmClass)) {
      throw new IllegalArgumentException("Class provided as "
              + CarrotParams.ALGORITHM + " must implement "
              + IClusteringAlgorithm.class.getName());
    }
    this.clusteringAlgorithmClass = (Class<? extends IClusteringAlgorithm>) algorithmClass;
    return result;
  }
  private List<Document> getDocuments(DocList docList,
                                      Query query, final SolrQueryRequest sreq) throws IOException {
    SolrHighlighter highlighter = null;
    SolrParams solrParams = sreq.getParams();
    SolrCore core = sreq.getCore();
    String urlField = solrParams.get(CarrotParams.URL_FIELD_NAME, "url");
    String titleField = solrParams.get(CarrotParams.TITLE_FIELD_NAME, "title");
    String snippetField = solrParams.get(CarrotParams.SNIPPET_FIELD_NAME,
            titleField);
    if (StringUtils.isBlank(snippetField)) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, CarrotParams.SNIPPET_FIELD_NAME
              + " must not be blank.");
    }
    Set<String> fieldsToLoad = Sets.newHashSet(urlField, titleField,
            snippetField, idFieldName);
    DocIterator docsIter = docList.iterator();
    boolean produceSummary = solrParams.getBool(CarrotParams.PRODUCE_SUMMARY,
            false);
    SolrQueryRequest req = null;
    String[] snippetFieldAry = null;
    if (produceSummary == true) {
      highlighter = core.getHighlighter();
      if (highlighter != null){
        Map args = new HashMap();
        snippetFieldAry = new String[]{snippetField};
        args.put(HighlightParams.FIELDS, snippetFieldAry);
        args.put(HighlightParams.HIGHLIGHT, "true");
        args.put(HighlightParams.SIMPLE_PRE, ""); 
        args.put(HighlightParams.SIMPLE_POST, "");
        args.put(HighlightParams.FRAGSIZE, solrParams.getInt(CarrotParams.SUMMARY_FRAGSIZE, solrParams.getInt(HighlightParams.FRAGSIZE, 100)));
        req = new LocalSolrQueryRequest(core, query.toString(), "", 0, 1, args) {
          @Override
          public SolrIndexSearcher getSearcher() {
            return sreq.getSearcher();
          }
        };
      } else {
        log.warn("No highlighter configured, cannot produce summary");
        produceSummary = false;
      }
    }
    SolrIndexSearcher searcher = sreq.getSearcher();
    List<Document> result = new ArrayList<Document>(docList.size());
    float[] scores = {1.0f};
    int[] docsHolder = new int[1];
    Query theQuery = query;
    while (docsIter.hasNext()) {
      Integer id = docsIter.next();
      org.apache.lucene.document.Document doc = searcher.doc(id,
              fieldsToLoad);
      String snippet = getValue(doc, snippetField);
      if (produceSummary == true) {
        docsHolder[0] = id.intValue();
        DocList docAsList = new DocSlice(0, 1, docsHolder, scores, 1, 1.0f);
        NamedList highlights = highlighter.doHighlighting(docAsList, theQuery, req, snippetFieldAry);
        if (highlights != null && highlights.size() == 1) {
          NamedList tmp = (NamedList) highlights.getVal(0);
          String [] highlt = (String[]) tmp.get(snippetField);
          if (highlt != null && highlt.length == 1) {
            snippet = highlt[0];
          }
        }
      }
      Document carrotDocument = new Document(getValue(doc, titleField),
              snippet, doc.get(urlField));
      carrotDocument.setField("solrId", doc.get(idFieldName));
      result.add(carrotDocument);
    }
    return result;
  }
  protected String getValue(org.apache.lucene.document.Document doc,
                            String field) {
    StringBuilder result = new StringBuilder();
    String[] vals = doc.getValues(field);
    for (int i = 0; i < vals.length; i++) {
      result.append(vals[i]).append(" . ");
    }
    return result.toString().trim();
  }
  private List clustersToNamedList(List<Cluster> carrotClusters,
                                   SolrParams solrParams) {
    List result = new ArrayList();
    clustersToNamedList(carrotClusters, result, solrParams.getBool(
            CarrotParams.OUTPUT_SUB_CLUSTERS, true), solrParams.getInt(
            CarrotParams.NUM_DESCRIPTIONS, Integer.MAX_VALUE));
    return result;
  }
  private void clustersToNamedList(List<Cluster> outputClusters,
                                   List parent, boolean outputSubClusters, int maxLabels) {
    for (Cluster outCluster : outputClusters) {
      NamedList cluster = new SimpleOrderedMap();
      parent.add(cluster);
      List<String> labels = outCluster.getPhrases();
      if (labels.size() > maxLabels)
        labels = labels.subList(0, maxLabels);
      cluster.add("labels", labels);
      List<Document> docs = outputSubClusters ? outCluster.getDocuments() : outCluster.getAllDocuments();
      List docList = new ArrayList();
      cluster.add("docs", docList);
      for (Document doc : docs) {
        docList.add(doc.getField("solrId"));
      }
      if (outputSubClusters) {
        List subclusters = new ArrayList();
        cluster.add("clusters", subclusters);
        clustersToNamedList(outCluster.getSubclusters(), subclusters,
                outputSubClusters, maxLabels);
      }
    }
  }
  private void extractCarrotAttributes(SolrParams solrParams,
                                       Map<String, Object> attributes) {
    for (Iterator<String> paramNames = solrParams.getParameterNamesIterator(); paramNames
            .hasNext();) {
      String paramName = paramNames.next();
      if (!CarrotParams.CARROT_PARAM_NAMES.contains(paramName)) {
        attributes.put(paramName, solrParams.get(paramName));
      }
    }
  }
}
