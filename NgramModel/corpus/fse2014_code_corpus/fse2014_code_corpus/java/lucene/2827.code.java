package org.apache.solr.request;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.JavaBinCodec;
import org.apache.solr.response.BinaryQueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.AbstractSolrTestCase;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
public class TestBinaryResponseWriter extends AbstractSolrTestCase {
  public String getSchemaFile() {
    return "schema12.xml";
  }
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  public void testUUID() throws Exception {
    String s = UUID.randomUUID().toString().toLowerCase();
    assertU(adoc("id", "101", "uuid", s));
    assertU(commit());
    LocalSolrQueryRequest req = lrf.makeRequest("q", "*:*");
    SolrQueryResponse rsp = h.queryAndResponse(req.getParams().get(CommonParams.QT), req);
    BinaryQueryResponseWriter writer = (BinaryQueryResponseWriter) h.getCore().getQueryResponseWriter("javabin");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    writer.write(baos, req, rsp);
    NamedList res = (NamedList) new JavaBinCodec().unmarshal(new ByteArrayInputStream(baos.toByteArray()));
    SolrDocumentList docs = (SolrDocumentList) res.get("response");
    for (Object doc : docs) {
      SolrDocument document = (SolrDocument) doc;
      assertEquals("Returned object must be a string", "java.lang.String", document.getFieldValue("uuid").getClass().getName());
      assertEquals("Wrong UUID string returned", s, document.getFieldValue("uuid"));
    }
  }
}
