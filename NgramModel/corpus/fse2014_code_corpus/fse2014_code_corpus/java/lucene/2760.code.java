package org.apache.solr.client.solrj.response;
import org.apache.solr.common.util.NamedList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
public class DocumentAnalysisResponseTest {
  @Test
  public void testSetResponse() throws Exception {
    final List<AnalysisResponseBase.AnalysisPhase> phases = new ArrayList<AnalysisResponseBase.AnalysisPhase>(1);
    AnalysisResponseBase.AnalysisPhase expectedPhase = new AnalysisResponseBase.AnalysisPhase("Tokenizer");
    phases.add(expectedPhase);
    NamedList responseNL = buildResponse();
    DocumentAnalysisResponse response = new DocumentAnalysisResponse() {
      @Override
      protected List<AnalysisPhase> buildPhases(NamedList<Object> phaseNL) {
        return phases;
      }
    };
    response.setResponse(responseNL);
    assertEquals(1, response.getDocumentAnalysesCount());
    DocumentAnalysisResponse.DocumentAnalysis documentAnalysis = response.getDocumentAnalysis("1");
    assertEquals("1", documentAnalysis.getDocumentKey());
    assertEquals(3, documentAnalysis.getFieldAnalysesCount());
    DocumentAnalysisResponse.FieldAnalysis fieldAnalysis = documentAnalysis.getFieldAnalysis("id");
    assertEquals("id", fieldAnalysis.getFieldName());
    assertEquals(1, fieldAnalysis.getQueryPhasesCount());
    AnalysisResponseBase.AnalysisPhase phase = fieldAnalysis.getQueryPhases().iterator().next();
    assertSame(expectedPhase, phase);
    assertEquals(1, fieldAnalysis.getValueCount());
    assertEquals(1, fieldAnalysis.getIndexPhasesCount("1"));
    phase = fieldAnalysis.getIndexPhases("1").iterator().next();
    assertSame(expectedPhase, phase);
    fieldAnalysis = documentAnalysis.getFieldAnalysis("name");
    assertEquals("name", fieldAnalysis.getFieldName());
    assertEquals(1, fieldAnalysis.getQueryPhasesCount());
    phase = fieldAnalysis.getQueryPhases().iterator().next();
    assertSame(expectedPhase, phase);
    assertEquals(2, fieldAnalysis.getValueCount());
    assertEquals(1, fieldAnalysis.getIndexPhasesCount("name value 1"));
    phase = fieldAnalysis.getIndexPhases("name value 1").iterator().next();
    assertSame(expectedPhase, phase);
    assertEquals(1, fieldAnalysis.getIndexPhasesCount("name value 2"));
    phase = fieldAnalysis.getIndexPhases("name value 2").iterator().next();
    assertSame(expectedPhase, phase);
    fieldAnalysis = documentAnalysis.getFieldAnalysis("text");
    assertEquals("text", fieldAnalysis.getFieldName());
    assertEquals(1, fieldAnalysis.getQueryPhasesCount());
    phase = fieldAnalysis.getQueryPhases().iterator().next();
    assertSame(expectedPhase, phase);
    assertEquals(1, fieldAnalysis.getValueCount());
    assertEquals(1, fieldAnalysis.getIndexPhasesCount("text value"));
    phase = fieldAnalysis.getIndexPhases("text value").iterator().next();
    assertSame(expectedPhase, phase);
  }
  private NamedList buildResponse() {
    NamedList response = new NamedList();
    NamedList responseHeader = new NamedList();
    response.add("responseHeader", responseHeader);
    NamedList params = new NamedList();
    responseHeader.add("params", params);
    params.add("analysis.showmatch", "true");
    params.add("analysis.query", "the query");
    responseHeader.add("status", 0);
    responseHeader.add("QTime", 105);
    NamedList analysis = new NamedList();
    response.add("analysis", analysis);
    NamedList doc1 = new NamedList();
    analysis.add("1", doc1);
    NamedList id = new NamedList();
    doc1.add("id", id);
    NamedList query = new NamedList();
    id.add("query", query);
    NamedList index = new NamedList();
    id.add("index", index);
    NamedList idValue = new NamedList();
    index.add("1", idValue);
    NamedList name = new NamedList();
    doc1.add("name", name);
    query = new NamedList();
    name.add("query", query);
    index = new NamedList();
    name.add("index", index);
    NamedList nameValue1 = new NamedList();
    index.add("name value 1", nameValue1);
    NamedList nameValue2 = new NamedList();
    index.add("name value 2", nameValue2);
    NamedList text = new NamedList();
    doc1.add("text", text);
    query = new NamedList();
    text.add("query", query);
    index = new NamedList();
    text.add("index", index);
    NamedList textValue = new NamedList();
    index.add("text value", textValue);
    return response;
  }
}
