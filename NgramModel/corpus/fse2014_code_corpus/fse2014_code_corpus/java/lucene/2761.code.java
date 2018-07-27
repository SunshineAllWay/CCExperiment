package org.apache.solr.client.solrj.response;
import org.apache.solr.common.util.NamedList;
import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
@SuppressWarnings("unchecked")
public class FieldAnalysisResponseTest {
  @Test
  public void testSetResponse() throws Exception {
    final List<AnalysisResponseBase.AnalysisPhase> phases = new ArrayList<AnalysisResponseBase.AnalysisPhase>(1);
    AnalysisResponseBase.AnalysisPhase expectedPhase = new AnalysisResponseBase.AnalysisPhase("Tokenizer");
    phases.add(expectedPhase);
    NamedList responseNL = buildResponse();
    FieldAnalysisResponse response = new FieldAnalysisResponse() {
      @Override
      protected List<AnalysisPhase> buildPhases(NamedList<Object> phaseNL) {
        return phases;
      }
    };
    response.setResponse(responseNL);
    assertEquals(1, response.getFieldNameAnalysisCount());
    FieldAnalysisResponse.Analysis analysis = response.getFieldNameAnalysis("name");
    Iterator<AnalysisResponseBase.AnalysisPhase> iter = analysis.getIndexPhases().iterator();
    assertTrue(iter.hasNext());
    assertSame(expectedPhase, iter.next());
    assertFalse(iter.hasNext());
    iter = analysis.getQueryPhases().iterator();
    assertTrue(iter.hasNext());
    assertSame(expectedPhase, iter.next());
    assertFalse(iter.hasNext());
    analysis = response.getFieldTypeAnalysis("text");
    iter = analysis.getIndexPhases().iterator();
    assertTrue(iter.hasNext());
    assertSame(expectedPhase, iter.next());
    assertFalse(iter.hasNext());
    iter = analysis.getQueryPhases().iterator();
    assertTrue(iter.hasNext());
    assertSame(expectedPhase, iter.next());
    assertFalse(iter.hasNext());
  }
  private NamedList buildResponse() {
    NamedList response = new NamedList();
    NamedList responseHeader = new NamedList();
    response.add("responseHeader", responseHeader);
    NamedList params = new NamedList();
    responseHeader.add("params", params);
    params.add("analysis.showmatch", "true");
    params.add("analysis.query", "the query");
    params.add("analysis.fieldname", "name");
    params.add("analysis.fieldvalue", "The field value");
    params.add("analysis.fieldtype", "text");
    responseHeader.add("status", 0);
    responseHeader.add("QTime", 66);
    NamedList analysis = new NamedList();
    response.add("analysis", analysis);
    NamedList fieldTypes = new NamedList();
    analysis.add("field_types", fieldTypes);
    NamedList text = new NamedList();
    fieldTypes.add("text", text);
    NamedList index = new NamedList();
    text.add("index", index);
    NamedList query = new NamedList();
    text.add("query", query);
    NamedList fieldNames = new NamedList();
    analysis.add("field_names", fieldNames);
    NamedList name = new NamedList();
    fieldNames.add("name", name);
    index = new NamedList();
    name.add("index", index);
    query = new NamedList();
    name.add("query", query);
    return response;
  }
}
