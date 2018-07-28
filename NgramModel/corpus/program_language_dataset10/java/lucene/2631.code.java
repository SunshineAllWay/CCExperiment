package org.apache.solr.client.solrj;
import java.io.Serializable;
import org.apache.solr.common.util.NamedList;
public abstract class SolrResponse implements Serializable
{
  public abstract long getElapsedTime();
  public abstract void setResponse(  NamedList<Object> rsp );
  public abstract NamedList<Object> getResponse();
}
