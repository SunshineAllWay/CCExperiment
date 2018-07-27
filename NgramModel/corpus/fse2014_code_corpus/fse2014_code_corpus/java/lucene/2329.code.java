package org.apache.solr.handler;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
public class DumpRequestHandler extends RequestHandlerBase
{
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws IOException 
  {
    rsp.add( "params", req.getParams().toNamedList() );
    if( req.getContentStreams() != null ) {
      ArrayList streams = new ArrayList();
      for( ContentStream content : req.getContentStreams() ) {
        NamedList<Object> stream = new SimpleOrderedMap<Object>();
        stream.add( "name", content.getName() );
        stream.add( "sourceInfo", content.getSourceInfo() );
        stream.add( "size", content.getSize() );
        stream.add( "contentType", content.getContentType() );
        InputStream is = content.getStream();
        try {
          stream.add( "stream", IOUtils.toString(is) );
        } finally {
          is.close();
        }
        streams.add( stream );
      }
      rsp.add( "streams", streams );
    }
    rsp.add("context", req.getContext());
  }
  @Override
  public String getDescription() {
    return "Dump handler (debug)";
  }
  @Override
  public String getVersion() {
      return "$Revision: 906553 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: DumpRequestHandler.java 906553 2010-02-04 16:26:38Z markrmiller $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/DumpRequestHandler.java $";
  }
}
