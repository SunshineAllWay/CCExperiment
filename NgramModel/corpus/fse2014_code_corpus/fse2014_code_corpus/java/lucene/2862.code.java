package org.apache.solr.servlet;
import java.util.Date;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.util.DateUtil;
public class NoCacheHeaderTest extends CacheHeaderTestBase {
  @Override public String getSolrConfigFilename() { return "solrconfig-nocache.xml";  }
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
  protected void doLastModified(String method) throws Exception {
    HttpMethodBase get = getSelectMethod(method);
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("Got no response code 200 in initial request", 200, get
        .getStatusCode());
    Header head = get.getResponseHeader("Last-Modified");
    assertNull("We got a Last-Modified header", head);
    get = getSelectMethod(method);
    get.addRequestHeader("If-Modified-Since", DateUtil.formatDate(new Date()));
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("Expected 200 with If-Modified-Since header. We should never get a 304 here", 200,
        get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Modified-Since", DateUtil.formatDate(new Date(System.currentTimeMillis()-10000)));
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("Expected 200 with If-Modified-Since header. We should never get a 304 here",
        200, get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Unmodified-Since", DateUtil.formatDate(new Date(System.currentTimeMillis()-10000)));
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals(
        "Expected 200 with If-Unmodified-Since header. We should never get a 304 here",
        200, get.getStatusCode());
    get = getSelectMethod(method);
    get
        .addRequestHeader("If-Unmodified-Since", DateUtil
            .formatDate(new Date()));
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals(
        "Expected 200 with If-Unmodified-Since header. We should never get a 304 here",
        200, get.getStatusCode());
  }
  protected void doETag(String method) throws Exception {
    HttpMethodBase get = getSelectMethod(method);
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("Got no response code 200 in initial request", 200, get
        .getStatusCode());
    Header head = get.getResponseHeader("ETag");
    assertNull("We got an ETag in the response", head);
    get = getSelectMethod(method);
    get.addRequestHeader("If-None-Match", "\"xyz123456\"");
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals(
        "If-None-Match: Got no response code 200 in response to non matching ETag",
        200, get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-None-Match", "*");
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("If-None-Match: Got no response 200 for star ETag", 200, get
        .getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Match", "\"xyz123456\"");
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals(
        "If-Match: Got no response code 200 in response to non matching ETag",
        200, get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Match", "*");
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("If-Match: Got no response 200 to star ETag", 200, get
        .getStatusCode());
  }
  protected void doCacheControl(String method) throws Exception {
      HttpMethodBase m = getSelectMethod(method);
      getClient().executeMethod(m);
      checkResponseBody(method, m);
      Header head = m.getResponseHeader("Cache-Control");
      assertNull("We got a cache-control header in response", head);
      head = m.getResponseHeader("Expires");
      assertNull("We got an Expires header in response", head);
  }
}