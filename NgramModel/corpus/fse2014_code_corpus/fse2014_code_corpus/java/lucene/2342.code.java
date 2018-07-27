package org.apache.solr.handler;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.XML;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryRequestBase;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
public class XmlUpdateRequestHandler extends ContentStreamHandlerBase {
  public static Logger log = LoggerFactory.getLogger(XmlUpdateRequestHandler.class);
  public static final String UPDATE_PROCESSOR = "update.processor";
  public static final String ADD = "add";
  public static final String DELETE = "delete";
  public static final String OPTIMIZE = "optimize";
  public static final String COMMIT = "commit";
  public static final String ROLLBACK = "rollback";
  public static final String WAIT_SEARCHER = "waitSearcher";
  public static final String WAIT_FLUSH = "waitFlush";
  public static final String OVERWRITE = "overwrite";
  public static final String COMMIT_WITHIN = "commitWithin";
  public static final String OVERWRITE_COMMITTED = "overwriteCommitted";
  public static final String OVERWRITE_PENDING = "overwritePending";
  public static final String ALLOW_DUPS = "allowDups";
  XMLInputFactory inputFactory;
  @Override
  public void init(NamedList args) {
    super.init(args);
    inputFactory = XMLInputFactory.newInstance();
    try {
      inputFactory.setProperty("reuse-instance", Boolean.FALSE);
    }
    catch (IllegalArgumentException ex) {
      log.debug("Unable to set the 'reuse-instance' property for the input chain: " + inputFactory);
    }
  }
  protected ContentStreamLoader newLoader(SolrQueryRequest req, UpdateRequestProcessor processor) {
    return new XMLLoader(processor, inputFactory);
  }
  @Deprecated
  public void doLegacyUpdate(Reader input, Writer output) {
    try {
      SolrCore core = SolrCore.getSolrCore();
      UpdateRequestProcessorChain processorFactory = core.getUpdateProcessingChain(null);
      SolrParams params = new MapSolrParams(new HashMap<String, String>());
      SolrQueryRequestBase req = new SolrQueryRequestBase(core, params) {
      };
      SolrQueryResponse rsp = new SolrQueryResponse(); 
      XMLStreamReader parser = inputFactory.createXMLStreamReader(input);
      UpdateRequestProcessor processor = processorFactory.createProcessor(req, rsp);
      XMLLoader loader = (XMLLoader) newLoader(req, processor);
      loader.processUpdate(processor, parser);
      processor.finish();
      output.write("<result status=\"0\"></result>");
    }
    catch (Exception ex) {
      try {
        SolrException.logOnce(log, "Error processing \"legacy\" update command", ex);
        XML.writeXML(output, "result", SolrException.toStr(ex), "status", "1");
      } catch (Exception ee) {
        log.error("Error writing to output stream: " + ee);
      }
    }
  }
  @Override
  public String getDescription() {
    return "Add documents with XML";
  }
  @Override
  public String getVersion() {
    return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: XmlUpdateRequestHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/XmlUpdateRequestHandler.java $";
  }
}
