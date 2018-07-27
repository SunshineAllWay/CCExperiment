package org.apache.solr.client.solrj.request;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.DocumentAnalysisResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.AnalysisParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.ContentStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class DocumentAnalysisRequest extends SolrRequest {
  private List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
  private String query;
  private boolean showMatch = false;
  public DocumentAnalysisRequest() {
    super(METHOD.POST, "/analysis/document");
  }
  public DocumentAnalysisRequest(String uri) {
    super(METHOD.POST, uri);
  }
  @Override
  public Collection<ContentStream> getContentStreams() throws IOException {
    return ClientUtils.toContentStreams(getXML(), ClientUtils.TEXT_XML);
  }
  @Override
  public ModifiableSolrParams getParams() {
    ModifiableSolrParams params = new ModifiableSolrParams();
    if (query != null) {
      params.add(AnalysisParams.QUERY, query);
      params.add(AnalysisParams.SHOW_MATCH, String.valueOf(showMatch));
    }
    return params;
  }
  @Override
  public DocumentAnalysisResponse process(SolrServer server) throws SolrServerException, IOException {
    long startTime = System.currentTimeMillis();
    DocumentAnalysisResponse res = new DocumentAnalysisResponse();
    res.setResponse(server.request(this));
    res.setElapsedTime(System.currentTimeMillis() - startTime);
    return res;
  }
  String getXML() throws IOException {
    StringWriter writer = new StringWriter();
    writer.write("<docs>");
    for (SolrInputDocument document : documents) {
      ClientUtils.writeXML(document, writer);
    }
    writer.write("</docs>");
    writer.flush();
    String xml = writer.toString();
    return (xml.length() > 0) ? xml : null;
  }
  public DocumentAnalysisRequest addDocument(SolrInputDocument doc) {
    documents.add(doc);
    return this;
  }
  public DocumentAnalysisRequest addDocuments(Collection<SolrInputDocument> docs) {
    documents.addAll(docs);
    return this;
  }
  public DocumentAnalysisRequest setQuery(String query) {
    this.query = query;
    return this;
  }
  public DocumentAnalysisRequest setShowMatch(boolean showMatch) {
    this.showMatch = showMatch;
    return this;
  }
  public List<SolrInputDocument> getDocuments() {
    return documents;
  }
  public String getQuery() {
    return query;
  }
  public boolean isShowMatch() {
    return showMatch;
  }
}
