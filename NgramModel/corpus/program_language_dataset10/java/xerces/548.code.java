package org.apache.xerces.jaxp.validation;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.util.DOMEntityResolverWrapper;
import org.apache.xerces.util.DOMInputSource;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.util.SAXInputSource;
import org.apache.xerces.util.SAXMessageFormatter;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.util.StAXInputSource;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
public final class XMLSchemaFactory extends SchemaFactory {
    private static final String JAXP_SOURCE_FEATURE_PREFIX = "http://javax.xml.transform";
    private static final String SCHEMA_FULL_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_FULL_CHECKING;
    private static final String USE_GRAMMAR_POOL_ONLY =
        Constants.XERCES_FEATURE_PREFIX + Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
    private static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    private final XMLSchemaLoader fXMLSchemaLoader = new XMLSchemaLoader();
    private ErrorHandler fErrorHandler;
    private LSResourceResolver fLSResourceResolver;
    private final DOMEntityResolverWrapper fDOMEntityResolverWrapper;
    private final ErrorHandlerWrapper fErrorHandlerWrapper;
    private SecurityManager fSecurityManager;
    private final XMLGrammarPoolWrapper fXMLGrammarPoolWrapper;
    private boolean fUseGrammarPoolOnly;
    public XMLSchemaFactory() {
        fErrorHandlerWrapper = new ErrorHandlerWrapper(DraconianErrorHandler.getInstance());
        fDOMEntityResolverWrapper = new DOMEntityResolverWrapper();
        fXMLGrammarPoolWrapper = new XMLGrammarPoolWrapper();
        fXMLSchemaLoader.setFeature(SCHEMA_FULL_CHECKING, true);
        fXMLSchemaLoader.setProperty(XMLGRAMMAR_POOL, fXMLGrammarPoolWrapper);
        fXMLSchemaLoader.setEntityResolver(fDOMEntityResolverWrapper);
        fXMLSchemaLoader.setErrorHandler(fErrorHandlerWrapper);
        fUseGrammarPoolOnly = true;
    }
    public boolean isSchemaLanguageSupported(String schemaLanguage) {
        if (schemaLanguage == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                    "SchemaLanguageNull", null));
        }
        if (schemaLanguage.length() == 0) {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                    "SchemaLanguageLengthZero", null));
        }
        return schemaLanguage.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }
    public LSResourceResolver getResourceResolver() {
        return fLSResourceResolver;
    }
    public void setResourceResolver(LSResourceResolver resourceResolver) {
        fLSResourceResolver = resourceResolver;
        fDOMEntityResolverWrapper.setEntityResolver(resourceResolver);
        fXMLSchemaLoader.setEntityResolver(fDOMEntityResolverWrapper);
    }
    public ErrorHandler getErrorHandler() {
        return fErrorHandler;
    }
    public void setErrorHandler(ErrorHandler errorHandler) {
        fErrorHandler = errorHandler;
        fErrorHandlerWrapper.setErrorHandler(errorHandler != null ? errorHandler : DraconianErrorHandler.getInstance());
        fXMLSchemaLoader.setErrorHandler(fErrorHandlerWrapper);
    }  
    public Schema newSchema( Source[] schemas ) throws SAXException {
        XMLGrammarPoolImplExtension pool = new XMLGrammarPoolImplExtension();
        fXMLGrammarPoolWrapper.setGrammarPool(pool);
        XMLInputSource[] xmlInputSources = new XMLInputSource[schemas.length];
        InputStream inputStream;
        Reader reader;
        for (int i = 0; i < schemas.length; ++i) {
            Source source = schemas[i];
            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource) source;
                String publicId = streamSource.getPublicId();
                String systemId = streamSource.getSystemId();
                inputStream = streamSource.getInputStream();
                reader = streamSource.getReader();
                XMLInputSource xmlInputSource = new XMLInputSource(publicId, systemId, null);
                xmlInputSource.setByteStream(inputStream);
                xmlInputSource.setCharacterStream(reader);               
                xmlInputSources[i] = xmlInputSource;
            }
            else if (source instanceof SAXSource) {
                SAXSource saxSource = (SAXSource) source;
                InputSource inputSource = saxSource.getInputSource();
                if (inputSource == null) {
                    throw new SAXException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                            "SAXSourceNullInputSource", null));
                }
                xmlInputSources[i] = new SAXInputSource(saxSource.getXMLReader(), inputSource);
            }
            else if (source instanceof DOMSource) {
                DOMSource domSource = (DOMSource) source;
                Node node = domSource.getNode();
                String systemID = domSource.getSystemId();          
                xmlInputSources[i] = new DOMInputSource(node, systemID);
            }
            else if (source instanceof StAXSource) {
                StAXSource staxSource = (StAXSource) source;
                XMLEventReader eventReader = staxSource.getXMLEventReader();
                if (eventReader != null) {
                    xmlInputSources[i] = new StAXInputSource(eventReader);
                }
                else {
                    xmlInputSources[i] = new StAXInputSource(staxSource.getXMLStreamReader());
                }
            }
            else if (source == null) {
                throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "SchemaSourceArrayMemberNull", null));
            }
            else {
                throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "SchemaFactorySourceUnrecognized", 
                        new Object [] {source.getClass().getName()}));
            }
        }
        try {
            fXMLSchemaLoader.loadGrammar(xmlInputSources);
        } 
        catch (XNIException e) {
            throw Util.toSAXException(e);
        } 
        catch (IOException e) {
            SAXParseException se = new SAXParseException(e.getMessage(),null,e);
            if (fErrorHandler != null) {
                fErrorHandler.error(se);
            }
            throw se; 
        }
        fXMLGrammarPoolWrapper.setGrammarPool(null);
        final int grammarCount = pool.getGrammarCount();
        AbstractXMLSchema schema = null;
        if (fUseGrammarPoolOnly) {
            if (grammarCount > 1) {
                schema = new XMLSchema(new ReadOnlyGrammarPool(pool));
            }
            else if (grammarCount == 1) {
                Grammar[] grammars = pool.retrieveInitialGrammarSet(XMLGrammarDescription.XML_SCHEMA);
                schema = new SimpleXMLSchema(grammars[0]);
            }
            else {
                schema = new EmptyXMLSchema();
            }
        }
        else {
            schema = new XMLSchema(new ReadOnlyGrammarPool(pool), false);
        }
        propagateFeatures(schema);
        return schema;
    }
    public Schema newSchema() throws SAXException {
        AbstractXMLSchema schema = new WeakReferenceXMLSchema();
        propagateFeatures(schema);
        return schema;
    }
    public Schema newSchema(XMLGrammarPool pool) throws SAXException {
        final AbstractXMLSchema schema = (fUseGrammarPoolOnly) ? 
            new XMLSchema(new ReadOnlyGrammarPool(pool)) : 
            new XMLSchema(pool, false);
        propagateFeatures(schema);
        return schema;
    }
    public boolean getFeature(String name) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                    "FeatureNameNull", null));
        }
        if (name.startsWith(JAXP_SOURCE_FEATURE_PREFIX)) {
            if (name.equals(StreamSource.FEATURE) ||
                name.equals(SAXSource.FEATURE) ||
                name.equals(DOMSource.FEATURE) ||
                name.equals(StAXSource.FEATURE)) {
                return true;
            }
        }
        if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
            return (fSecurityManager != null);
        }
        else if (name.equals(USE_GRAMMAR_POOL_ONLY)) {
            return fUseGrammarPoolOnly;
        }
        try {
            return fXMLSchemaLoader.getFeature(name);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "feature-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "feature-not-supported", new Object [] {identifier}));
            }
        }
    }
    public Object getProperty(String name) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                    "ProperyNameNull", null));
        }
        if (name.equals(SECURITY_MANAGER)) {
            return fSecurityManager;
        }
        else if (name.equals(XMLGRAMMAR_POOL)) {
            throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                    "property-not-supported", new Object [] {name}));
        }
        try {
            return fXMLSchemaLoader.getProperty(name);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "property-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "property-not-supported", new Object [] {identifier}));
            }
        }
    }
    public void setFeature(String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                    "FeatureNameNull", null));
        }
        if (name.startsWith(JAXP_SOURCE_FEATURE_PREFIX)) {
            if (name.equals(StreamSource.FEATURE) ||
                name.equals(SAXSource.FEATURE) ||
                name.equals(DOMSource.FEATURE) ||
                name.equals(StAXSource.FEATURE)) {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "feature-read-only", new Object [] {name}));
            }
        }
        if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
            fSecurityManager = value ? new SecurityManager() : null;
            fXMLSchemaLoader.setProperty(SECURITY_MANAGER, fSecurityManager);
            return;
        }
        else if (name.equals(USE_GRAMMAR_POOL_ONLY)) {
            fUseGrammarPoolOnly = value;
            return;
        }
        try {
            fXMLSchemaLoader.setFeature(name, value);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "feature-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "feature-not-supported", new Object [] {identifier}));
            }
        }
    }
    public void setProperty(String name, Object object)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                    "ProperyNameNull", null));
        }
        if (name.equals(SECURITY_MANAGER)) {
            fSecurityManager = (SecurityManager) object;
            fXMLSchemaLoader.setProperty(SECURITY_MANAGER, fSecurityManager);
            return;
        }
        else if (name.equals(XMLGRAMMAR_POOL)) {
            throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                    "property-not-supported", new Object [] {name}));
        }
        try {
            fXMLSchemaLoader.setProperty(name, object);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "property-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(), 
                        "property-not-supported", new Object [] {identifier}));
            }
        }
    }
    private void propagateFeatures(AbstractXMLSchema schema) {
        schema.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, fSecurityManager != null);
        String[] features = fXMLSchemaLoader.getRecognizedFeatures();
        for (int i = 0; i < features.length; ++i) {
            boolean state = fXMLSchemaLoader.getFeature(features[i]);
            schema.setFeature(features[i], state);
        }
    }
    static class XMLGrammarPoolImplExtension extends XMLGrammarPoolImpl {
        public XMLGrammarPoolImplExtension() {
            super();
        }
        public XMLGrammarPoolImplExtension(int initialCapacity) {
            super(initialCapacity);
        }
        int getGrammarCount() {
            return fGrammarCount;
        }
    } 
    static class XMLGrammarPoolWrapper implements XMLGrammarPool {
        private XMLGrammarPool fGrammarPool;
        public Grammar[] retrieveInitialGrammarSet(String grammarType) {
            return fGrammarPool.retrieveInitialGrammarSet(grammarType);
        }
        public void cacheGrammars(String grammarType, Grammar[] grammars) {
            fGrammarPool.cacheGrammars(grammarType, grammars);
        }
        public Grammar retrieveGrammar(XMLGrammarDescription desc) {
            return fGrammarPool.retrieveGrammar(desc);
        }
        public void lockPool() {
            fGrammarPool.lockPool();
        }
        public void unlockPool() {
            fGrammarPool.unlockPool();
        }
        public void clear() {
            fGrammarPool.clear();
        }
        void setGrammarPool(XMLGrammarPool grammarPool) {
            fGrammarPool = grammarPool;
        }
        XMLGrammarPool getGrammarPool() {
            return fGrammarPool;
        }
    } 
} 
