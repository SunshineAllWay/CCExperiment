package org.apache.solr.util;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.XML;
import org.apache.solr.request.*;
import org.apache.solr.util.TestHarness;
import org.xml.sax.SAXException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import junit.framework.TestCase;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
public abstract class AbstractSolrTestCase extends TestCase {
    protected SolrConfig solrConfig;
  protected TestHarness h;
  protected TestHarness.LocalRequestFactory lrf;
  public abstract String getSchemaFile();
  public abstract String getSolrConfigFile();
  protected File dataDir;
  public static Logger log = LoggerFactory.getLogger(AbstractSolrTestCase.class);
  public void setUp() throws Exception {
    log.info("####SETUP_START " + getName());
    dataDir = new File(System.getProperty("java.io.tmpdir")
            + System.getProperty("file.separator")
            + getClass().getName() + "-" + System.currentTimeMillis());
    dataDir.mkdirs();
    String configFile = getSolrConfigFile();
    if (configFile != null) {
      solrConfig = h.createConfig(getSolrConfigFile());
      h = new TestHarness( dataDir.getAbsolutePath(),
              solrConfig,
              getSchemaFile());
      lrf = h.getRequestFactory
              ("standard",0,20,"version","2.2");
    }
    log.info("####SETUP_END " + getName());
  }
  public void postSetUp() {
    log.info("####POSTSETUP " + getName());
  }
  public void preTearDown() {
    log.info("####PRETEARDOWN " + getName());      
  }
  public void tearDown() throws Exception {
    log.info("####TEARDOWN_START " + getName());
    if (h != null) { h.close(); }
    String skip = System.getProperty("solr.test.leavedatadir");
    if (null != skip && 0 != skip.trim().length()) {
      System.err.println("NOTE: per solr.test.leavedatadir, dataDir will not be removed: " + dataDir.getAbsolutePath());
    } else {
      if (!recurseDelete(dataDir)) {
        System.err.println("!!!! WARNING: best effort to remove " + dataDir.getAbsolutePath() + " FAILED !!!!!");
      }
    }
  }
  public void assertU(String update) {
    assertU(null, update);
  }
  public void assertU(String message, String update) {
    checkUpdateU(message, update, true);
  }
  public void assertFailedU(String update) {
    assertFailedU(null, update);
  }
  public void assertFailedU(String message, String update) {
    checkUpdateU(message, update, false);
  }
  private void checkUpdateU(String message, String update, boolean shouldSucceed) {
    try {
      String m = (null == message) ? "" : message + " ";
      if (shouldSucceed) {
           String res = h.validateUpdate(update);
         if (res != null) fail(m + "update was not successful: " + res);
      } else {
           String res = h.validateErrorUpdate(update);
         if (res != null) fail(m + "update succeeded, but should have failed: " + res);        
      }
    } catch (SAXException e) {
      throw new RuntimeException("Invalid XML", e);
    }
  }
  public void assertQ(SolrQueryRequest req, String... tests) {
    assertQ(null, req, tests);
  }
  public void assertQ(String message, SolrQueryRequest req, String... tests) {
    try {
      String m = (null == message) ? "" : message + " ";
      String response = h.query(req);
      String results = h.validateXPath(response, tests);
      if (null != results) {
        fail(m + "query failed XPath: " + results +
             "\n xml response was: " + response +
             "\n request was: " + req.getParamString());
      }
    } catch (XPathExpressionException e1) {
      throw new RuntimeException("XPath is invalid", e1);
    } catch (Exception e2) {
      throw new RuntimeException("Exception during query", e2);
    }
  }
  public void assertQEx(String message, SolrQueryRequest req, int code ) {
    try {
      h.query(req);
      fail( message );
    } catch (SolrException sex) {
      assertEquals( code, sex.code() );
    } catch (Exception e2) {
      throw new RuntimeException("Exception during query", e2);
    }
  }
  public void assertQEx(String message, SolrQueryRequest req, SolrException.ErrorCode code ) {
    try {
      h.query(req);
      fail( message );
    } catch (SolrException e) {
      assertEquals( code.code, e.code() );
    } catch (Exception e2) {
      throw new RuntimeException("Exception during query", e2);
    }
  }
  public String optimize(String... args) {
    return h.optimize(args);
  }
  public String commit(String... args) {
    return h.commit(args);
  }
  public String adoc(String... fieldsAndValues) {
    Doc d = doc(fieldsAndValues);
    return add(d);
  }
  public String adoc(SolrInputDocument sdoc) {
    List<String> fields = new ArrayList<String>();
    for (SolrInputField sf : sdoc) {
      for (Object o : sf.getValues()) {
        fields.add(sf.getName());
        fields.add(o.toString());
      }
    }
    return adoc(fields.toArray(new String[fields.size()]));
  }
  public String add(Doc doc, String... args) {
    try {
      StringWriter r = new StringWriter();
      if (null == args || 0 == args.length) {
        r.write("<add>");
        r.write(doc.xml);
        r.write("</add>");
      } else {
        XML.writeUnescapedXML(r, "add", doc.xml, (Object[])args);
      }
      return r.getBuffer().toString();
    } catch (IOException e) {
      throw new RuntimeException
        ("this should never happen with a StringWriter", e);
    }
  }
  public String delI(String id) {
    return h.deleteById(id);
  }
  public String delQ(String q) {
    return h.deleteByQuery(q);
  }
  public Doc doc(String... fieldsAndValues) {
    Doc d = new Doc();
    d.xml = h.makeSimpleDoc(fieldsAndValues).toString();
    return d;
  }
  public SolrQueryRequest req(String... q) {
    return lrf.makeRequest(q);
  }
  public SolrQueryRequest req(String[] params, String... moreParams) {
    String[] allParams = moreParams;
    if (params.length!=0) {
      int len = params.length + moreParams.length;
      allParams = new String[len];
      System.arraycopy(params,0,allParams,0,params.length);
      System.arraycopy(moreParams,0,allParams,params.length,moreParams.length);
    }
    return lrf.makeRequest(allParams);
  }
  public static class Doc {
    public String xml;
    public String toString() { return xml; }
  }
  public static boolean recurseDelete(File f) {
    if (f.isDirectory()) {
      for (File sub : f.listFiles()) {
        if (!recurseDelete(sub)) {
          return false;
        }
      }
    }
    return f.delete();
  }
}
