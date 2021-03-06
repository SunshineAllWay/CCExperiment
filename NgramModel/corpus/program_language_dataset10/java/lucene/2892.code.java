package org.apache.solr.servlet;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.XmlUpdateRequestHandler;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.XMLResponseWriter;
@Deprecated
public class SolrUpdateServlet extends HttpServlet {
  final Logger log = LoggerFactory.getLogger(SolrUpdateServlet.class);
  XmlUpdateRequestHandler legacyUpdateHandler;
  XMLResponseWriter xmlResponseWriter;
  private boolean hasMulticore = false;
  @Override
  public void init() throws ServletException
  {
    legacyUpdateHandler = new XmlUpdateRequestHandler();
    legacyUpdateHandler.init( null );
    String instanceDir = SolrResourceLoader.locateInstanceDir();
    File fconf = new File(instanceDir, "solr.xml");
    hasMulticore = fconf.exists();
    log.info("SolrUpdateServlet.init() done");
  }
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if( hasMulticore ) {
      response.sendError( 400, "Missing solr core name in path" );
      return;
    }
    BufferedReader requestReader = request.getReader();
    response.setContentType(QueryResponseWriter.CONTENT_TYPE_XML_UTF8);
    if( request.getQueryString() != null ) {
      log.warn( 
          "The @Deprecated SolrUpdateServlet does not accept query parameters: "+request.getQueryString()+"\n"
          +"  If you are using solrj, make sure to register a request handler to /update rather then use this servlet.\n"
          +"  Add: <requestHandler name=\"/update\" class=\"solr.XmlUpdateRequestHandler\" > to your solrconfig.xml\n\n" );
    }
    PrintWriter writer = response.getWriter();
    legacyUpdateHandler.doLegacyUpdate(requestReader, writer);
  }
}
