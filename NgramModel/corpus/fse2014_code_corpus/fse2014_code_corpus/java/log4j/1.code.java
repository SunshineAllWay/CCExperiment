package org.apache.log4j.xml;
import org.apache.log4j.Category;
import org.apache.log4j.Layout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.DateLayout;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;
import org.apache.xerces.parsers.SAXParser;
import org.apache.trax.Processor;
import org.apache.trax.TemplatesBuilder;
import org.apache.trax.Templates;
import org.apache.trax.Transformer;
import org.apache.trax.Result;
import org.apache.trax.ProcessorException; 
import org.apache.trax.ProcessorFactoryException;
import org.apache.trax.TransformException; 
import org.apache.serialize.SerializerFactory;
import org.apache.serialize.Serializer;
import org.apache.serialize.OutputFormat;
import org.xml.sax.helpers.AttributesImpl;
import java.io.FileOutputStream;
import java.io.IOException;
public class Transform {
  public static void main(String[] args) throws Exception {
    PropertyConfigurator.disableAll();
    PropertyConfigurator.configure("x.lcf");
    Processor processor = Processor.newInstance("xslt");
    XMLReader reader = XMLReaderFactory.createXMLReader();
    TemplatesBuilder templatesBuilder = processor.getTemplatesBuilder();
    reader.setContentHandler(templatesBuilder);
    if(templatesBuilder instanceof LexicalHandler) {
       reader.setProperty("http://xml.org/sax/properties/lexical-handler", 
                           templatesBuilder);
    }
    reader.parse(args[0]);
    Templates templates = templatesBuilder.getTemplates();
    Transformer transformer = templates.newTransformer();
	FileOutputStream fos = new FileOutputStream(args[2]);
    Result result = new Result(fos);
    Serializer serializer = SerializerFactory.getSerializer("xml");
    serializer.setOutputStream(fos);
    transformer.setContentHandler(serializer.asContentHandler());
    org.xml.sax.ContentHandler chandler = transformer.getInputContentHandler();
    DC dc = new DC(chandler);
    reader.setContentHandler(dc);
    if(chandler instanceof LexicalHandler) {
       reader.setProperty("http://xml.org/sax/properties/lexical-handler", 
			  chandler);
    } else {
       reader.setProperty("http://xml.org/sax/properties/lexical-handler", 
			  null);
    }
    reader.parse(args[1]);
  }	
}
 class DC implements ContentHandler {
   static Category cat = Category.getInstance("DC");
   ContentHandler  chandler;
   DC(ContentHandler chandler) {
     this.chandler = chandler;
   }
  public 
  void characters(char[] ch, int start, int length) 
                            throws org.xml.sax.SAXException {
    cat.debug("characters: ["+new String(ch, start, length)+ "] called");
    chandler.characters(ch, start, length);
  }
  public 
  void endDocument() throws org.xml.sax.SAXException {
    cat.debug("endDocument called.");
    chandler.endDocument();
  }
  public 
  void endElement(String namespaceURI, String localName, String qName)
                                           throws org.xml.sax.SAXException {
    cat.debug("endElement("+namespaceURI+", "+localName+", "+qName+") called");
    chandler.endElement(namespaceURI, localName, qName);
  }
   public
   void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
     cat.debug("endPrefixMapping("+prefix+") called");
     chandler.endPrefixMapping(prefix);
   }
  public 
  void ignorableWhitespace(char[] ch, int start, int length) 
                                     throws org.xml.sax.SAXException {
    cat.debug("ignorableWhitespace called");
    chandler.ignorableWhitespace(ch, start, length);
  }
  public 
  void processingInstruction(java.lang.String target, java.lang.String data) 
                                              throws org.xml.sax.SAXException {
    cat.debug("processingInstruction called");
    chandler.processingInstruction(target, data);
  }
  public 
  void setDocumentLocator(Locator locator)  {
    cat.debug("setDocumentLocator called");
    chandler.setDocumentLocator(locator);
  }
   public
   void skippedEntity(String name) throws org.xml.sax.SAXException {
     cat.debug("skippedEntity("+name+")  called");
     chandler.skippedEntity(name);     
   }
  public 
  void startDocument() throws org.xml.sax.SAXException {
    cat.debug("startDocument called");
    chandler.startDocument();
  }
  public 
  void startElement(String namespaceURI, String localName, String qName,
		    Attributes atts) throws org.xml.sax.SAXException {
    cat.debug("startElement("+namespaceURI+", "+localName+", "+qName+")called");
    if("log4j:event".equals(qName)) {
      cat.debug("-------------");      
      if(atts instanceof org.xml.sax.helpers.AttributesImpl) {
	AttributesImpl ai = (AttributesImpl) atts;
	int i = atts.getIndex("timestamp");
	ai.setValue(i, "hello");
      }
      String ts = atts.getValue("timestamp");
      cat.debug("New timestamp is " + ts);
    }
    chandler.startElement(namespaceURI, localName, qName, atts);
  }
   public
   void startPrefixMapping(String prefix, String uri) 
                                          throws org.xml.sax.SAXException {
     cat.debug("startPrefixMapping("+prefix+", "+uri+") called");     
     chandler.startPrefixMapping(prefix, uri);
   }
}
