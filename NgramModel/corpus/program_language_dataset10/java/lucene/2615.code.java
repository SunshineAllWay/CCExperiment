package org.apache.solr.util;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.XML;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.XmlUpdateRequestHandler;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.apache.solr.common.util.NamedList.NamedListEntry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TestHarness {
  protected CoreContainer container;
  private SolrCore core;
  private XPath xpath = XPathFactory.newInstance().newXPath();
  private DocumentBuilder builder;
  public XmlUpdateRequestHandler updater;
  public static SolrConfig createConfig(String confFile) {
      System.setProperty("solr.test.sys.prop1", "propone");
      System.setProperty("solr.test.sys.prop2", "proptwo");
      try {
      return new SolrConfig(confFile);
      }
      catch(Exception xany) {
        throw new RuntimeException(xany);
      }
  }
  public TestHarness( String dataDirectory) {
    this( dataDirectory, "schema.xml");
  }
  public TestHarness( String dataDirectory, String schemaFile) {
    this( dataDirectory, "solrconfig.xml", schemaFile);
  }
   public TestHarness( String dataDirectory, String configFile, String schemaFile) {
     this( dataDirectory, createConfig(configFile), schemaFile);
   }
      public TestHarness( String dataDirectory,
                          SolrConfig solrConfig,
                          String schemaFile) {
     this( dataDirectory, solrConfig, new IndexSchema(solrConfig, schemaFile, null));
   }
  public TestHarness( String dataDirectory,
                      SolrConfig solrConfig,
                      IndexSchema indexSchema) {
      this("", new Initializer("", dataDirectory, solrConfig, indexSchema));
  }
  public TestHarness(String coreName, CoreContainer.Initializer init) {
    try {
      container = init.initialize();
      if (coreName == null)
        coreName = "";
      core = container.getCore(coreName);
      if (core != null)
        core.close();
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      updater = new XmlUpdateRequestHandler();
      updater.init( null );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  static class Initializer extends CoreContainer.Initializer {
    String coreName;
    String dataDirectory;
    SolrConfig solrConfig;
    IndexSchema indexSchema;
    public Initializer(String coreName,
                      String dataDirectory,
                      SolrConfig solrConfig,
                      IndexSchema indexSchema) {
      if (coreName == null)
        coreName = "";
      this.coreName = coreName;
      this.dataDirectory = dataDirectory;
      this.solrConfig = solrConfig;
      this.indexSchema = indexSchema;
    }
    public String getCoreName() {
      return coreName;
    }
    @Override
    public CoreContainer initialize() {
      CoreContainer container = new CoreContainer(new SolrResourceLoader(SolrResourceLoader.locateSolrHome()));
      CoreDescriptor dcore = new CoreDescriptor(container, coreName, solrConfig.getResourceLoader().getInstanceDir());
      dcore.setConfigName(solrConfig.getResourceName());
      dcore.setSchemaName(indexSchema.getResourceName());
      SolrCore core = new SolrCore( null, dataDirectory, solrConfig, indexSchema, dcore);
      container.register(coreName, core, false);
      return container;
    }
  }
  public CoreContainer getCoreContainer() {
    return container;
  }
  public SolrCore getCore() {
    return core;
  }
  @Deprecated
  public String update(String xml) {
    StringReader req = new StringReader(xml);
    StringWriter writer = new StringWriter(32000);
    updater.doLegacyUpdate(req, writer);
    return writer.toString();
  }
  public String validateUpdate(String xml) throws SAXException {
    return checkUpdateStatus(xml, "0");
  }
  public String validateErrorUpdate(String xml) throws SAXException {
    return checkUpdateStatus(xml, "1");
  }
  public String checkUpdateStatus(String xml, String code) throws SAXException {
    try {
      String res = update(xml);
      String valid = validateXPath(res, "//result[@status="+code+"]" );
      return (null == valid) ? null : res;
    } catch (XPathExpressionException e) {
      throw new RuntimeException
        ("?!? static xpath has bug?", e);
    }
  }
  public String validateAddDoc(String... fieldsAndValues)
    throws XPathExpressionException, SAXException, IOException {
    StringBuilder buf = new StringBuilder();
    buf.append("<add>");
    appendSimpleDoc(buf, fieldsAndValues);
    buf.append("</add>");
    String res = update(buf.toString());
    String valid = validateXPath(res, "//result[@status=0]" );
    return (null == valid) ? null : res;
  }
  public String validateQuery(SolrQueryRequest req, String... tests)
    throws IOException, Exception {
    String res = query(req);
    return validateXPath(res, tests);
  }
  public String query(SolrQueryRequest req) throws IOException, Exception {
    return query(req.getParams().get(CommonParams.QT), req);
  }
  public String query(String handler, SolrQueryRequest req) throws IOException, Exception {
    SolrQueryResponse rsp = queryAndResponse(handler, req);
    StringWriter sw = new StringWriter(32000);
    QueryResponseWriter responseWriter = core.getQueryResponseWriter(req);
    responseWriter.write(sw,req,rsp);
    req.close();
    return sw.toString();
  }
  public SolrQueryResponse queryAndResponse(String handler, SolrQueryRequest req) throws Exception {
    SolrQueryResponse rsp = new SolrQueryResponse();
    core.execute(core.getRequestHandler(handler),req,rsp);
    if (rsp.getException() != null) {
      throw rsp.getException();
    }
    return rsp;
  }
  public String validateXPath(String xml, String... tests)
    throws XPathExpressionException, SAXException {
    if (tests==null || tests.length == 0) return null;
    Document document=null;
    try {
      document = builder.parse(new ByteArrayInputStream
                               (xml.getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e1) {
      throw new RuntimeException("Totally weird UTF-8 exception", e1);
    } catch (IOException e2) {
      throw new RuntimeException("Totally weird io exception", e2);
    }
    for (String xp : tests) {
      xp=xp.trim();
      Boolean bool = (Boolean) xpath.evaluate(xp, document,
                                              XPathConstants.BOOLEAN);
      if (!bool) {
        return xp;
      }
    }
    return null;
  }
  public void close() {
    if (container != null) {
      for (SolrCore c : container.getCores()) {
        if (c.getOpenCount() > 1)
          throw new RuntimeException("SolrCore.getOpenCount()=="+core.getOpenCount());
      }      
    }
    if (container != null) {
      container.shutdown();
      container = null;
    }
  }
  public void appendSimpleDoc(StringBuilder buf, String... fieldsAndValues)
    throws IOException {
    buf.append(makeSimpleDoc(fieldsAndValues));
  }
  public void appendSimpleDoc(StringBuffer buf, String... fieldsAndValues)
    throws IOException {
    buf.append(makeSimpleDoc(fieldsAndValues));
  }
  public static StringBuffer makeSimpleDoc(String... fieldsAndValues) {
    try {
      StringWriter w = new StringWriter();
      w.append("<doc>");
      for (int i = 0; i < fieldsAndValues.length; i+=2) {
        XML.writeXML(w, "field", fieldsAndValues[i+1], "name",
                     fieldsAndValues[i]);
      }
      w.append("</doc>");
      return w.getBuffer();
    } catch (IOException e) {
      throw new RuntimeException
        ("this should never happen with a StringWriter", e);
    }
  }
  public static String deleteByQuery(String q) {
    return delete("query", q);
  }
  public static String deleteById(String id) {
    return delete("id", id);
  }
  private static String delete(String deltype, String val) {
    try {
      StringWriter r = new StringWriter();
      r.write("<delete>");
      XML.writeXML(r, deltype, val);
      r.write("</delete>");
      return r.getBuffer().toString();
    } catch (IOException e) {
      throw new RuntimeException
        ("this should never happen with a StringWriter", e);
    }
  }
  public static String optimize(String... args) {
    return simpleTag("optimize", args);
  }
  private static String simpleTag(String tag, String... args) {
    try {
      StringWriter r = new StringWriter();
      if (null == args || 0 == args.length) {
        XML.writeXML(r, tag, null);
      } else {
        XML.writeXML(r, tag, null, (Object[])args);
      }
      return r.getBuffer().toString();
    } catch (IOException e) {
      throw new RuntimeException
        ("this should never happen with a StringWriter", e);
    }
  }
  public static String commit(String... args) {
    return simpleTag("commit", args);
  }
  public LocalRequestFactory getRequestFactory(String qtype,
                                               int start,
                                               int limit) {
    LocalRequestFactory f = new LocalRequestFactory();
    f.qtype = qtype;
    f.start = start;
    f.limit = limit;
    return f;
  }
  public LocalRequestFactory getRequestFactory(String qtype,
                                               int start, int limit,
                                               String... args) {
    LocalRequestFactory f = getRequestFactory(qtype, start, limit);
    for (int i = 0; i < args.length; i+=2) {
      f.args.put(args[i], args[i+1]);
    }
    return f;
  }
  public LocalRequestFactory getRequestFactory(String qtype,
                                               int start, int limit,
                                               Map<String,String> args) {
    LocalRequestFactory f = getRequestFactory(qtype, start, limit);
    f.args.putAll(args);
    return f;
  }
  public class LocalRequestFactory {
    public String qtype = "standard";
    public int start = 0;
    public int limit = 1000;
    public Map<String,String> args = new HashMap<String,String>();
    public LocalRequestFactory() {
    }
    public LocalSolrQueryRequest makeRequest(String ... q) {
      if (q.length==1) {
        return new LocalSolrQueryRequest(TestHarness.this.getCore(),
                                       q[0], qtype, start, limit, args);
      }
      if (q.length%2 != 0) { 
        throw new RuntimeException("The length of the string array (query arguments) needs to be even");
      }
      Map.Entry<String, String> [] entries = new NamedListEntry[q.length / 2];
      for (int i = 0; i < q.length; i += 2) {
        entries[i/2] = new NamedListEntry<String>(q[i], q[i+1]);
      }
      return new LocalSolrQueryRequest(TestHarness.this.getCore(), new NamedList(entries));
    }
  }
}
