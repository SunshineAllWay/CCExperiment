package org.apache.solr.client.solrj;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
public abstract class SolrRequest implements Serializable
{
  public enum METHOD {
    GET,
    POST
  };
  private METHOD method = METHOD.GET;
  private String path = null;
  private ResponseParser responseParser;
  public SolrRequest( METHOD m, String path )
  {
    this.method = m;
    this.path = path;
  }
  public METHOD getMethod() {
    return method;
  }
  public void setMethod(METHOD method) {
    this.method = method;
  }
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public ResponseParser getResponseParser() {
    return responseParser;
  }
  public void setResponseParser(ResponseParser responseParser) {
    this.responseParser = responseParser;
  }
  public abstract SolrParams getParams();
  public abstract Collection<ContentStream> getContentStreams() throws IOException;
  public abstract SolrResponse process( SolrServer server ) throws SolrServerException, IOException;
}
