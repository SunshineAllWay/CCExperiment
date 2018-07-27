package org.apache.solr.handler.dataimport;
import org.apache.commons.io.IOUtils;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;
import static org.apache.solr.handler.dataimport.DataImporter.COLUMN;
import static org.apache.solr.handler.dataimport.XPathEntityProcessor.URL;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.apache.tika.sax.XHTMLContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
public class TikaEntityProcessor extends EntityProcessorBase {
  private TikaConfig tikaConfig;
  private static final Logger LOG = LoggerFactory.getLogger(TikaEntityProcessor.class);
  private String format = "text";
  private boolean done = false;
  private String parser;
  static final String AUTO_PARSER = "org.apache.tika.parser.AutoDetectParser";
  @Override
  protected void firstInit(Context context) {
    String tikaConfigFile = context.getResolvedEntityAttribute("tikaConfig");
    if (tikaConfigFile == null) {
      tikaConfig = TikaConfig.getDefaultConfig();
    } else {
      File configFile = new File(tikaConfigFile);
      if (!configFile.isAbsolute()) {
        configFile = new File(context.getSolrCore().getResourceLoader().getConfigDir(), tikaConfigFile);
      }
      try {
        tikaConfig = new TikaConfig(configFile);
      } catch (Exception e) {
        wrapAndThrow (SEVERE, e,"Unable to load Tika Config");
      }
    }
    format = context.getResolvedEntityAttribute("format");
    if(format == null)
      format = "text";
    if (!"html".equals(format) && !"xml".equals(format) && !"text".equals(format)&& !"none".equals(format) )
      throw new DataImportHandlerException(SEVERE, "'format' can be one of text|html|xml|none");
    parser = context.getResolvedEntityAttribute("parser");
    if(parser == null) {
      parser = AUTO_PARSER;
    }
    done = false;
  }
  public Map<String, Object> nextRow() {
    if(done) return null;
    Map<String, Object> row = new HashMap<String, Object>();
    DataSource<InputStream> dataSource = context.getDataSource();
    InputStream is = dataSource.getData(context.getResolvedEntityAttribute(URL));
    ContentHandler contentHandler = null;
    Metadata metadata = new Metadata();
    StringWriter sw = new StringWriter();
    try {
      if ("html".equals(format)) {
        contentHandler = getHtmlHandler(sw);
      } else if ("xml".equals(format)) {
        contentHandler = getXmlContentHandler(sw);
      } else if ("text".equals(format)) {
        contentHandler = getTextContentHandler(sw);
      } else if("none".equals(format)){
        contentHandler = new DefaultHandler();        
      }
    } catch (TransformerConfigurationException e) {
      wrapAndThrow(SEVERE, e, "Unable to create content handler");
    }
    Parser tikaParser = null;
    if(parser.equals(AUTO_PARSER)){
      AutoDetectParser parser = new AutoDetectParser();
      parser.setConfig(tikaConfig);
      tikaParser = parser;
    } else {
      tikaParser = (Parser) context.getSolrCore().getResourceLoader().newInstance(parser);
    }
    try {
      tikaParser.parse(is, contentHandler, metadata , new ParseContext());
    } catch (Exception e) {
      wrapAndThrow(SEVERE, e, "Unable to read content");
    }
    IOUtils.closeQuietly(is);
    for (Map<String, String> field : context.getAllEntityFields()) {
      if (!"true".equals(field.get("meta"))) continue;
      String col = field.get(COLUMN);
      String s = metadata.get(col);
      if (s != null) row.put(col, s);
    }
    if(!"none".equals(format) ) row.put("text", sw.toString());
    done = true;
    return row;
  }
  private static ContentHandler getHtmlHandler(Writer writer)
          throws TransformerConfigurationException {
    SAXTransformerFactory factory = (SAXTransformerFactory)
            SAXTransformerFactory.newInstance();
    TransformerHandler handler = factory.newTransformerHandler();
    handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
    handler.setResult(new StreamResult(writer));
    return new ContentHandlerDecorator(handler) {
      @Override
      public void startElement(
              String uri, String localName, String name, Attributes atts)
              throws SAXException {
        if (XHTMLContentHandler.XHTML.equals(uri)) {
          uri = null;
        }
        if (!"head".equals(localName)) {
          super.startElement(uri, localName, name, atts);
        }
      }
      @Override
      public void endElement(String uri, String localName, String name)
              throws SAXException {
        if (XHTMLContentHandler.XHTML.equals(uri)) {
          uri = null;
        }
        if (!"head".equals(localName)) {
          super.endElement(uri, localName, name);
        }
      }
      @Override
      public void startPrefixMapping(String prefix, String uri) { }
      @Override
      public void endPrefixMapping(String prefix) { }
    };
  }
  private static ContentHandler getTextContentHandler(Writer writer) {
    return new BodyContentHandler(writer);
  }
  private static ContentHandler getXmlContentHandler(Writer writer)
          throws TransformerConfigurationException {
    SAXTransformerFactory factory = (SAXTransformerFactory)
            SAXTransformerFactory.newInstance();
    TransformerHandler handler = factory.newTransformerHandler();
    handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
    handler.setResult(new StreamResult(writer));
    return handler;
  }
}
