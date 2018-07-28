package org.apache.solr.response;
import java.io.Writer;
import java.io.OutputStream;
import java.io.IOException;
import org.apache.solr.request.SolrQueryRequest;
public interface BinaryQueryResponseWriter extends QueryResponseWriter{
    public void write(OutputStream out, SolrQueryRequest request, SolrQueryResponse response) throws IOException;
}
