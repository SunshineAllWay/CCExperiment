package org.apache.solr.handler;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
public abstract class ContentStreamLoader {
  protected String errHeader;
  public String getErrHeader() {
    return errHeader;
  }
  public void setErrHeader(String errHeader) {
    this.errHeader = errHeader;
  }
  public abstract void load(SolrQueryRequest req, SolrQueryResponse rsp, ContentStream stream) throws Exception;
}
