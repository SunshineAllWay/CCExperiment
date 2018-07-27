package org.apache.solr.response;
import java.io.Writer;
import java.io.IOException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
public interface QueryResponseWriter extends NamedListInitializedPlugin {
  public static String CONTENT_TYPE_XML_UTF8="text/xml; charset=UTF-8";
  public static String CONTENT_TYPE_TEXT_UTF8="text/plain; charset=UTF-8";
  public static String CONTENT_TYPE_TEXT_ASCII="text/plain; charset=US-ASCII";
  public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException;
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response);
  public void init(NamedList args);
}
