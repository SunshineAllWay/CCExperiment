package org.apache.solr;
import org.apache.lucene.util.LuceneTestCaseJ4;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.XML;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.TestHarness;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
public class SolrTestCaseJ4 extends LuceneTestCaseJ4 {
  @BeforeClass
  public static void beforeClass() throws Exception {
  }
  @AfterClass
  public static void afterClass() throws Exception {
    deleteCore();
  }
  @Override
  public void setUp() throws Exception {
    super.setUp();
    log.info("###Starting " + getName());  
  }
  @Override
  public void tearDown() throws Exception {
    log.info("###Ending " + getName());    
    super.tearDown();
  }
  public static void initCore(String config, String schema) throws Exception {
    configString = config;
    schemaString = schema;
    initCore();
  }
  protected static String configString;
  protected static String schemaString;
  protected static SolrConfig solrConfig;
  protected static TestHarness h;
  protected static TestHarness.LocalRequestFactory lrf;
  public static  String getSchemaFile() {
    return schemaString;
  };
  public static  String getSolrConfigFile() {
    return configString;
  };
  protected static File dataDir;
  public static Logger log = LoggerFactory.getLogger(SolrTestCaseJ4.class);
  public static void initCore() throws Exception {
    log.info("####initCore");
    dataDir = new File(System.getProperty("java.io.tmpdir")
            + System.getProperty("file.separator")
            + System.currentTimeMillis());
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
    log.info("####initCore end");
  }
  public void postSetUp() {
    log.info("####POSTSETUP " + getName());
  }
  public void preTearDown() {
    log.info("####PRETEARDOWN " + getName());
  }
  public static void deleteCore() throws Exception {
    log.info("###deleteCore" );
    if (h != null) { h.close(); }
    if (dataDir != null) {
      String skip = System.getProperty("solr.test.leavedatadir");
      if (null != skip && 0 != skip.trim().length()) {
        System.err.println("NOTE: per solr.test.leavedatadir, dataDir will not be removed: " + dataDir.getAbsolutePath());
      } else {
        if (!recurseDelete(dataDir)) {
          System.err.println("!!!! WARNING: best effort to remove " + dataDir.getAbsolutePath() + " FAILED !!!!!");
        }
      }
    }
    dataDir = null;
    solrConfig = null;
    h = null;
    lrf = null;
    configString = schemaString = null;    
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
  public static void assertQEx(String message, SolrQueryRequest req, int code ) {
    try {
      h.query(req);
      fail( message );
    } catch (SolrException sex) {
      assertEquals( code, sex.code() );
    } catch (Exception e2) {
      throw new RuntimeException("Exception during query", e2);
    }
  }
  public static void assertQEx(String message, SolrQueryRequest req, SolrException.ErrorCode code ) {
    try {
      h.query(req);
      fail( message );
    } catch (SolrException e) {
      assertEquals( code.code, e.code() );
    } catch (Exception e2) {
      throw new RuntimeException("Exception during query", e2);
    }
  }
  public static String optimize(String... args) {
    return h.optimize(args);
  }
  public static String commit(String... args) {
    return h.commit(args);
  }
  public static String adoc(String... fieldsAndValues) {
    Doc d = doc(fieldsAndValues);
    return add(d);
  }
  public static String adoc(SolrInputDocument sdoc) {
    List<String> fields = new ArrayList<String>();
    for (SolrInputField sf : sdoc) {
      for (Object o : sf.getValues()) {
        fields.add(sf.getName());
        fields.add(o.toString());
      }
    }
    return adoc(fields.toArray(new String[fields.size()]));
  }
  public static String add(Doc doc, String... args) {
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
  public static String delI(String id) {
    return h.deleteById(id);
  }
  public static String delQ(String q) {
    return h.deleteByQuery(q);
  }
  public static Doc doc(String... fieldsAndValues) {
    Doc d = new Doc();
    d.xml = h.makeSimpleDoc(fieldsAndValues).toString();
    return d;
  }
  public static SolrQueryRequest req(String... q) {
    return lrf.makeRequest(q);
  }
  public static SolrQueryRequest req(String[] params, String... moreParams) {
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
  public void clearIndex() {
    assertU(delQ("*:*"));
  }
}
