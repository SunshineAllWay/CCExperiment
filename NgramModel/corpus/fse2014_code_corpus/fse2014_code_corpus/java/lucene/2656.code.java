package org.apache.solr.client.solrj.response;
import org.apache.solr.common.util.NamedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class DocumentAnalysisResponse extends AnalysisResponseBase implements Iterable<Map.Entry<String, DocumentAnalysisResponse.DocumentAnalysis>> {
  private final Map<String, DocumentAnalysis> documentAnalysisByKey = new HashMap<String, DocumentAnalysis>();
  @Override
  public void setResponse(NamedList<Object> response) {
    super.setResponse(response);
    NamedList<Object> analysis = (NamedList<Object>) response.get("analysis");
    for (Map.Entry<String, Object> documentEntry : analysis) {
      DocumentAnalysis documentAnalysis = new DocumentAnalysis(documentEntry.getKey());
      NamedList<Object> document = (NamedList<Object>) documentEntry.getValue();
      for (Map.Entry<String, Object> fieldEntry : document) {
        FieldAnalysis fieldAnalysis = new FieldAnalysis(fieldEntry.getKey());
        NamedList field = (NamedList) fieldEntry.getValue();
        NamedList<Object> query = (NamedList<Object>) field.get("query");
        if (query != null) {
          List<AnalysisPhase> phases = buildPhases(query);
          fieldAnalysis.setQueryPhases(phases);
        }
        NamedList<Object> index = (NamedList<Object>) field.get("index");
        for (Map.Entry<String, Object> valueEntry : index) {
          String fieldValue = valueEntry.getKey();
          NamedList<Object> valueNL = (NamedList<Object>) valueEntry.getValue();
          List<AnalysisPhase> phases = buildPhases(valueNL);
          fieldAnalysis.setIndexPhases(fieldValue, phases);
        }
        documentAnalysis.addFieldAnalysis(fieldAnalysis);
      }
      documentAnalysisByKey.put(documentAnalysis.getDocumentKey(), documentAnalysis);
    }
  }
  public int getDocumentAnalysesCount() {
    return documentAnalysisByKey.size();
  }
  public DocumentAnalysis getDocumentAnalysis(String documentKey) {
    return documentAnalysisByKey.get(documentKey);
  }
  public Iterator<Map.Entry<String, DocumentAnalysis>> iterator() {
    return documentAnalysisByKey.entrySet().iterator();
  }
  public static class DocumentAnalysis implements Iterable<Map.Entry<String, FieldAnalysis>> {
    private final String documentKey;
    private Map<String, FieldAnalysis> fieldAnalysisByFieldName = new HashMap<String, FieldAnalysis>();
    private DocumentAnalysis(String documentKey) {
      this.documentKey = documentKey;
    }
    private void addFieldAnalysis(FieldAnalysis fieldAnalysis) {
      fieldAnalysisByFieldName.put(fieldAnalysis.getFieldName(), fieldAnalysis);
    }
    public String getDocumentKey() {
      return documentKey;
    }
    public int getFieldAnalysesCount() {
      return fieldAnalysisByFieldName.size();
    }
    public FieldAnalysis getFieldAnalysis(String fieldName) {
      return fieldAnalysisByFieldName.get(fieldName);
    }
    public Iterator<Map.Entry<String, FieldAnalysis>> iterator() {
      return fieldAnalysisByFieldName.entrySet().iterator();
    }
  }
  public static class FieldAnalysis {
    private final String fieldName;
    private List<AnalysisPhase> queryPhases;
    private Map<String, List<AnalysisPhase>> indexPhasesByFieldValue = new HashMap<String, List<AnalysisPhase>>();
    private FieldAnalysis(String fieldName) {
      this.fieldName = fieldName;
    }
    public void setQueryPhases(List<AnalysisPhase> queryPhases) {
      this.queryPhases = queryPhases;
    }
    public void setIndexPhases(String fieldValue, List<AnalysisPhase> indexPhases) {
      indexPhasesByFieldValue.put(fieldValue, indexPhases);
    }
    public String getFieldName() {
      return fieldName;
    }
    public int getQueryPhasesCount() {
      return queryPhases == null ? -1 : queryPhases.size();
    }
    public Iterable<AnalysisPhase> getQueryPhases() {
      return queryPhases;
    }
    public int getValueCount() {
      return indexPhasesByFieldValue.entrySet().size();
    }
    public int getIndexPhasesCount(String fieldValue) {
      return indexPhasesByFieldValue.get(fieldValue).size();
    }
    public Iterable<AnalysisPhase> getIndexPhases(String fieldValue) {
      return indexPhasesByFieldValue.get(fieldValue);
    }
    public Iterable<Map.Entry<String, List<AnalysisPhase>>> getIndexPhasesByFieldValue() {
      return indexPhasesByFieldValue.entrySet();
    }
  }
}
