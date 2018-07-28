package org.apache.solr.handler.extraction;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.handler.ContentStreamLoader;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.tika.sax.xpath.Matcher;
import org.apache.tika.sax.xpath.MatchingContentHandler;
import org.apache.tika.sax.xpath.XPathParser;
import org.apache.tika.exception.TikaException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.BaseMarkupSerializer;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.TextSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
public class ExtractingDocumentLoader extends ContentStreamLoader {
  public static final String TEXT_FORMAT = "text";
  public static final String XML_FORMAT = "xml";
  private static final XPathParser PARSER =
          new XPathParser("xhtml", XHTMLContentHandler.XHTML);
  final IndexSchema schema;
  final SolrParams params;
  final UpdateRequestProcessor processor;
  protected AutoDetectParser autoDetectParser;
  private final AddUpdateCommand templateAdd;
  protected TikaConfig config;
  protected SolrContentHandlerFactory factory;
  public ExtractingDocumentLoader(SolrQueryRequest req, UpdateRequestProcessor processor,
                           TikaConfig config, SolrContentHandlerFactory factory) {
    this.params = req.getParams();
    schema = req.getSchema();
    this.config = config;
    this.processor = processor;
    templateAdd = new AddUpdateCommand();
    templateAdd.allowDups = false;
    templateAdd.overwriteCommitted = true;
    templateAdd.overwritePending = true;
    if (params.getBool(UpdateParams.OVERWRITE, true)) {
      templateAdd.allowDups = false;
      templateAdd.overwriteCommitted = true;
      templateAdd.overwritePending = true;
    } else {
      templateAdd.allowDups = true;
      templateAdd.overwriteCommitted = false;
      templateAdd.overwritePending = false;
    }
    autoDetectParser = new AutoDetectParser(config);
    this.factory = factory;
  }
  void doAdd(SolrContentHandler handler, AddUpdateCommand template)
          throws IOException {
    template.solrDoc = handler.newDocument();
    processor.processAdd(template);
  }
  void addDoc(SolrContentHandler handler) throws IOException {
    templateAdd.indexedId = null;
    doAdd(handler, templateAdd);
  }
  public void load(SolrQueryRequest req, SolrQueryResponse rsp, ContentStream stream) throws IOException {
    errHeader = "ExtractingDocumentLoader: " + stream.getSourceInfo();
    Parser parser = null;
    String streamType = req.getParams().get(ExtractingParams.STREAM_TYPE, null);
    if (streamType != null) {
      parser = config.getParser(streamType.trim().toLowerCase());
    } else {
      parser = autoDetectParser;
    }
    if (parser != null) {
      Metadata metadata = new Metadata();
      metadata.add(ExtractingMetadataConstants.STREAM_NAME, stream.getName());
      metadata.add(ExtractingMetadataConstants.STREAM_SOURCE_INFO, stream.getSourceInfo());
      metadata.add(ExtractingMetadataConstants.STREAM_SIZE, String.valueOf(stream.getSize()));
      metadata.add(ExtractingMetadataConstants.STREAM_CONTENT_TYPE, stream.getContentType());
      String resourceName = req.getParams().get(ExtractingParams.RESOURCE_NAME, null);
      if (resourceName != null) {
        metadata.add(Metadata.RESOURCE_NAME_KEY, resourceName);
      }
      SolrContentHandler handler = factory.createSolrContentHandler(metadata, params, schema);
      InputStream inputStream = null;
      try {
        inputStream = stream.getStream();
        String xpathExpr = params.get(ExtractingParams.XPATH_EXPRESSION);
        boolean extractOnly = params.getBool(ExtractingParams.EXTRACT_ONLY, false);
        ContentHandler parsingHandler = handler;
        StringWriter writer = null;
        BaseMarkupSerializer serializer = null;
        if (extractOnly == true) {
          String extractFormat = params.get(ExtractingParams.EXTRACT_FORMAT, "xml");
          writer = new StringWriter();
          if (extractFormat.equals(TEXT_FORMAT)) {
            serializer = new TextSerializer();
            serializer.setOutputCharStream(writer);
            serializer.setOutputFormat(new OutputFormat("Text", "UTF-8", true));
          } else {
            serializer = new XMLSerializer(writer, new OutputFormat("XML", "UTF-8", true));
          }
          if (xpathExpr != null) {
            Matcher matcher =
                    PARSER.parse(xpathExpr);
            serializer.startDocument();
            parsingHandler = new MatchingContentHandler(serializer, matcher);
          } else {
            parsingHandler = serializer;
          }
        } else if (xpathExpr != null) {
          Matcher matcher =
                  PARSER.parse(xpathExpr);
          parsingHandler = new MatchingContentHandler(handler, matcher);
        } 
        parser.parse(inputStream, parsingHandler, metadata);
        if (extractOnly == false) {
          addDoc(handler);
        } else {
          if (xpathExpr != null){
            serializer.endDocument();
          }
          rsp.add(stream.getName(), writer.toString());
          writer.close();
          String[] names = metadata.names();
          NamedList metadataNL = new NamedList();
          for (int i = 0; i < names.length; i++) {
            String[] vals = metadata.getValues(names[i]);
            metadataNL.add(names[i], vals);
          }
          rsp.add(stream.getName() + "_metadata", metadataNL);
        }
      } catch (SAXException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      } catch (TikaException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    } else {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Stream type of " + streamType + " didn't match any known parsers.  Please supply the " + ExtractingParams.STREAM_TYPE + " parameter.");
    }
  }
}
