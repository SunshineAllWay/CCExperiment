package org.apache.solr.client.solrj.impl;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.JavaBinCodec;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
public class BinaryResponseParser extends ResponseParser {
  public String getWriterType() {
    return "javabin";
  }
  public NamedList<Object> processResponse(InputStream body, String encoding) {
    try {
      return (NamedList<Object>) new JavaBinCodec().unmarshal(body);
    } catch (IOException e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "parsing error", e);
    }
  }
  public String getVersion() {
    return "1";
  }
  public NamedList<Object> processResponse(Reader reader) {
    throw new RuntimeException("Cannot handle character stream");
  }
}
