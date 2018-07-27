package org.apache.solr.response;
import java.io.Writer;
import java.io.IOException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
public class XMLResponseWriter implements QueryResponseWriter {
  public void init(NamedList n) {
  }
  public void write(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
    XMLWriter.writeResponse(writer,req,rsp);
  }
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    return CONTENT_TYPE_XML_UTF8;
  }
}
