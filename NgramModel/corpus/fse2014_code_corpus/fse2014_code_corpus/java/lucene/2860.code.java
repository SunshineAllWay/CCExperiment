package org.apache.solr.servlet;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.solr.client.solrj.SolrExampleTestBase;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
public abstract class CacheHeaderTestBase extends SolrExampleTestBase {
  @Override public String getSolrHome() {  return "solr/"; }
  abstract public String getSolrConfigFilename();
  public String getSolrConfigFile() { return getSolrHome()+"conf/"+getSolrConfigFilename(); }
  CommonsHttpSolrServer server;
  JettySolrRunner jetty;
  int port = 0;
  static final String context = "/example";
  @Override
  public void setUp() throws Exception {
    super.setUp();
    jetty = new JettySolrRunner(context, 0, getSolrConfigFilename());
    jetty.start();
    port = jetty.getLocalPort();
    server = this.createNewSolrServer();
  }
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    jetty.stop(); 
  }
  @Override
  protected SolrServer getSolrServer() {
    return server;
  }
  @Override
  protected CommonsHttpSolrServer createNewSolrServer() {
    try {
      String url = "http://localhost:" + port + context;
      CommonsHttpSolrServer s = new CommonsHttpSolrServer(url);
      s.setConnectionTimeout(100); 
      s.setDefaultMaxConnectionsPerHost(100);
      s.setMaxTotalConnections(100);
      return s;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  protected HttpMethodBase getSelectMethod(String method) {
    HttpMethodBase m = null;
    if ("GET".equals(method)) {
      m = new GetMethod(server.getBaseURL() + "/select");
    } else if ("HEAD".equals(method)) {
      m = new HeadMethod(server.getBaseURL() + "/select");
    } else if ("POST".equals(method)) {
      m = new PostMethod(server.getBaseURL() + "/select");
    }
    m.setQueryString(new NameValuePair[] { new NameValuePair("q", "solr"),
          new NameValuePair("qt", "standard") });
    return m;
  }
  protected HttpMethodBase getUpdateMethod(String method) {
    HttpMethodBase m = null;
    if ("GET".equals(method)) {
      m=new GetMethod(server.getBaseURL()+"/update/csv");
    } else if ("POST".equals(method)) {
      m=new PostMethod(server.getBaseURL()+"/update/csv");      
    } else if ("HEAD".equals(method)) {
      m=new HeadMethod(server.getBaseURL()+"/update/csv");      
    }
    return m;
  }
  protected HttpClient getClient() {
    return server.getHttpClient();
  }
  protected void checkResponseBody(String method, HttpMethodBase resp)
      throws Exception {
    String responseBody = resp.getResponseBodyAsString();
    if ("GET".equals(method)) {
      switch (resp.getStatusCode()) {
        case 200:
          assertTrue("Response body was empty for method " + method,
              responseBody != null && responseBody.length() > 0);
          break;
        case 304:
          assertTrue("Response body was not empty for method " + method,
              responseBody == null || responseBody.length() == 0);
          break;
        case 412:
          assertTrue("Response body was not empty for method " + method,
              responseBody == null || responseBody.length() == 0);
          break;
        default:
          System.err.println(responseBody);
          assertEquals("Unknown request response", 0, resp.getStatusCode());
      }
    }
    if ("HEAD".equals(method)) {
      assertTrue("Response body was not empty for method " + method,
          responseBody == null || responseBody.length() == 0);
    }
  }
  public void testLastModified() throws Exception {
    doLastModified("GET");
    doLastModified("HEAD");
  }
  public void testEtag() throws Exception {
    doETag("GET");
    doETag("HEAD");
  }
  public void testCacheControl() throws Exception {
    doCacheControl("GET");
    doCacheControl("HEAD");
    doCacheControl("POST");
  }
  protected abstract void doCacheControl(String method) throws Exception;
  protected abstract void doETag(String method) throws Exception;
  protected abstract void doLastModified(String method) throws Exception;
}
