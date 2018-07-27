package org.apache.xerces.parsers;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDTDContentModelSource;
import org.apache.xerces.xni.parser.XMLDTDSource;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
public abstract class AbstractXMLDocumentParser
    extends XMLParser
    implements XMLDocumentHandler, XMLDTDHandler, XMLDTDContentModelHandler {
    protected boolean fInDTD;
    protected XMLDocumentSource fDocumentSource;
    protected XMLDTDSource fDTDSource;
    protected XMLDTDContentModelSource fDTDContentModelSource;
    protected AbstractXMLDocumentParser(XMLParserConfiguration config) {
        super(config);
        config.setDocumentHandler(this);
        config.setDTDHandler(this);
        config.setDTDContentModelHandler(this);
    } 
    public void startDocument(XMLLocator locator, String encoding, 
                              NamespaceContext namespaceContext, Augmentations augs) 
        throws XNIException {
    } 
    public void xmlDecl(String version, String encoding, String standalone, Augmentations augs)
        throws XNIException {
    } 
    public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
        throws XNIException {
    } 
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
    } 
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        startElement(element, attributes, augs);
        endElement(element, augs);
    } 
    public void characters(XMLString text, Augmentations augs) throws XNIException {
    } 
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
    } 
    public void endElement(QName element, Augmentations augs) throws XNIException {
    } 
    public void startCDATA(Augmentations augs) throws XNIException {
    } 
    public void endCDATA(Augmentations augs) throws XNIException {
    } 
    public void endDocument(Augmentations augs) throws XNIException {
    } 
    public void startGeneralEntity(String name, 
                                   XMLResourceIdentifier identifier,
                                   String encoding,
                                   Augmentations augs) throws XNIException {
    } 
    public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
    } 
    public void endGeneralEntity(String name, Augmentations augs) 
        throws XNIException {
    } 
    public void comment(XMLString text, Augmentations augs) throws XNIException {
    } 
    public void processingInstruction(String target, XMLString data, Augmentations augs)
        throws XNIException {
    } 
    public void setDocumentSource(XMLDocumentSource source){
        fDocumentSource = source;
    } 
    public XMLDocumentSource getDocumentSource (){
        return fDocumentSource;
    } 
    public void startDTD(XMLLocator locator, Augmentations augs) throws XNIException {
        fInDTD = true;
    } 
    public void startExternalSubset(XMLResourceIdentifier identifier, Augmentations augmentations) 
        throws XNIException {
    } 
    public void endExternalSubset(Augmentations augmentations) 
        throws XNIException {
    } 
    public void startParameterEntity(String name, 
                                     XMLResourceIdentifier identifier,
                                     String encoding,
                                     Augmentations augs) throws XNIException {
    } 
    public void endParameterEntity(String name, Augmentations augs) 
        throws XNIException {
    } 
     public void ignoredCharacters(XMLString text, Augmentations augs) throws XNIException {
     } 
    public void elementDecl(String name, String contentModel, Augmentations augs)
        throws XNIException {
    } 
    public void startAttlist(String elementName, Augmentations augs) throws XNIException {
    } 
    public void attributeDecl(String elementName, String attributeName, 
                              String type, String[] enumeration, 
                              String defaultType, XMLString defaultValue, 
			      XMLString nonNormalizedDefaultValue, Augmentations augs)
        throws XNIException {
    } 
    public void endAttlist(Augmentations augs) throws XNIException {
    } 
    public void internalEntityDecl(String name, XMLString text,
                                   XMLString nonNormalizedText, Augmentations augs) 
        throws XNIException {
    } 
    public void externalEntityDecl(String name, XMLResourceIdentifier identifier,
                                   Augmentations augs) throws XNIException {
    } 
    public void unparsedEntityDecl(String name, XMLResourceIdentifier identifier,
                                   String notation, Augmentations augs) throws XNIException {
    } 
    public void notationDecl(String name, XMLResourceIdentifier identifier, 
    	Augmentations augs)
        throws XNIException {
    } 
    public void startConditional(short type, Augmentations augs) throws XNIException  {
    } 
    public void endConditional(Augmentations augs) throws XNIException {
    } 
    public void endDTD(Augmentations augs) throws XNIException {
        fInDTD = false;
    } 
    public void setDTDSource(XMLDTDSource source) {
        fDTDSource = source;
    }
    public XMLDTDSource getDTDSource() {
        return fDTDSource;
    }
    public void startContentModel(String elementName, Augmentations augs) throws XNIException {
    } 
    public void any(Augmentations augs) throws XNIException {
    } 
    public void empty(Augmentations augs) throws XNIException {
    } 
    public void startGroup(Augmentations augs) throws XNIException {
    } 
    public void pcdata(Augmentations augs) throws XNIException {
    } 
    public void element(String elementName, Augmentations augs) throws XNIException {
    } 
    public void separator(short separator, Augmentations augs) throws XNIException {
    } 
    public void occurrence(short occurrence, Augmentations augs) throws XNIException {
    } 
    public void endGroup(Augmentations augs) throws XNIException {
    } 
    public void endContentModel(Augmentations augs) throws XNIException {
    } 
    public void setDTDContentModelSource(XMLDTDContentModelSource source) {
        fDTDContentModelSource = source;
    }
    public XMLDTDContentModelSource getDTDContentModelSource() {
        return fDTDContentModelSource;
    }
    protected void reset() throws XNIException {
        super.reset();
        fInDTD = false;
    } 
} 
