package org.apache.xerces.jaxp;
import java.io.IOException;
import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.opti.DefaultXMLDocumentHandler;
import org.apache.xerces.util.AttributesProxy;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.ErrorHandlerProxy;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.util.LocatorProxy;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
final class JAXPValidatorComponent 
    extends TeeXMLDocumentFilterImpl implements XMLComponent {
    private static final String ENTITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;    
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    private final ValidatorHandler validator;
    private final XNI2SAX xni2sax = new XNI2SAX();
    private final SAX2XNI sax2xni = new SAX2XNI();
    private final TypeInfoProvider typeInfoProvider;
    private Augmentations fCurrentAug;
    private XMLAttributes fCurrentAttributes;
    private SymbolTable fSymbolTable;
    private XMLErrorReporter fErrorReporter;
    private XMLEntityResolver fEntityResolver;
    public JAXPValidatorComponent( ValidatorHandler validatorHandler ) {
        this.validator = validatorHandler;
        TypeInfoProvider tip = validatorHandler.getTypeInfoProvider();
        if(tip==null)   tip = noInfoProvider;
        this.typeInfoProvider = tip;
        xni2sax.setContentHandler(validator);
        validator.setContentHandler(sax2xni);
        this.setSide(xni2sax);
        validator.setErrorHandler(new ErrorHandlerProxy() {
            protected XMLErrorHandler getErrorHandler() {
                XMLErrorHandler handler = fErrorReporter.getErrorHandler();
                if(handler!=null)   return handler;
                return new ErrorHandlerWrapper(DraconianErrorHandler.getInstance());
            }
        });
        validator.setResourceResolver(new LSResourceResolver() {
            public LSInput resolveResource(String type,String ns, String publicId, String systemId, String baseUri) {
                if(fEntityResolver==null)   return null;
                try {
                    XMLInputSource is = fEntityResolver.resolveEntity(
                        new XMLResourceIdentifierImpl(publicId,systemId,baseUri,null));
                    if(is==null)    return null;
                    LSInput di = new DOMInputImpl();
                    di.setBaseURI(is.getBaseSystemId());
                    di.setByteStream(is.getByteStream());
                    di.setCharacterStream(is.getCharacterStream());
                    di.setEncoding(is.getEncoding());
                    di.setPublicId(is.getPublicId());
                    di.setSystemId(is.getSystemId());
                    return di;
                } catch( IOException e ) {
                    throw new XNIException(e);
                }
            }
        });
    }
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
        fCurrentAttributes = attributes;
        fCurrentAug = augs;
        xni2sax.startElement(element,attributes,null);
        fCurrentAttributes = null; 
    }
    public void endElement(QName element, Augmentations augs) throws XNIException {
        fCurrentAug = augs;
        xni2sax.endElement(element,null);
    }
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
        startElement(element,attributes,augs);
        endElement(element,augs);
    }
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        fCurrentAug = augs;
        xni2sax.characters(text,null);
    }
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
        fCurrentAug = augs;
        xni2sax.ignorableWhitespace(text,null);
    }
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        try {
            fEntityResolver = (XMLEntityResolver) componentManager.getProperty(ENTITY_MANAGER);
        }
        catch (XMLConfigurationException e) {
            fEntityResolver = null;
        }
    }
    private final class SAX2XNI extends DefaultHandler {
        private final Augmentations fAugmentations = new AugmentationsImpl();
        private final QName fQName = new QName(); 
        public void characters(char[] ch, int start, int len) throws SAXException {
            try {
                handler().characters(new XMLString(ch,start,len),aug());
            } catch( XNIException e ) {
                throw toSAXException(e);
            }
        }
        public void ignorableWhitespace(char[] ch, int start, int len) throws SAXException {
            try {
                handler().ignorableWhitespace(new XMLString(ch,start,len),aug());
            } catch( XNIException e ) {
                throw toSAXException(e);
            }
        }
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            try {
                updateAttributes(atts);
                handler().startElement(toQName(uri,localName,qname), fCurrentAttributes, elementAug());
            } catch( XNIException e ) {
                throw toSAXException(e);
            }
        }
        public void endElement(String uri, String localName, String qname) throws SAXException {
            try {
                handler().endElement(toQName(uri,localName,qname),aug());
            } catch( XNIException e ) {
                throw toSAXException(e);
            }
        }
        private Augmentations elementAug() {
            Augmentations aug = aug();
            return aug;
        }
        private Augmentations aug() {
            if( fCurrentAug!=null ) {
                Augmentations r = fCurrentAug;
                fCurrentAug = null; 
                return r;
            }
            fAugmentations.removeAllItems();
            return fAugmentations;
        }
        private XMLDocumentHandler handler() {
            return JAXPValidatorComponent.this.getDocumentHandler();
        }
        private SAXException toSAXException( XNIException xe ) {
            Exception e = xe.getException();
            if( e==null )   e = xe;
            if( e instanceof SAXException )  return (SAXException)e;
            return new SAXException(e);
        }
        private QName toQName( String uri, String localName, String qname ) {
            String prefix = null;
            int idx = qname.indexOf(':');
            if( idx>0 )
                prefix = symbolize(qname.substring(0,idx));
            localName = symbolize(localName);
            qname = symbolize(qname);
            uri = symbolize(uri);
            fQName.setValues(prefix, localName, qname, uri);
            return fQName;
        }
    }
    private static final class XNI2SAX extends DefaultXMLDocumentHandler {
        private ContentHandler fContentHandler;
        private String fVersion;
        protected NamespaceContext fNamespaceContext;
        private final AttributesProxy fAttributesProxy = new AttributesProxy(null);
        public void setContentHandler( ContentHandler handler ) {
            this.fContentHandler = handler;
        }
        public ContentHandler getContentHandler() {
            return fContentHandler;
        }
        public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException {
            this.fVersion = version;
        }
        public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext, Augmentations augs) throws XNIException {
            fNamespaceContext = namespaceContext;
            fContentHandler.setDocumentLocator(new LocatorProxy(locator));
            try {
                fContentHandler.startDocument();
            } catch (SAXException e) {
                throw new XNIException(e);
            }
        }
        public void endDocument(Augmentations augs) throws XNIException {
            try {
                fContentHandler.endDocument();
            } catch (SAXException e) {
                throw new XNIException(e);
            }
        }
        public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
            try {
                fContentHandler.processingInstruction(target,data.toString());
            } catch (SAXException e) {
                throw new XNIException(e);
            }
        }
        public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
            try {
                int count = fNamespaceContext.getDeclaredPrefixCount();
                if (count > 0) {
                    String prefix = null;
                    String uri = null;
                    for (int i = 0; i < count; i++) {
                        prefix = fNamespaceContext.getDeclaredPrefixAt(i);
                        uri = fNamespaceContext.getURI(prefix);
                        fContentHandler.startPrefixMapping(prefix, (uri == null)?"":uri);
                    }
                }
                String uri = element.uri != null ? element.uri : "";
                String localpart = element.localpart;
                fAttributesProxy.setAttributes(attributes);
                fContentHandler.startElement(uri, localpart, element.rawname, fAttributesProxy);
            } catch( SAXException e ) {
                throw new XNIException(e);
            }
        }
        public void endElement(QName element, Augmentations augs) throws XNIException {
            try {
                String uri = element.uri != null ? element.uri : "";
                String localpart = element.localpart;
                fContentHandler.endElement(uri, localpart, element.rawname);
                int count = fNamespaceContext.getDeclaredPrefixCount();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        fContentHandler.endPrefixMapping(fNamespaceContext.getDeclaredPrefixAt(i));
                    }
                }
            } catch( SAXException e ) {
                throw new XNIException(e);
            }
        }
        public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
            startElement(element,attributes,augs);
            endElement(element,augs);
        }
        public void characters(XMLString text, Augmentations augs) throws XNIException {
            try {
                fContentHandler.characters(text.ch,text.offset,text.length);
            } catch (SAXException e) {
                throw new XNIException(e);
            }
        }
        public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
            try {
                fContentHandler.ignorableWhitespace(text.ch,text.offset,text.length);
            } catch (SAXException e) {
                throw new XNIException(e);
            }
        }
    }
    private static final class DraconianErrorHandler implements ErrorHandler {
        private static final DraconianErrorHandler ERROR_HANDLER_INSTANCE 
            = new DraconianErrorHandler();
        private DraconianErrorHandler() {}
        public static DraconianErrorHandler getInstance() {
            return ERROR_HANDLER_INSTANCE;
        }
        public void warning(SAXParseException e) throws SAXException {
        }
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    } 
    private void updateAttributes( Attributes atts ) {
        int len = atts.getLength();
        for( int i=0; i<len; i++ ) {
            String aqn = atts.getQName(i);
            int j = fCurrentAttributes.getIndex(aqn);
            String av = atts.getValue(i);
            if(j==-1) {
                String prefix;
                int idx = aqn.indexOf(':');
                if( idx<0 ) {
                    prefix = null;
                } else {
                    prefix = symbolize(aqn.substring(0,idx));
                }
                j = fCurrentAttributes.addAttribute(
                    new QName(
                        prefix,
                        symbolize(atts.getLocalName(i)),
                        symbolize(aqn),
                        symbolize(atts.getURI(i))),
                    atts.getType(i),av);
            } else {
                if( !av.equals(fCurrentAttributes.getValue(j)) ) {
                    fCurrentAttributes.setValue(j,av);
                }
            }
        }
    }
    private String symbolize( String s ) {
        return fSymbolTable.addSymbol(s);
    }
    private static final TypeInfoProvider noInfoProvider = new TypeInfoProvider() {
        public TypeInfo getElementTypeInfo() {
            return null;
        }
        public TypeInfo getAttributeTypeInfo(int index) {
            return null;
        }
        public TypeInfo getAttributeTypeInfo(String attributeQName) {
            return null;
        }
        public TypeInfo getAttributeTypeInfo(String attributeUri, String attributeLocalName) {
            return null;
        }
        public boolean isIdAttribute(int index) {
            return false;
        }
        public boolean isSpecified(int index) {
            return false;
        }
    };
    public String[] getRecognizedFeatures() {
        return null;
    }
    public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
    }
    public String[] getRecognizedProperties() {
        return new String[]{ENTITY_MANAGER, ERROR_REPORTER, SYMBOL_TABLE};
    }
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
    }
    public Boolean getFeatureDefault(String featureId) {
        return null;
    }
    public Object getPropertyDefault(String propertyId) {
        return null;
    }
}
