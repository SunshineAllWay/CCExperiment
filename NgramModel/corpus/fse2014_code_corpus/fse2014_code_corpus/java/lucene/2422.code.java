package org.apache.solr.response;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
public class RawResponseWriter implements QueryResponseWriter 
{
  public static final String CONTENT = "content";
  private String _baseWriter = null;
  public void init(NamedList n) {
    if( n != null ) {
      Object base = n.get( "base" );
      if( base != null ) {
        _baseWriter = base.toString();
      }
    }
  }
  protected QueryResponseWriter getBaseWriter( SolrQueryRequest request )
  {
    return request.getCore().getQueryResponseWriter( _baseWriter );
  }
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    Object obj = response.getValues().get( CONTENT );
    if( obj != null && (obj instanceof ContentStream ) ) {
      return ((ContentStream)obj).getContentType();
    }
    return getBaseWriter( request ).getContentType( request, response );
  }
  public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException 
  {
    Object obj = response.getValues().get( CONTENT );
    if( obj != null && (obj instanceof ContentStream ) ) {
      ContentStream content = (ContentStream)obj;
      Reader reader = content.getReader();
      try {
        IOUtils.copy( reader, writer );
      } finally {
        reader.close();
      }
    }
    else {
      getBaseWriter( request ).write( writer, request, response );
    }
  }
}
