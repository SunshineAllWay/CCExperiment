package org.apache.solr.response;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
public abstract class GenericBinaryResponseWriter extends BaseResponseWriter
    implements BinaryQueryResponseWriter {
  public void write(OutputStream out, SolrQueryRequest request,
      SolrQueryResponse response) throws IOException {
    super.write(getSingleResponseWriter(out, request, response), request,
        response);
  }
  public abstract SingleResponseWriter getSingleResponseWriter(
      OutputStream out, SolrQueryRequest request, SolrQueryResponse response);
  public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException {
    throw new RuntimeException("This is a binary writer , Cannot write to a characterstream");
  }
}
