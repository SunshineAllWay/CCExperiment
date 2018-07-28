package org.apache.solr.client.solrj.embedded;
import java.net.URL;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
public class JettyWebappTest extends TestCase 
{
  int port = 0;
  static final String context = "/test";
  Server server;
  @Override
  public void setUp() throws Exception 
  {
    System.setProperty("solr.solr.home", "../../../example/solr");
    System.setProperty("solr.data.dir", "./solr/data");
    String path = "../../webapp/web";
    server = new Server(port);
    new WebAppContext(server, path, context );
    SocketConnector connector = new SocketConnector();
    connector.setMaxIdleTime(1000 * 60 * 60);
    connector.setSoLingerTime(-1);
    connector.setPort(0);
    server.setConnectors(new Connector[]{connector});
    server.setStopAtShutdown( true );
    server.start();
    port = connector.getLocalPort();
  }
  @Override
  public void tearDown() throws Exception 
  {
    try {
      server.stop();
    } catch( Exception ex ) {}
  }
  public void testJSP() throws Exception
  {
    String adminPath = "http://localhost:"+port+context+"/";
    String html = IOUtils.toString( new URL(adminPath).openStream() );
    assertNotNull( html ); 
    adminPath += "admin/";
    html = IOUtils.toString( new URL(adminPath).openStream() );
    assertNotNull( html ); 
    html = IOUtils.toString( new URL(adminPath+"analysis.jsp").openStream() );
    assertNotNull( html ); 
    html = IOUtils.toString( new URL(adminPath+"schema.jsp").openStream() );
    assertNotNull( html ); 
    html = IOUtils.toString( new URL(adminPath+"threaddump.jsp").openStream() );
    assertNotNull( html ); 
  }
}
