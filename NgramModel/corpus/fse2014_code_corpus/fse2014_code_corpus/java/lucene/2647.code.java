package org.apache.solr.client.solrj.request;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FieldAnalysisResponse;
import org.apache.solr.common.params.AnalysisParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
public class FieldAnalysisRequest extends SolrRequest {
  private String fieldValue;
  private String query;
  private boolean showMatch;
  private List<String> fieldNames;
  private List<String> fieldTypes;
  public FieldAnalysisRequest() {
    super(METHOD.GET, "/analysis/field");
  }
  public FieldAnalysisRequest(String uri) {
    super(METHOD.GET, uri);
  }
  @Override
  public Collection<ContentStream> getContentStreams() throws IOException {
    return null;
  }
  @Override
  public SolrParams getParams() {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set(AnalysisParams.FIELD_VALUE, fieldValue);
    if (query != null) {
      params.add(AnalysisParams.QUERY, query);
      params.add(AnalysisParams.SHOW_MATCH, String.valueOf(showMatch));
    }
    if (fieldNames != null) {
      String fieldNameValue = listToCommaDelimitedString(fieldNames);
      params.add(AnalysisParams.FIELD_NAME, fieldNameValue);
    }
    if (fieldTypes != null) {
      String fieldTypeValue = listToCommaDelimitedString(fieldTypes);
      params.add(AnalysisParams.FIELD_TYPE, fieldTypeValue);
    }
    return params;
  }
  @Override
  public FieldAnalysisResponse process(SolrServer server) throws SolrServerException, IOException {
    if (fieldTypes == null || fieldNames == null) {
      throw new IllegalStateException("A list one field type or field name need to be specified");
    }
    if (fieldValue == null) {
      throw new IllegalStateException("The field value must be set");
    }
    long startTime = System.currentTimeMillis();
    FieldAnalysisResponse res = new FieldAnalysisResponse();
    res.setResponse(server.request(this));
    res.setElapsedTime(System.currentTimeMillis() - startTime);
    return res;
  }
  static String listToCommaDelimitedString(List<String> list) {
    StringBuilder result = new StringBuilder();
    for (String str : list) {
      if (result.length() > 0) {
        result.append(",");
      }
      result.append(str);
    }
    return result.toString();
  }
  public FieldAnalysisRequest setFieldValue(String fieldValue) {
    this.fieldValue = fieldValue;
    return this;
  }
  public String getFieldValue() {
    return fieldValue;
  }
  public FieldAnalysisRequest setQuery(String query) {
    this.query = query;
    return this;
  }
  public String getQuery() {
    return query;
  }
  public FieldAnalysisRequest setShowMatch(boolean showMatch) {
    this.showMatch = showMatch;
    return this;
  }
  public boolean isShowMatch() {
    return showMatch;
  }
  public FieldAnalysisRequest addFieldName(String fieldName) {
    if (fieldNames == null) {
      fieldNames = new LinkedList<String>();
    }
    fieldNames.add(fieldName);
    return this;
  }
  public FieldAnalysisRequest setFieldNames(List<String> fieldNames) {
    this.fieldNames = fieldNames;
    return this;
  }
  public List<String> getFieldNames() {
    return fieldNames;
  }
  public FieldAnalysisRequest addFieldType(String fieldTypeName) {
    if (fieldTypes == null) {
      fieldTypes = new LinkedList<String>();
    }
    fieldTypes.add(fieldTypeName);
    return this;
  }
  public FieldAnalysisRequest setFieldTypes(List<String> fieldTypes) {
    this.fieldTypes = fieldTypes;
    return this;
  }
  public List<String> getFieldTypes() {
    return fieldTypes;
  }
}
