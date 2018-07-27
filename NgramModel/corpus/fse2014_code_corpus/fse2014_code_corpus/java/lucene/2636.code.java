package org.apache.solr.client.solrj.impl;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.request.JavaBinUpdateRequestCodec;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.util.ContentStream;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class BinaryRequestWriter extends RequestWriter {
  public Collection<ContentStream> getContentStreams(SolrRequest req) throws IOException {
    if (req instanceof UpdateRequest) {
      UpdateRequest updateRequest = (UpdateRequest) req;
      if (isNull(updateRequest.getDocuments()) &&
              isNull(updateRequest.getDeleteById()) &&
              isNull(updateRequest.getDeleteQuery())
              && (updateRequest.getDocIterator() == null) ) {
        return null;
      }
      List<ContentStream> l = new ArrayList<ContentStream>();
      l.add(new LazyContentStream(updateRequest));
      return l;
    } else {
      return super.getContentStreams(req);
    }
  }
  public String getUpdateContentType() {
    return "application/octet-stream";
  }
  public ContentStream getContentStream(final UpdateRequest request) throws IOException {
    final BAOS baos = new BAOS();
      new JavaBinUpdateRequestCodec().marshal(request, baos);
    return new ContentStream() {
      public String getName() {
        return null;
      }
      public String getSourceInfo() {
        return "javabin";
      }
      public String getContentType() {
        return "application/octet-stream";
      }
      public Long getSize() 
      {
        return new Long(baos.size());
      }
      public InputStream getStream() throws IOException {
        return new ByteArrayInputStream(baos.getbuf(), 0, baos.size());
      }
      public Reader getReader() throws IOException {
        throw new RuntimeException("No reader available . this is a binarystream");
      }
    };
  }
  public void write(SolrRequest request, OutputStream os) throws IOException {
    if (request instanceof UpdateRequest) {
      UpdateRequest updateRequest = (UpdateRequest) request;
      new JavaBinUpdateRequestCodec().marshal(updateRequest, os);
    } 
  }
  class BAOS extends ByteArrayOutputStream {
    byte[] getbuf() {
      return super.buf;
    }
  }
  public String getPath(SolrRequest req) {
    if (req instanceof UpdateRequest) {
      return "/update/javabin";
    } else {
      return req.getPath();
    }
  }
}
