package org.apache.solr.response;
import java.io.Writer;
import java.io.IOException;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
public abstract class GenericTextResponseWriter extends BaseResponseWriter
    implements QueryResponseWriter {
  public void write(Writer writer, SolrQueryRequest request,
      SolrQueryResponse response) throws IOException {
    super.write(getSingleResponseWriter(writer, request, response), request,
        response);
  }
  protected abstract SingleResponseWriter getSingleResponseWriter(
      Writer writer, SolrQueryRequest request, SolrQueryResponse response);
}
