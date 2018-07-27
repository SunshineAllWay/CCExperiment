package org.apache.solr.handler;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.solr.client.solrj.request.DocumentAnalysisRequest;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.AnalysisParams;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
public class DocumentAnalysisRequestHandler extends AnalysisRequestHandlerBase {
  public static final Logger log = LoggerFactory.getLogger(DocumentAnalysisRequestHandler.class);
  private static final float DEFAULT_BOOST = 1.0f;
  private XMLInputFactory inputFactory;
  @Override
  public void init(NamedList args) {
    super.init(args);
    inputFactory = XMLInputFactory.newInstance();
    try {
      inputFactory.setProperty("reuse-instance", Boolean.FALSE);
    } catch (IllegalArgumentException ex) {
      log.debug("Unable to set the 'reuse-instance' property for the input factory: " + inputFactory);
    }
  }
  protected NamedList doAnalysis(SolrQueryRequest req) throws Exception {
    DocumentAnalysisRequest analysisRequest = resolveAnalysisRequest(req);
    return handleAnalysisRequest(analysisRequest, req.getSchema());
  }
  @Override
  public String getDescription() {
    return "Provides a breakdown of the analysis process of provided documents";
  }
  @Override
  public String getVersion() {
    return "$Revision: 824333 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: DocumentAnalysisRequestHandler.java 824333 2009-10-12 13:40:27Z ehatcher $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/DocumentAnalysisRequestHandler.java $";
  }
  DocumentAnalysisRequest resolveAnalysisRequest(SolrQueryRequest req) throws IOException, XMLStreamException {
    DocumentAnalysisRequest request = new DocumentAnalysisRequest();
    SolrParams params = req.getParams();
    String query = params.get(AnalysisParams.QUERY, params.get(CommonParams.Q, null));
    request.setQuery(query);
    boolean showMatch = params.getBool(AnalysisParams.SHOW_MATCH, false);
    request.setShowMatch(showMatch);
    ContentStream stream = extractSingleContentStream(req);
    Reader reader = stream.getReader();
    XMLStreamReader parser = inputFactory.createXMLStreamReader(reader);
    try {
      while (true) {
        int event = parser.next();
        switch (event) {
          case XMLStreamConstants.END_DOCUMENT: {
            parser.close();
            return request;
          }
          case XMLStreamConstants.START_ELEMENT: {
            String currTag = parser.getLocalName();
            if ("doc".equals(currTag)) {
              log.trace("Reading doc...");
              SolrInputDocument document = readDocument(parser, req.getSchema());
              request.addDocument(document);
            }
            break;
          }
        }
      }
    } finally {
      parser.close();
      IOUtils.closeQuietly(reader);
    }
  }
  NamedList<Object> handleAnalysisRequest(DocumentAnalysisRequest request, IndexSchema schema) {
    SchemaField uniqueKeyField = schema.getUniqueKeyField();
    NamedList<Object> result = new SimpleOrderedMap<Object>();
    for (SolrInputDocument document : request.getDocuments()) {
      NamedList<NamedList> theTokens = new SimpleOrderedMap<NamedList>();
      result.add(document.getFieldValue(uniqueKeyField.getName()).toString(), theTokens);
      for (String name : document.getFieldNames()) {
        SchemaField field = schema.getField(name);
        if (!field.indexed()) {
          continue;
        }
        NamedList<Object> fieldTokens = new SimpleOrderedMap<Object>();
        theTokens.add(name, fieldTokens);
        FieldType fieldType = schema.getFieldType(name);
        Set<String> termsToMatch = new HashSet<String>();
        if (request.getQuery() != null && request.isShowMatch()) {
          try {
            List<Token> tokens = analyzeValue(request.getQuery(), fieldType.getQueryAnalyzer());
            for (Token token : tokens) {
              termsToMatch.add(token.term());
            }
          } catch (Exception e) {
          }
        }
        if (request.getQuery() != null) {
          try {
            AnalysisContext analysisContext = new AnalysisContext(fieldType, fieldType.getQueryAnalyzer(), Collections.EMPTY_SET);
            NamedList<List<NamedList>> tokens = analyzeValue(request.getQuery(), analysisContext);
            fieldTokens.add("query", tokens);
          } catch (Exception e) {
          }
        }
        Analyzer analyzer = fieldType.getAnalyzer();
        AnalysisContext analysisContext = new AnalysisContext(fieldType, analyzer, termsToMatch);
        Collection<Object> fieldValues = document.getFieldValues(name);
        NamedList<NamedList<List<NamedList>>> indexTokens = new SimpleOrderedMap<NamedList<List<NamedList>>>();
        for (Object fieldValue : fieldValues) {
          NamedList<List<NamedList>> tokens = analyzeValue(fieldValue.toString(), analysisContext);
          indexTokens.add(String.valueOf(fieldValue), tokens);
        }
        fieldTokens.add("index", indexTokens);
      }
    }
    return result;
  }
  SolrInputDocument readDocument(XMLStreamReader reader, IndexSchema schema) throws XMLStreamException {
    SolrInputDocument doc = new SolrInputDocument();
    String uniqueKeyField = schema.getUniqueKeyField().getName();
    StringBuilder text = new StringBuilder();
    String fieldName = null;
    boolean hasId = false;
    while (true) {
      int event = reader.next();
      switch (event) {
        case XMLStreamConstants.SPACE:
        case XMLStreamConstants.CDATA:
        case XMLStreamConstants.CHARACTERS:
          text.append(reader.getText());
          break;
        case XMLStreamConstants.END_ELEMENT:
          if ("doc".equals(reader.getLocalName())) {
            if (!hasId) {
              throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                      "All documents must contain a unique key value: '" + doc.toString() + "'");
            }
            return doc;
          } else if ("field".equals(reader.getLocalName())) {
            doc.addField(fieldName, text.toString(), DEFAULT_BOOST);
            if (uniqueKeyField.equals(fieldName)) {
              hasId = true;
            }
          }
          break;
        case XMLStreamConstants.START_ELEMENT:
          text.setLength(0);
          String localName = reader.getLocalName();
          if (!"field".equals(localName)) {
            log.warn("unexpected XML tag doc/" + localName);
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "unexpected XML tag doc/" + localName);
          }
          for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attrName = reader.getAttributeLocalName(i);
            if ("name".equals(attrName)) {
              fieldName = reader.getAttributeValue(i);
            }
          }
          break;
      }
    }
  }
  private ContentStream extractSingleContentStream(SolrQueryRequest req) {
    Iterable<ContentStream> streams = req.getContentStreams();
    if (streams == null) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "DocumentAnlysisRequestHandler expects a single content stream with documents to analyze");
    }
    Iterator<ContentStream> iter = streams.iterator();
    if (!iter.hasNext()) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "DocumentAnlysisRequestHandler expects a single content stream with documents to analyze");
    }
    ContentStream stream = iter.next();
    if (iter.hasNext()) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "DocumentAnlysisRequestHandler expects a single content stream with documents to analyze");
    }
    return stream;
  }
}
