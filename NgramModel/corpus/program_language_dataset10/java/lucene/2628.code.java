package org.apache.solr.client.solrj;
import java.io.Reader;
import java.io.InputStream;
import org.apache.solr.common.util.NamedList;
public abstract class ResponseParser
{
  public abstract String getWriterType(); 
  public abstract NamedList<Object> processResponse(InputStream body, String encoding);
  public abstract NamedList<Object> processResponse(Reader reader);
  public String getVersion()
  {
    return "2.2";
  }
}
