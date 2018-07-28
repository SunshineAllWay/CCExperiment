package org.apache.solr.handler;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrException;
import org.apache.solr.handler.extraction.ExtractingParams;
import org.apache.solr.handler.extraction.ExtractingRequestHandler;
import org.apache.solr.handler.extraction.ExtractingDocumentLoader;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
public class ExtractingRequestHandlerTest extends AbstractSolrTestCase {
  @Override
  public String getSchemaFile() {
    return "schema.xml";
  }
  @Override
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  public void testExtraction() throws Exception {
    ExtractingRequestHandler handler = (ExtractingRequestHandler) h.getCore().getRequestHandler("/update/extract");
    assertTrue("handler is null and it shouldn't be", handler != null);
    loadLocal("solr-word.pdf", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
            "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
            "fmap.Author", "extractedAuthor",
            "fmap.content", "extractedContent",
           "literal.id", "one",
            "fmap.Last-Modified", "extractedDate"
    );
    assertQ(req("title:solr-word"), "//*[@numFound='0']");
    assertU(commit());
    assertQ(req("title:solr-word"), "//*[@numFound='1']");
    loadLocal("simple.html", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
            "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
            "fmap.Author", "extractedAuthor",
            "fmap.language", "extractedLanguage",
            "literal.id", "two",
            "fmap.content", "extractedContent",
            "fmap.Last-Modified", "extractedDate"
    );
    assertQ(req("title:Welcome"), "//*[@numFound='0']");
    assertU(commit());
    assertQ(req("title:Welcome"), "//*[@numFound='1']");
    loadLocal("simple.html",
      "literal.id","simple2",
      "uprefix", "t_",
      "lowernames", "true",
      "captureAttr", "true",
      "fmap.a","t_href",
      "fmap.content_type", "abcxyz",  
      "commit", "true"  
    );
    assertQ(req("+id:simple2 +t_href:[* TO *]"), "//*[@numFound='1']");
    assertQ(req("+id:simple2 +t_abcxyz:[* TO *]"), "//*[@numFound='1']");
    loadLocal("simple.html",
      "literal.id","simple3",
      "uprefix", "t_",
      "lowernames", "true",
      "captureAttr", "true",  "fmap.a","t_href",
      "commit", "true"
      ,"boost.t_href", "100.0"
    );
    assertQ(req("t_href:http"), "//*[@numFound='2']");
    assertQ(req("t_href:http"), "//doc[1]/str[.='simple3']");
    assertQ(req("+id:simple3 +t_content_type:[* TO *]"), "//*[@numFound='1']");
     loadLocal("simple.html",
      "literal.id","simple4",
      "uprefix", "t_",
      "capture","p",     
      "commit", "true"
    );
    assertQ(req("+id:simple4 +t_content:Solr"), "//*[@numFound='1']");
    assertQ(req("+id:simple4 +t_p:\"here is some text\""), "//*[@numFound='1']");
    loadLocal("version_control.xml", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
            "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
            "fmap.Author", "extractedAuthor",
            "literal.id", "three",
            "fmap.content", "extractedContent",
            "fmap.language", "extractedLanguage",
            "fmap.Last-Modified", "extractedDate"
    );
    assertQ(req("stream_name:version_control.xml"), "//*[@numFound='0']");
    assertU(commit());
    assertQ(req("stream_name:version_control.xml"), "//*[@numFound='1']");
  }
  public void testDefaultField() throws Exception {
    ExtractingRequestHandler handler = (ExtractingRequestHandler) h.getCore().getRequestHandler("/update/extract");
    assertTrue("handler is null and it shouldn't be", handler != null);
    try {
      loadLocal("simple.html",
      "literal.id","simple2",
      "lowernames", "true",
        "captureAttr", "true",
        "commit", "true"  
      );
      assertTrue(false);
    } catch (SolrException e) {
    }
    loadLocal("simple.html",
      "literal.id","simple2",
      ExtractingParams.DEFAULT_FIELD, "defaultExtr",
      "lowernames", "true",
      "captureAttr", "true",
      "commit", "true"  
    );
    assertQ(req("id:simple2"), "//*[@numFound='1']");
    assertQ(req("defaultExtr:http\\://www.apache.org"), "//*[@numFound='1']");
    loadLocal("simple.html",
      "literal.id","simple2",
      ExtractingParams.DEFAULT_FIELD, "defaultExtr",
            ExtractingParams.UNKNOWN_FIELD_PREFIX, "t_",
      "lowernames", "true",
      "captureAttr", "true",
      "fmap.a","t_href",
      "commit", "true"  
    );
    assertQ(req("+id:simple2 +t_href:[* TO *]"), "//*[@numFound='1']");
  }
  public void testLiterals() throws Exception {
    ExtractingRequestHandler handler = (ExtractingRequestHandler) h.getCore().getRequestHandler("/update/extract");
    assertTrue("handler is null and it shouldn't be", handler != null);
    loadLocal("version_control.xml", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
            "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
            "fmap.Author", "extractedAuthor",
            "fmap.content", "extractedContent",
            "literal.id", "one",
            "fmap.language", "extractedLanguage",
            "literal.extractionLiteralMV", "one",
            "literal.extractionLiteralMV", "two",
            "fmap.Last-Modified", "extractedDate"
    );
    assertQ(req("stream_name:version_control.xml"), "//*[@numFound='0']");
    assertU(commit());
    assertQ(req("stream_name:version_control.xml"), "//*[@numFound='1']");
    assertQ(req("extractionLiteralMV:one"), "//*[@numFound='1']");
    assertQ(req("extractionLiteralMV:two"), "//*[@numFound='1']");
    try {
      loadLocal("version_control.xml", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
              "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
              "fmap.Author", "extractedAuthor",
              "fmap.content", "extractedContent",
              "literal.id", "two",
              "fmap.language", "extractedLanguage",
              "literal.extractionLiteral", "one",
              "literal.extractionLiteral", "two",
              "fmap.Last-Modified", "extractedDate"
      );
    } catch (SolrException e) {
    }
    loadLocal("version_control.xml", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
            "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
            "fmap.Author", "extractedAuthor",
            "fmap.content", "extractedContent",
            "literal.id", "three",
            "fmap.language", "extractedLanguage",
            "literal.extractionLiteral", "one",
            "fmap.Last-Modified", "extractedDate"
    );
    assertU(commit());
    assertQ(req("extractionLiteral:one"), "//*[@numFound='1']");
  }
  public void testPlainTextSpecifyingMimeType() throws Exception {
    ExtractingRequestHandler handler = (ExtractingRequestHandler) h.getCore().getRequestHandler("/update/extract");
    assertTrue("handler is null and it shouldn't be", handler != null);
    loadLocal("version_control.txt", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
            "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
            "fmap.Author", "extractedAuthor",
            "literal.id", "one",
            "fmap.language", "extractedLanguage",
            "fmap.content", "extractedContent",
            ExtractingParams.STREAM_TYPE, "text/plain"
    );
    assertQ(req("extractedContent:Apache"), "//*[@numFound='0']");
    assertU(commit());
    assertQ(req("extractedContent:Apache"), "//*[@numFound='1']");
  }
  public void testPlainTextSpecifyingResourceName() throws Exception {
    ExtractingRequestHandler handler = (ExtractingRequestHandler) h.getCore().getRequestHandler("/update/extract");
    assertTrue("handler is null and it shouldn't be", handler != null);
    loadLocal("version_control.txt", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
            "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
            "fmap.Author", "extractedAuthor",
            "literal.id", "one",
            "fmap.language", "extractedLanguage",
            "fmap.content", "extractedContent",
            ExtractingParams.RESOURCE_NAME, "version_control.txt"
    );
    assertQ(req("extractedContent:Apache"), "//*[@numFound='0']");
    assertU(commit());
    assertQ(req("extractedContent:Apache"), "//*[@numFound='1']");
  }
  public void testExtractOnly() throws Exception {
    ExtractingRequestHandler handler = (ExtractingRequestHandler) h.getCore().getRequestHandler("/update/extract");
    assertTrue("handler is null and it shouldn't be", handler != null);
    SolrQueryResponse rsp = loadLocal("solr-word.pdf", ExtractingParams.EXTRACT_ONLY, "true");
    assertTrue("rsp is null and it shouldn't be", rsp != null);
    NamedList list = rsp.getValues();
    String extraction = (String) list.get("solr-word.pdf");
    assertTrue("extraction is null and it shouldn't be", extraction != null);
    assertTrue(extraction + " does not contain " + "solr-word", extraction.indexOf("solr-word") != -1);
    NamedList nl = (NamedList) list.get("solr-word.pdf_metadata");
    assertTrue("nl is null and it shouldn't be", nl != null);
    Object title = nl.get("title");
    assertTrue("title is null and it shouldn't be", title != null);
    assertTrue(extraction.indexOf("<?xml") != -1);
    rsp = loadLocal("solr-word.pdf", ExtractingParams.EXTRACT_ONLY, "true",
            ExtractingParams.EXTRACT_FORMAT, ExtractingDocumentLoader.TEXT_FORMAT);
    assertTrue("rsp is null and it shouldn't be", rsp != null);
    list = rsp.getValues();
    extraction = (String) list.get("solr-word.pdf");
    assertTrue("extraction is null and it shouldn't be", extraction != null);
    assertTrue(extraction + " does not contain " + "solr-word", extraction.indexOf("solr-word") != -1);
    assertTrue(extraction.indexOf("<?xml") == -1);
    nl = (NamedList) list.get("solr-word.pdf_metadata");
    assertTrue("nl is null and it shouldn't be", nl != null);
    title = nl.get("title");
    assertTrue("title is null and it shouldn't be", title != null);
  }
  public void testXPath() throws Exception {
    ExtractingRequestHandler handler = (ExtractingRequestHandler) h.getCore().getRequestHandler("/update/extract");
    assertTrue("handler is null and it shouldn't be", handler != null);
    SolrQueryResponse rsp = loadLocal("example.html",
            ExtractingParams.XPATH_EXPRESSION, "/xhtml:html/xhtml:body/xhtml:a/descendant:node()",
            ExtractingParams.EXTRACT_ONLY, "true"
    );
    assertTrue("rsp is null and it shouldn't be", rsp != null);
    NamedList list = rsp.getValues();
    String val = (String) list.get("example.html");
    val = val.trim();
    assertTrue(val + " is not equal to " + "linkNews", val.equals("linkNews") == true);
  }
  public void testArabicPDF() throws Exception {
    ExtractingRequestHandler handler = (ExtractingRequestHandler) 
      h.getCore().getRequestHandler("/update/extract");
    assertTrue("handler is null and it shouldn't be", handler != null);
    loadLocal("arabic.pdf", "fmap.created", "extractedDate", "fmap.producer", "extractedProducer",
        "fmap.creator", "extractedCreator", "fmap.Keywords", "extractedKeywords",
        "fmap.Author", "extractedAuthor",
        "fmap.content", "wdf_nocase",
       "literal.id", "one",
        "fmap.Last-Modified", "extractedDate");
    assertQ(req("wdf_nocase:السلم"), "//result[@numFound=0]");
    assertU(commit());
    assertQ(req("wdf_nocase:السلم"), "//result[@numFound=1]");
  }
  SolrQueryResponse loadLocal(String filename, String... args) throws Exception {
    LocalSolrQueryRequest req = (LocalSolrQueryRequest) req(args);
    List<ContentStream> cs = new ArrayList<ContentStream>();
    cs.add(new ContentStreamBase.FileStream(new File(filename)));
    req.setContentStreams(cs);
    return h.queryAndResponse("/update/extract", req);
  }
}
