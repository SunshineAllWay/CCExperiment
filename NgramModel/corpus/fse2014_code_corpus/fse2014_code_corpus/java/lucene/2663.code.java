package org.apache.solr.client.solrj.response;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.common.util.NamedList;
public class SolrResponseBase extends SolrResponse
{
  private long elapsedTime = -1;
  private NamedList<Object> response = null;
  private String requestUrl = null;
  @Override
  public long getElapsedTime() {
    return elapsedTime;
  }
  public void setElapsedTime(long elapsedTime) {
    this.elapsedTime = elapsedTime;
  }
  @Override
  public NamedList<Object> getResponse() {
    return response;
  }
  @Override
  public void setResponse(NamedList<Object> response) {
    this.response = response;
  }
  @Override
  public String toString() {
    return response.toString();
  }
  public NamedList getResponseHeader() {
    return (NamedList) response.get("responseHeader");
  }
  public int getStatus() {
    NamedList header = getResponseHeader();
    if (header != null) {
        return (Integer) header.get("status");
    }
    else {
        return 0;
    }
  }
  public int getQTime() {
    NamedList header = getResponseHeader();
    if (header != null) {
        return (Integer) header.get("QTime");
    }
    else {
        return 0;
    }
  }
  public String getRequestUrl() {
    return requestUrl;
  }
  public void setRequestUrl(String requestUrl) {
    this.requestUrl = requestUrl;
  }
}
