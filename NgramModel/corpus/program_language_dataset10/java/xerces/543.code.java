package org.apache.xerces.jaxp.validation;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.validation.EntityState;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.util.AttributesProxy;
import org.apache.xerces.util.SAXLocatorWrapper;
import org.apache.xerces.util.SAXMessageFormatter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.ItemPSVI;
import org.apache.xerces.xs.PSVIProvider;
import org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.ext.LexicalHandler;
final class ValidatorHandlerImpl extends ValidatorHandler implements
    DTDHandler, EntityState, PSVIProvider, ValidatorHelper, XMLDocumentHandler {
    private static final String NAMESPACE_PREFIXES =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACE_PREFIXES_FEATURE;
    private static final String STRING_INTERNING =
        Constants.SAX_FEATURE_PREFIX + Constants.STRING_INTERNING_FEATURE;
    private static final String STRINGS_INTERNED =
        Constants.XERCES_FEATURE_PREFIX + Constants.STRINGS_INTERNED_FEATURE;
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String LEXICAL_HANDLER =
        Constants.SAX_PROPERTY_PREFIX + Constants.LEXICAL_HANDLER_PROPERTY;
    private static final String NAMESPACE_CONTEXT =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
    private static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    private final XMLErrorReporter fErrorReporter;
    private final NamespaceContext fNamespaceContext;
    private final XMLSchemaValidator fSchemaValidator;
    private final SymbolTable fSymbolTable;
    private final ValidationManager fValidationManager;
    private final XMLSchemaValidatorComponentManager fComponentManager;
    private final SAXLocatorWrapper fSAXLocatorWrapper = new SAXLocatorWrapper();
    private boolean fNeedPushNSContext = true;
    private HashMap fUnparsedEntities = null;
    private boolean fStringsInternalized = false;
    private final QName fElementQName = new QName();
    private final QName fAttributeQName = new QName();
    private final XMLAttributesImpl fAttributes = new XMLAttributesImpl();
    private final AttributesProxy fAttrAdapter = new AttributesProxy(fAttributes); 
    private final XMLString fTempString = new XMLString();
    private ContentHandler fContentHandler = null;
    public ValidatorHandlerImpl(XSGrammarPoolContainer grammarContainer) {
        this(new XMLSchemaValidatorComponentManager(grammarContainer));
        fComponentManager.addRecognizedFeatures(new String [] {NAMESPACE_PREFIXES});
        fComponentManager.setFeature(NAMESPACE_PREFIXES, false);
        setErrorHandler(null);
        setResourceResolver(null);
    }
    public ValidatorHandlerImpl(XMLSchemaValidatorComponentManager componentManager) {
        fComponentManager = componentManager;
        fErrorReporter = (XMLErrorReporter) fComponentManager.getProperty(ERROR_REPORTER);
        fNamespaceContext = (NamespaceContext) fComponentManager.getProperty(NAMESPACE_CONTEXT);
        fSchemaValidator = (XMLSchemaValidator) fComponentManager.getProperty(SCHEMA_VALIDATOR);
        fSymbolTable = (SymbolTable) fComponentManager.getProperty(SYMBOL_TABLE);
        fValidationManager = (ValidationManager) fComponentManager.getProperty(VALIDATION_MANAGER);
    }
    public void setContentHandler(ContentHandler receiver) {
        fContentHandler = receiver;
    }
    public ContentHandler getContentHandler() {
        return fContentHandler;
    }
    public void setErrorHandler(ErrorHandler errorHandler) {
        fComponentManager.setErrorHandler(errorHandler);
    }
    public ErrorHandler getErrorHandler() {
        return fComponentManager.getErrorHandler();
    }
    public void setResourceResolver(LSResourceResolver resourceResolver) {
        fComponentManager.setResourceResolver(resourceResolver);
    }
    public LSResourceResolver getResourceResolver() {
        return fComponentManager.getResourceResolver();
    }
    public TypeInfoProvider getTypeInfoProvider() {
        return fTypeInfoProvider;
    }
    public boolean getFeature(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                    "FeatureNameNull", null));
        }
        if (STRINGS_INTERNED.equals(name)) {
            return fStringsInternalized;
        }
        try {
            return fComponentManager.getFeature(name);
        }
        catch (XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "feature-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "feature-not-supported", new Object [] {identifier}));
            }
        }
    }
    public void setFeature(String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                    "FeatureNameNull", null));
        }
        if (STRINGS_INTERNED.equals(name)) {
            fStringsInternalized = value;
            return;
        }
        try {
            fComponentManager.setFeature(name, value);
        }
        catch (XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "feature-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "feature-not-supported", new Object [] {identifier}));
            }
        }
    }
    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                    "ProperyNameNull", null));
        }
        try {
            return fComponentManager.getProperty(name);
        }
        catch (XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "property-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "property-not-supported", new Object [] {identifier}));
            }
        }
    }
    public void setProperty(String name, Object object)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                    "ProperyNameNull", null));
        }
        try {
            fComponentManager.setProperty(name, object);
        }
        catch (XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "property-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "property-not-supported", new Object [] {identifier}));
            }
        }
    }
    public boolean isEntityDeclared(String name) {
        return false;
    }
    public boolean isEntityUnparsed(String name) {
        if (fUnparsedEntities != null) {
            return fUnparsedEntities.containsKey(name);
        }
        return false;
    }
    public void startDocument(XMLLocator locator, String encoding,
            NamespaceContext namespaceContext, Augmentations augs)
            throws XNIException {
        if (fContentHandler != null) {
            try {
                fContentHandler.startDocument();
            }
            catch (SAXException e) {
                throw new XNIException(e);
            }
        }
    }
    public void xmlDecl(String version, String encoding, String standalone,
            Augmentations augs) throws XNIException {}
    public void doctypeDecl(String rootElement, String publicId,
            String systemId, Augmentations augs) throws XNIException {}
    public void comment(XMLString text, Augmentations augs) throws XNIException {}
    public void processingInstruction(String target, XMLString data,
            Augmentations augs) throws XNIException {
        if (fContentHandler != null) {
            try {
                fContentHandler.processingInstruction(target, data.toString());
            }
            catch (SAXException e) {
                throw new XNIException(e);
            }
        }
    }
    public void startElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        if (fContentHandler != null) {
            try {
                fTypeInfoProvider.beginStartElement(augs, attributes);
                fContentHandler.startElement((element.uri != null) ? element.uri : XMLSymbols.EMPTY_STRING, 
                        element.localpart, element.rawname, fAttrAdapter);
            }
            catch (SAXException e) {
                throw new XNIException(e);
            }
            finally {
                fTypeInfoProvider.finishStartElement();
            }
        }
    }
    public void emptyElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        startElement(element, attributes, augs);
        endElement(element, augs);
    }
    public void startGeneralEntity(String name,
            XMLResourceIdentifier identifier, String encoding,
            Augmentations augs) throws XNIException {}
    public void textDecl(String version, String encoding, Augmentations augs)
            throws XNIException {}
    public void endGeneralEntity(String name, Augmentations augs)
            throws XNIException {}
    public void characters(XMLString text, Augmentations augs)
            throws XNIException {
        if (fContentHandler != null) {
            if (text.length == 0) {
                return;
            }
            try {
                fContentHandler.characters(text.ch, text.offset, text.length);
            }
            catch (SAXException e) {
                throw new XNIException(e);
            }
        }
    }
    public void ignorableWhitespace(XMLString text, Augmentations augs)
            throws XNIException {
        if (fContentHandler != null) {
            try {
                fContentHandler.ignorableWhitespace(text.ch, text.offset, text.length);
            }
            catch (SAXException e) {
                throw new XNIException(e);
            }
        }
    }
    public void endElement(QName element, Augmentations augs)
            throws XNIException {
        if (fContentHandler != null) {
            try {
                fTypeInfoProvider.beginEndElement(augs);
                fContentHandler.endElement((element.uri != null) ? element.uri : XMLSymbols.EMPTY_STRING,
                        element.localpart, element.rawname);
            }
            catch (SAXException e) {
                throw new XNIException(e);
            }
            finally {
                fTypeInfoProvider.finishEndElement();
            }
        }
    }
    public void startCDATA(Augmentations augs) throws XNIException {}
    public void endCDATA(Augmentations augs) throws XNIException {}
    public void endDocument(Augmentations augs) throws XNIException {
        if (fContentHandler != null) {
            try {
                fContentHandler.endDocument();
            }
            catch (SAXException e) {
                throw new XNIException(e);
            }
        }
    }
    public void setDocumentSource(XMLDocumentSource source) {}
    public XMLDocumentSource getDocumentSource() {
        return fSchemaValidator;
    }
    public void setDocumentLocator(Locator locator) {
        fSAXLocatorWrapper.setLocator(locator);
        if (fContentHandler != null) {
            fContentHandler.setDocumentLocator(locator);
        }
    }
    public void startDocument() throws SAXException {
        fComponentManager.reset();
        fSchemaValidator.setDocumentHandler(this);
        fValidationManager.setEntityState(this);
        fTypeInfoProvider.finishStartElement(); 
        fNeedPushNSContext = true;
        if (fUnparsedEntities != null && !fUnparsedEntities.isEmpty()) {
            fUnparsedEntities.clear();
        }
        fErrorReporter.setDocumentLocator(fSAXLocatorWrapper);
        try {
            fSchemaValidator.startDocument(fSAXLocatorWrapper, fSAXLocatorWrapper.getEncoding(), fNamespaceContext, null);
        }
        catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        }
        catch (XNIException e) {
            throw Util.toSAXException(e);
        }
    }
    public void endDocument() throws SAXException {
        fSAXLocatorWrapper.setLocator(null);
        try {
            fSchemaValidator.endDocument(null);
        }
        catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        }
        catch (XNIException e) {
            throw Util.toSAXException(e);
        }
    }
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        String prefixSymbol;
        String uriSymbol;
        if (!fStringsInternalized) {
            prefixSymbol = (prefix != null) ? fSymbolTable.addSymbol(prefix) : XMLSymbols.EMPTY_STRING;
            uriSymbol = (uri != null && uri.length() > 0) ? fSymbolTable.addSymbol(uri) : null;
        }
        else {
            prefixSymbol = (prefix != null) ? prefix : XMLSymbols.EMPTY_STRING;
            uriSymbol = (uri != null && uri.length() > 0) ? uri : null;
        }
        if (fNeedPushNSContext) {
            fNeedPushNSContext = false;
            fNamespaceContext.pushContext();
        }
        fNamespaceContext.declarePrefix(prefixSymbol, uriSymbol);
        if (fContentHandler != null) {
            fContentHandler.startPrefixMapping(prefix, uri);
        }
    }
    public void endPrefixMapping(String prefix) throws SAXException {
        if (fContentHandler != null) {
            fContentHandler.endPrefixMapping(prefix);
        }
    }
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (fNeedPushNSContext) {
            fNamespaceContext.pushContext();
        }
        fNeedPushNSContext = true;
        fillQName(fElementQName, uri, localName, qName);
        if (atts instanceof Attributes2) {
            fillXMLAttributes2((Attributes2) atts);
        }
        else {
            fillXMLAttributes(atts);
        }
        try {
            fSchemaValidator.startElement(fElementQName, fAttributes, null);
        }
        catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        }
        catch (XNIException e) {
            throw Util.toSAXException(e);
        }
    }
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        fillQName(fElementQName, uri, localName, qName);
        try {
            fSchemaValidator.endElement(fElementQName, null);
        }
        catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        }
        catch (XNIException e) {
            throw Util.toSAXException(e);
        }
        finally {
            fNamespaceContext.popContext();
        }
    }
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        try {
            fTempString.setValues(ch, start, length);
            fSchemaValidator.characters(fTempString, null);
        }
        catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        }
        catch (XNIException e) {
            throw Util.toSAXException(e);
        }
    }
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        try {
            fTempString.setValues(ch, start, length);
            fSchemaValidator.ignorableWhitespace(fTempString, null);
        }
        catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        }
        catch (XNIException e) {
            throw Util.toSAXException(e);
        }
    }
    public void processingInstruction(String target, String data)
            throws SAXException {
        if (fContentHandler != null) {
            fContentHandler.processingInstruction(target, data);
        }
    }
    public void skippedEntity(String name) throws SAXException {
        if (fContentHandler != null) {
            fContentHandler.skippedEntity(name);
        }
    }
    public void notationDecl(String name, String publicId, 
            String systemId) throws SAXException {}
    public void unparsedEntityDecl(String name, String publicId, 
            String systemId, String notationName) throws SAXException {
        if (fUnparsedEntities == null) {
            fUnparsedEntities = new HashMap();
        }
        fUnparsedEntities.put(name, name);
    }
    public void validate(Source source, Result result) 
        throws SAXException, IOException {
        if (result instanceof SAXResult || result == null) {
            final SAXSource saxSource = (SAXSource) source;
            final SAXResult saxResult = (SAXResult) result;
            LexicalHandler lh = null;
            if (result != null) {
                ContentHandler ch = saxResult.getHandler();
                lh = saxResult.getLexicalHandler();
                if (lh == null && ch instanceof LexicalHandler) {
                    lh = (LexicalHandler) ch;
                }
                setContentHandler(ch);
            }
            XMLReader reader = null;
            try {
                reader = saxSource.getXMLReader();
                if (reader == null) {
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    spf.setNamespaceAware(true);
                    try {
                        reader = spf.newSAXParser().getXMLReader();
                        if (reader instanceof org.apache.xerces.parsers.SAXParser) {
                            Object securityManager = fComponentManager.getProperty(SECURITY_MANAGER);
                            if (securityManager != null) {
                                try {
                                    reader.setProperty(SECURITY_MANAGER, securityManager);
                                }
                                catch (SAXException exc) {}
                            }
                        }
                    } 
                    catch (Exception e) {
                        throw new FactoryConfigurationError(e);
                    }
                }
                try {
                    fStringsInternalized = reader.getFeature(STRING_INTERNING);
                }
                catch (SAXException exc) {
                    fStringsInternalized = false;
                }
                ErrorHandler errorHandler = fComponentManager.getErrorHandler();
                reader.setErrorHandler(errorHandler != null ? errorHandler : DraconianErrorHandler.getInstance());
                reader.setEntityResolver(fResolutionForwarder);
                fResolutionForwarder.setEntityResolver(fComponentManager.getResourceResolver());
                reader.setContentHandler(this);
                reader.setDTDHandler(this);
                try {
                    reader.setProperty(LEXICAL_HANDLER, lh);
                }
                catch (SAXException exc) {}
                InputSource is = saxSource.getInputSource();
                reader.parse(is);
            } 
            finally {
                setContentHandler(null);
                if (reader != null) {
                    try {
                        reader.setContentHandler(null);
                        reader.setDTDHandler(null);
                        reader.setErrorHandler(null);
                        reader.setEntityResolver(null);
                        fResolutionForwarder.setEntityResolver(null);
                        reader.setProperty(LEXICAL_HANDLER, null);
                    }
                    catch (Exception exc) {}
                }
            }
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                "SourceResultMismatch", 
                new Object [] {source.getClass().getName(), result.getClass().getName()}));
    }
    public ElementPSVI getElementPSVI() {
        return fTypeInfoProvider.getElementPSVI();
    }
    public AttributePSVI getAttributePSVI(int index) {
        return fTypeInfoProvider.getAttributePSVI(index);
    }
    public AttributePSVI getAttributePSVIByName(String uri, String localname) {
        return fTypeInfoProvider.getAttributePSVIByName(uri, localname);
    }
    private void fillQName(QName toFill, String uri, String localpart, String raw) {
        if (!fStringsInternalized) {
            uri = (uri != null && uri.length() > 0) ? fSymbolTable.addSymbol(uri) : null;
            localpart = (localpart != null) ? fSymbolTable.addSymbol(localpart) : XMLSymbols.EMPTY_STRING;
            raw = (raw != null) ? fSymbolTable.addSymbol(raw) : XMLSymbols.EMPTY_STRING;
        }
        else {
            if (uri != null && uri.length() == 0) {
                uri = null;
            }
            if (localpart == null) {
                localpart = XMLSymbols.EMPTY_STRING;
            }
            if (raw == null) {
                raw = XMLSymbols.EMPTY_STRING;
            }
        }
        String prefix = XMLSymbols.EMPTY_STRING;
        int prefixIdx = raw.indexOf(':');
        if (prefixIdx != -1) {
            prefix = fSymbolTable.addSymbol(raw.substring(0, prefixIdx));
        }
        toFill.setValues(prefix, localpart, raw, uri);
    }
    private void fillXMLAttributes(Attributes att) {
        fAttributes.removeAllAttributes();
        final int len = att.getLength();
        for (int i = 0; i < len; ++i) {
            fillXMLAttribute(att, i);
            fAttributes.setSpecified(i, true);
        }
    }
    private void fillXMLAttributes2(Attributes2 att) {
        fAttributes.removeAllAttributes();
        final int len = att.getLength();
        for (int i = 0; i < len; ++i) {
            fillXMLAttribute(att, i);
            fAttributes.setSpecified(i, att.isSpecified(i));
            if (att.isDeclared(i)) {
                fAttributes.getAugmentations(i).putItem(Constants.ATTRIBUTE_DECLARED, Boolean.TRUE);
            }
        }
    }
    private void fillXMLAttribute(Attributes att, int index) {
        fillQName(fAttributeQName, att.getURI(index), att.getLocalName(index), att.getQName(index));
        String type = att.getType(index);
        fAttributes.addAttributeNS(fAttributeQName, (type != null) ? type : XMLSymbols.fCDATASymbol, att.getValue(index));
    }
    private final XMLSchemaTypeInfoProvider fTypeInfoProvider = new XMLSchemaTypeInfoProvider();
    private class XMLSchemaTypeInfoProvider extends TypeInfoProvider {
        private Augmentations fElementAugs;
        private XMLAttributes fAttributes;
        private boolean fInStartElement = false;
        private boolean fInEndElement = false;
        void beginStartElement(Augmentations elementAugs, XMLAttributes attributes) {
            fInStartElement = true;
            fElementAugs = elementAugs;
            fAttributes = attributes;
        }
        void finishStartElement() {
            fInStartElement = false;
            fElementAugs = null;
            fAttributes = null;
        }
        void beginEndElement(Augmentations elementAugs) {
            fInEndElement = true;
            fElementAugs = elementAugs;
        }
        void finishEndElement() {
            fInEndElement = false;
            fElementAugs = null;
        }
        private void checkStateAttribute() {
            if (!fInStartElement) {
                throw new IllegalStateException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "TypeInfoProviderIllegalStateAttribute", null));
            }
        }
        private void checkStateElement() {
            if (!fInStartElement && !fInEndElement) {
                throw new IllegalStateException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "TypeInfoProviderIllegalStateElement", null));
            }
        }
        public TypeInfo getAttributeTypeInfo(int index) {
            checkStateAttribute();
            return getAttributeType(index);
        }
        private TypeInfo getAttributeType( int index ) {
            checkStateAttribute();
            if (index < 0 || fAttributes.getLength() <= index) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            Augmentations augs = fAttributes.getAugmentations(index);
            if (augs == null) {
                return null;
            }
            AttributePSVI psvi = (AttributePSVI)augs.getItem(Constants.ATTRIBUTE_PSVI);
            return getTypeInfoFromPSVI(psvi);
        }
        public TypeInfo getAttributeTypeInfo(String attributeUri, String attributeLocalName) {
            checkStateAttribute();
            return getAttributeTypeInfo(fAttributes.getIndex(attributeUri,attributeLocalName));
        }
        public TypeInfo getAttributeTypeInfo(String attributeQName) {
            checkStateAttribute();
            return getAttributeTypeInfo(fAttributes.getIndex(attributeQName));
        }
        public TypeInfo getElementTypeInfo() {
            checkStateElement();
            if (fElementAugs == null) {
                return null;
            }
            ElementPSVI psvi = (ElementPSVI)fElementAugs.getItem(Constants.ELEMENT_PSVI);
            return getTypeInfoFromPSVI(psvi);
        }
        private TypeInfo getTypeInfoFromPSVI(ItemPSVI psvi) {
            if (psvi == null) {
                return null;
            }
            if (psvi.getValidity() == ItemPSVI.VALIDITY_VALID) {
                XSTypeDefinition t = psvi.getMemberTypeDefinition();
                if (t != null) {
                    return (t instanceof TypeInfo) ? (TypeInfo) t : null;
                }
            }
            XSTypeDefinition t = psvi.getTypeDefinition();
            if (t != null) {
                return (t instanceof TypeInfo) ? (TypeInfo) t : null; 
            }
            return null;
        }
        public boolean isIdAttribute(int index) {
            checkStateAttribute();
            XSSimpleType type = (XSSimpleType)getAttributeType(index);
            if (type == null) {
                return false;
            }
            return type.isIDType();
        }
        public boolean isSpecified(int index) {
            checkStateAttribute();
            return fAttributes.isSpecified(index);
        }
        ElementPSVI getElementPSVI() {
            return (fElementAugs != null) ? (ElementPSVI) fElementAugs.getItem(Constants.ELEMENT_PSVI) : null;
        }
        AttributePSVI getAttributePSVI(int index) {
            if (fAttributes != null) {
                Augmentations augs = fAttributes.getAugmentations(index);
                if (augs != null) {
                    return (AttributePSVI) augs.getItem(Constants.ATTRIBUTE_PSVI);
                }
            }
            return null;
        }
        AttributePSVI getAttributePSVIByName(String uri, String localname) {
            if (fAttributes != null) {
                Augmentations augs = fAttributes.getAugmentations(uri, localname);
                if (augs != null) {
                    return (AttributePSVI) augs.getItem(Constants.ATTRIBUTE_PSVI);
                }
            }
            return null;
        }
    }
    private final ResolutionForwarder fResolutionForwarder = new ResolutionForwarder(null);
    static final class ResolutionForwarder 
        implements EntityResolver2 {
        private static final String XML_TYPE = "http://www.w3.org/TR/REC-xml";
        protected LSResourceResolver fEntityResolver;
        public ResolutionForwarder() {}
        public ResolutionForwarder(LSResourceResolver entityResolver) {
            setEntityResolver(entityResolver);
        }
        public void setEntityResolver(LSResourceResolver entityResolver) {
            fEntityResolver = entityResolver;
        } 
        public LSResourceResolver getEntityResolver() {
            return fEntityResolver;
        } 
        public InputSource getExternalSubset(String name, String baseURI)
                throws SAXException, IOException {
            return null;
        }
        public InputSource resolveEntity(String name, String publicId, 
                String baseURI, String systemId) throws SAXException, IOException {
            if (fEntityResolver != null) {
                LSInput lsInput = fEntityResolver.resolveResource(XML_TYPE, null, publicId, systemId, baseURI);
                if (lsInput != null) {
                    final String pubId = lsInput.getPublicId();
                    final String sysId = lsInput.getSystemId();
                    final String baseSystemId = lsInput.getBaseURI();
                    final Reader charStream = lsInput.getCharacterStream();
                    final InputStream byteStream = lsInput.getByteStream();
                    final String data = lsInput.getStringData();
                    final String encoding = lsInput.getEncoding();
                    InputSource inputSource = new InputSource();
                    inputSource.setPublicId(pubId);
                    inputSource.setSystemId((baseSystemId != null) ? resolveSystemId(sysId, baseSystemId) : sysId);
                    if (charStream != null) {
                        inputSource.setCharacterStream(charStream);
                    }
                    else if (byteStream != null) {
                        inputSource.setByteStream(byteStream);
                    }
                    else if (data != null && data.length() != 0) {
                        inputSource.setCharacterStream(new StringReader(data));
                    }
                    inputSource.setEncoding(encoding);
                    return inputSource;
                }
            }
            return null;
        }
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            return resolveEntity(null, publicId, null, systemId);
        }
        private String resolveSystemId(String systemId, String baseURI) {
            try {
                return XMLEntityManager.expandSystemId(systemId, baseURI, false);
            }
            catch (URI.MalformedURIException ex) {
                return systemId;
            }
        }
    }
}
