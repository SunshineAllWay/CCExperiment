package org.apache.xerces.impl.dtd;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLDTDScannerImpl;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.DefaultErrorHandler;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarLoader;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
public class XMLDTDLoader
        extends XMLDTDProcessor 
        implements XMLGrammarLoader {
    protected static final String STANDARD_URI_CONFORMANT_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.STANDARD_URI_CONFORMANT_FEATURE;
    protected static final String BALANCE_SYNTAX_TREES =
        Constants.XERCES_FEATURE_PREFIX + Constants.BALANCE_SYNTAX_TREES;
    private static final String[] LOADER_RECOGNIZED_FEATURES = {
        VALIDATION,
        WARN_ON_DUPLICATE_ATTDEF,
        WARN_ON_UNDECLARED_ELEMDEF,
        NOTIFY_CHAR_REFS,
        STANDARD_URI_CONFORMANT_FEATURE,
        BALANCE_SYNTAX_TREES
    };
    protected static final String ERROR_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    public static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    public static final String LOCALE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.LOCALE_PROPERTY;
    private static final String[] LOADER_RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,       
        ERROR_REPORTER,
        ERROR_HANDLER,
        ENTITY_RESOLVER,
        GRAMMAR_POOL,       
        DTD_VALIDATOR,
        LOCALE
    };
    private boolean fStrictURI = false;
    private boolean fBalanceSyntaxTrees = false;
    protected XMLEntityResolver fEntityResolver;
    protected XMLDTDScannerImpl fDTDScanner;
    protected XMLEntityManager fEntityManager;
    protected Locale fLocale;
    public XMLDTDLoader() {
        this(new SymbolTable());
    } 
    public XMLDTDLoader(SymbolTable symbolTable) {
        this(symbolTable, null);
    } 
    public XMLDTDLoader(SymbolTable symbolTable,
                XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null, new XMLEntityManager());
    } 
    XMLDTDLoader(SymbolTable symbolTable,
                XMLGrammarPool grammarPool, XMLErrorReporter errorReporter, 
                XMLEntityResolver entityResolver) {
        fSymbolTable = symbolTable;
        fGrammarPool = grammarPool;
        if(errorReporter == null) {
            errorReporter = new XMLErrorReporter();
            errorReporter.setProperty(ERROR_HANDLER, new DefaultErrorHandler());
        }
        fErrorReporter = errorReporter;
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
        fEntityResolver = entityResolver;
        if(fEntityResolver instanceof XMLEntityManager) {
            fEntityManager = (XMLEntityManager)fEntityResolver;
        } else {
            fEntityManager = new XMLEntityManager();
        }
        fEntityManager.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY, errorReporter);
        fDTDScanner = createDTDScanner(fSymbolTable, fErrorReporter, fEntityManager);
        fDTDScanner.setDTDHandler(this);
        fDTDScanner.setDTDContentModelHandler(this);
        reset();
    } 
    public String[] getRecognizedFeatures() {
        return (String[])(LOADER_RECOGNIZED_FEATURES.clone());
    } 
    public void setFeature(String featureId, boolean state)
            throws XMLConfigurationException {
        if (featureId.equals(VALIDATION)) {
            fValidation = state;
        } 
        else if (featureId.equals(WARN_ON_DUPLICATE_ATTDEF)) {
            fWarnDuplicateAttdef = state;
        }
        else if (featureId.equals(WARN_ON_UNDECLARED_ELEMDEF)) {
            fWarnOnUndeclaredElemdef = state;
        }
        else if (featureId.equals(NOTIFY_CHAR_REFS)) {
            fDTDScanner.setFeature(featureId, state);
        } 
        else if (featureId.equals(STANDARD_URI_CONFORMANT_FEATURE)) {
            fStrictURI = state;
        }
        else if (featureId.equals(BALANCE_SYNTAX_TREES)) {
            fBalanceSyntaxTrees = state;
        }
        else {
            throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, featureId);
        }
    } 
    public String[] getRecognizedProperties() {
        return (String[])(LOADER_RECOGNIZED_PROPERTIES.clone());
    } 
    public Object getProperty(String propertyId) 
            throws XMLConfigurationException {
        if (propertyId.equals(SYMBOL_TABLE)) {
            return fSymbolTable;
        } 
        else if (propertyId.equals(ERROR_REPORTER)) {
            return fErrorReporter;
        } 
        else if (propertyId.equals(ERROR_HANDLER)) {
            return fErrorReporter.getErrorHandler();
        } 
        else if (propertyId.equals(ENTITY_RESOLVER)) {
            return fEntityResolver;
        } 
        else if (propertyId.equals(LOCALE)) {
            return getLocale();
        }
        else if (propertyId.equals(GRAMMAR_POOL)) {
            return fGrammarPool;
        } 
        else if (propertyId.equals(DTD_VALIDATOR)) {
            return fValidator;
        } 
        throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, propertyId);
    } 
    public void setProperty(String propertyId, Object value)
            throws XMLConfigurationException {
        if (propertyId.equals(SYMBOL_TABLE)) {
            fSymbolTable = (SymbolTable)value;
            fDTDScanner.setProperty(propertyId, value);
            fEntityManager.setProperty(propertyId, value);
        } 
        else if(propertyId.equals(ERROR_REPORTER)) {
            fErrorReporter = (XMLErrorReporter)value;
            if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
                XMLMessageFormatter xmft = new XMLMessageFormatter();
                fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
                fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
            }
            fDTDScanner.setProperty(propertyId, value);
            fEntityManager.setProperty(propertyId, value);
        } 
        else if (propertyId.equals(ERROR_HANDLER)) {
            fErrorReporter.setProperty(propertyId, value);
        } 
        else if (propertyId.equals(ENTITY_RESOLVER)) {
            fEntityResolver = (XMLEntityResolver)value;
            fEntityManager.setProperty(propertyId, value);
        }
        else if (propertyId.equals(LOCALE)) {
            setLocale((Locale) value);
        }
        else if(propertyId.equals(GRAMMAR_POOL)) {
            fGrammarPool = (XMLGrammarPool)value;
        } 
        else {
            throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, propertyId);
        }
    } 
    public boolean getFeature(String featureId) 
            throws XMLConfigurationException {
        if (featureId.equals(VALIDATION)) {
            return fValidation;
        } 
        else if (featureId.equals(WARN_ON_DUPLICATE_ATTDEF)) {
            return fWarnDuplicateAttdef;
        }
        else if (featureId.equals(WARN_ON_UNDECLARED_ELEMDEF)) {
            return fWarnOnUndeclaredElemdef;
        }
        else if (featureId.equals(NOTIFY_CHAR_REFS)) {
            return fDTDScanner.getFeature(featureId);
        }
        else if (featureId.equals(STANDARD_URI_CONFORMANT_FEATURE)) {
            return fStrictURI;
        }
        else if (featureId.equals(BALANCE_SYNTAX_TREES)) {
            return fBalanceSyntaxTrees;
        }
        throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, featureId);
    } 
    public void setLocale(Locale locale) {
        fLocale = locale;
        fErrorReporter.setLocale(locale);
    } 
    public Locale getLocale() {
        return fLocale;
    } 
    public void setErrorHandler(XMLErrorHandler errorHandler) {
        fErrorReporter.setProperty(ERROR_HANDLER, errorHandler);
    } 
    public XMLErrorHandler getErrorHandler() {
        return fErrorReporter.getErrorHandler();
    } 
    public void setEntityResolver(XMLEntityResolver entityResolver) {
        fEntityResolver = entityResolver;
        fEntityManager.setProperty(ENTITY_RESOLVER, entityResolver);
    } 
    public XMLEntityResolver getEntityResolver() {
        return fEntityResolver;
    } 
    public Grammar loadGrammar(XMLInputSource source)
            throws IOException, XNIException {
        reset();
        String eid = XMLEntityManager.expandSystemId(source.getSystemId(), source.getBaseSystemId(), fStrictURI);
        XMLDTDDescription desc = new XMLDTDDescription(source.getPublicId(), source.getSystemId(), source.getBaseSystemId(), eid, null);
        if (!fBalanceSyntaxTrees) {
            fDTDGrammar = new DTDGrammar(fSymbolTable, desc);
        }
        else {
            fDTDGrammar = new BalancedDTDGrammar(fSymbolTable, desc);
        }
        fGrammarBucket = new DTDGrammarBucket();
        fGrammarBucket.setStandalone(false);
        fGrammarBucket.setActiveGrammar(fDTDGrammar); 
        try {
            fDTDScanner.setInputSource(source);
            fDTDScanner.scanDTDExternalSubset(true);
        } catch (EOFException e) {
        }
        finally {
            fEntityManager.closeReaders();
        }
        if(fDTDGrammar != null && fGrammarPool != null) {
            fGrammarPool.cacheGrammars(XMLDTDDescription.XML_DTD, new Grammar[] {fDTDGrammar});
        }
        return fDTDGrammar;
    } 
    public void loadGrammarWithContext(XMLDTDValidator validator, String rootName,
            String publicId, String systemId, String baseSystemId, String internalSubset) 
        throws IOException, XNIException {
        final DTDGrammarBucket grammarBucket = validator.getGrammarBucket();
        final DTDGrammar activeGrammar = grammarBucket.getActiveGrammar();
        if (activeGrammar != null && !activeGrammar.isImmutable()) {
            fGrammarBucket = grammarBucket;
            fEntityManager.setScannerVersion(getScannerVersion());
            reset();
            try {
                if (internalSubset != null) {
                    StringBuffer buffer = new StringBuffer(internalSubset.length() + 2);
                    buffer.append(internalSubset).append("]>");
                    XMLInputSource is = new XMLInputSource(null, baseSystemId, 
                            null, new StringReader(buffer.toString()), null);
                    fEntityManager.startDocumentEntity(is);
                    fDTDScanner.scanDTDInternalSubset(true, false, systemId != null);
                }
                if (systemId != null) {
                    XMLDTDDescription desc = new XMLDTDDescription(publicId, systemId, baseSystemId, null, rootName);
                    XMLInputSource source = fEntityManager.resolveEntity(desc);
                    fDTDScanner.setInputSource(source);
                    fDTDScanner.scanDTDExternalSubset(true);
                }
            } 
            catch (EOFException e) {
            }
            finally {
                fEntityManager.closeReaders();
            }
        }
    } 
    protected void reset() {
        super.reset();
        fDTDScanner.reset();
        fEntityManager.reset();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
    }
    protected XMLDTDScannerImpl createDTDScanner(SymbolTable symbolTable,
            XMLErrorReporter errorReporter, XMLEntityManager entityManager) {
        return new XMLDTDScannerImpl(symbolTable, errorReporter, entityManager);
    } 
    protected short getScannerVersion() {
        return Constants.XML_VERSION_1_0;
    } 
} 
