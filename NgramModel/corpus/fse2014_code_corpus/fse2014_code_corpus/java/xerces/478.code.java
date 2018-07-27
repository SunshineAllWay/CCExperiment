package org.apache.xerces.impl.xs.opti;
import java.io.IOException;
import java.util.Locale;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XML11DTDScannerImpl;
import org.apache.xerces.impl.XML11NSDocumentScannerImpl;
import org.apache.xerces.impl.XMLDTDScannerImpl;
import org.apache.xerces.impl.XMLEntityHandler;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.XMLNSDocumentScannerImpl;
import org.apache.xerces.impl.XMLVersionDetector;
import org.apache.xerces.impl.dv.DTDDVFactory;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.parsers.BasicParserConfiguration;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
public class SchemaParsingConfig extends BasicParserConfiguration 
    implements XMLPullParserConfiguration {
    protected final static String XML11_DATATYPE_VALIDATOR_FACTORY =
        "org.apache.xerces.impl.dv.dtd.XML11DTDDVFactoryImpl";
    protected static final String WARN_ON_DUPLICATE_ATTDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ATTDEF_FEATURE;
    protected static final String WARN_ON_UNDECLARED_ELEMDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_UNDECLARED_ELEMDEF_FEATURE;
    protected static final String ALLOW_JAVA_ENCODINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    protected static final String CONTINUE_AFTER_FATAL_ERROR = 
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    protected static final String LOAD_EXTERNAL_DTD =
        Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE;
    protected static final String NOTIFY_BUILTIN_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_BUILTIN_REFS_FEATURE;
    protected static final String NOTIFY_CHAR_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_CHAR_REFS_FEATURE;
    protected static final String NORMALIZE_DATA =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;
    protected static final String SCHEMA_ELEMENT_DEFAULT =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_ELEMENT_DEFAULT;
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE;
    protected static final String ERROR_REPORTER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ENTITY_MANAGER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    protected static final String DOCUMENT_SCANNER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DOCUMENT_SCANNER_PROPERTY;
    protected static final String DTD_SCANNER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;
    protected static final String XMLGRAMMAR_POOL = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    protected static final String DTD_VALIDATOR = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_VALIDATOR_PROPERTY;
    protected static final String NAMESPACE_BINDER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_BINDER_PROPERTY;
    protected static final String DATATYPE_VALIDATOR_FACTORY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DATATYPE_VALIDATOR_FACTORY_PROPERTY;
    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    protected static final String LOCALE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.LOCALE_PROPERTY;
    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    protected final DTDDVFactory fDatatypeValidatorFactory;
    protected final XMLNSDocumentScannerImpl fNamespaceScanner;
    protected final XMLDTDScannerImpl fDTDScanner;
    protected DTDDVFactory fXML11DatatypeFactory = null;
    protected XML11NSDocumentScannerImpl fXML11NSDocScanner = null;
    protected XML11DTDScannerImpl fXML11DTDScanner = null;
    protected DTDDVFactory fCurrentDVFactory;
    protected XMLDocumentScanner fCurrentScanner;
    protected XMLDTDScanner fCurrentDTDScanner;
    protected XMLGrammarPool fGrammarPool;
    protected final XMLVersionDetector fVersionDetector;
    protected final XMLErrorReporter fErrorReporter;
    protected final XMLEntityManager fEntityManager;
    protected XMLInputSource fInputSource;
    protected final ValidationManager fValidationManager;
    protected XMLLocator fLocator;
    protected boolean fParseInProgress = false;
    protected boolean fConfigUpdated = false;
    private boolean f11Initialized = false;
    public SchemaParsingConfig() {
        this(null, null, null);
    } 
    public SchemaParsingConfig(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } 
    public SchemaParsingConfig(SymbolTable symbolTable,
            XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public SchemaParsingConfig(SymbolTable symbolTable,
            XMLGrammarPool grammarPool,
            XMLComponentManager parentSettings) {
        super(symbolTable, parentSettings);
        final String[] recognizedFeatures = {
            PARSER_SETTINGS, WARN_ON_DUPLICATE_ATTDEF,   WARN_ON_UNDECLARED_ELEMDEF,
            ALLOW_JAVA_ENCODINGS,       CONTINUE_AFTER_FATAL_ERROR,
            LOAD_EXTERNAL_DTD,          NOTIFY_BUILTIN_REFS,
            NOTIFY_CHAR_REFS, GENERATE_SYNTHETIC_ANNOTATIONS
        };
        addRecognizedFeatures(recognizedFeatures);
        fFeatures.put(PARSER_SETTINGS, Boolean.TRUE);
        fFeatures.put(WARN_ON_DUPLICATE_ATTDEF, Boolean.FALSE);
        fFeatures.put(WARN_ON_UNDECLARED_ELEMDEF, Boolean.FALSE);
        fFeatures.put(ALLOW_JAVA_ENCODINGS, Boolean.FALSE);
        fFeatures.put(CONTINUE_AFTER_FATAL_ERROR, Boolean.FALSE);
        fFeatures.put(LOAD_EXTERNAL_DTD, Boolean.TRUE);
        fFeatures.put(NOTIFY_BUILTIN_REFS, Boolean.FALSE);
        fFeatures.put(NOTIFY_CHAR_REFS, Boolean.FALSE);
        fFeatures.put(GENERATE_SYNTHETIC_ANNOTATIONS, Boolean.FALSE);
        final String[] recognizedProperties = {
            ERROR_REPORTER,             
            ENTITY_MANAGER, 
            DOCUMENT_SCANNER,
            DTD_SCANNER,
            DTD_VALIDATOR,
            NAMESPACE_BINDER,
            XMLGRAMMAR_POOL,   
            DATATYPE_VALIDATOR_FACTORY,
            VALIDATION_MANAGER,
            GENERATE_SYNTHETIC_ANNOTATIONS,
            LOCALE
        };
        addRecognizedProperties(recognizedProperties);
        fGrammarPool = grammarPool;
        if (fGrammarPool != null) {
            setProperty(XMLGRAMMAR_POOL, fGrammarPool);
        }
        fEntityManager = new XMLEntityManager();
        fProperties.put(ENTITY_MANAGER, fEntityManager);
        addComponent(fEntityManager);
        fErrorReporter = new XMLErrorReporter();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
        fProperties.put(ERROR_REPORTER, fErrorReporter);
        addComponent(fErrorReporter);
        fNamespaceScanner = new XMLNSDocumentScannerImpl();
        fProperties.put(DOCUMENT_SCANNER, fNamespaceScanner);
        addRecognizedParamsAndSetDefaults(fNamespaceScanner);
        fDTDScanner = new XMLDTDScannerImpl();
        fProperties.put(DTD_SCANNER, fDTDScanner);
        addRecognizedParamsAndSetDefaults(fDTDScanner);
        fDatatypeValidatorFactory = DTDDVFactory.getInstance();
        fProperties.put(DATATYPE_VALIDATOR_FACTORY,
                fDatatypeValidatorFactory);
        fValidationManager = new ValidationManager();
        fProperties.put(VALIDATION_MANAGER, fValidationManager);
        fVersionDetector = new XMLVersionDetector();
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
        if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
            XSMessageFormatter xmft = new XSMessageFormatter();
            fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
        }
        try {
            setLocale(Locale.getDefault()); 
        }
        catch (XNIException e) {
        }
    } 
    public boolean getFeature(String featureId)
        throws XMLConfigurationException {
        if (featureId.equals(PARSER_SETTINGS)) {
            return fConfigUpdated;
        }
        return super.getFeature(featureId);
    } 
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        fConfigUpdated = true;
        fNamespaceScanner.setFeature(featureId, state);
        fDTDScanner.setFeature(featureId, state);
        if (f11Initialized) {
            try {
                fXML11DTDScanner.setFeature(featureId, state);
            }
            catch (Exception e) {}
            try {
                fXML11NSDocScanner.setFeature(featureId, state);
            }
            catch (Exception e) {}
        }
        super.setFeature(featureId, state);
    } 
    public Object getProperty(String propertyId)
        throws XMLConfigurationException {
        if (LOCALE.equals(propertyId)) {
            return getLocale();
        }
        return super.getProperty(propertyId);
    }
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        fConfigUpdated = true;
        if (LOCALE.equals(propertyId)) {
            setLocale((Locale) value);
        }
        fNamespaceScanner.setProperty(propertyId, value);
        fDTDScanner.setProperty(propertyId, value);
        if (f11Initialized) {
            try {
                fXML11DTDScanner.setProperty(propertyId, value);
            }
            catch (Exception e) {}
            try {
                fXML11NSDocScanner.setProperty(propertyId, value);
            }
            catch (Exception e) {}
        }
        super.setProperty(propertyId, value);
    } 
    public void setLocale(Locale locale) throws XNIException {
        super.setLocale(locale);
        fErrorReporter.setLocale(locale);
    } 
    public void setInputSource(XMLInputSource inputSource)
    throws XMLConfigurationException, IOException {
        fInputSource = inputSource;
    } 
    public boolean parse(boolean complete) throws XNIException, IOException {
        if (fInputSource != null) {
            try {
                fValidationManager.reset();
                fVersionDetector.reset(this);
                reset();
                short version = fVersionDetector.determineDocVersion(fInputSource);
                if (version == Constants.XML_VERSION_1_0) {
                    configurePipeline();
                    resetXML10();
                }
                else if (version == Constants.XML_VERSION_1_1) {
                    initXML11Components();
                    configureXML11Pipeline();
                    resetXML11();
                }
                else {
                   return false;
                }
                fConfigUpdated = false;
                fVersionDetector.startDocumentParsing((XMLEntityHandler) fCurrentScanner, version);
                fInputSource = null;
            } 
            catch (XNIException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } 
            catch (IOException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } 
            catch (RuntimeException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            }
            catch (Exception ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw new XNIException(ex);
            }
        }
        try {
            return fCurrentScanner.scanDocument(complete);
        } 
        catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }
        catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }
    } 
    public void cleanup() {
        fEntityManager.closeReaders();
    }
    public void parse(XMLInputSource source) throws XNIException, IOException {
        if (fParseInProgress) {
            throw new XNIException("FWK005 parse may not be called while parsing.");
        }
        fParseInProgress = true;
        try {
            setInputSource(source);
            parse(true);
        } 
        catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }
        catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }              
        catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }
        finally {
            fParseInProgress = false;
            this.cleanup();
        }
    } 
    public void reset() throws XNIException {
        super.reset();
    } 
    protected void configurePipeline() {
        if (fCurrentDVFactory != fDatatypeValidatorFactory) {
            fCurrentDVFactory = fDatatypeValidatorFactory;
            setProperty(DATATYPE_VALIDATOR_FACTORY, fCurrentDVFactory);
        }
        if (fCurrentScanner != fNamespaceScanner) {
            fCurrentScanner = fNamespaceScanner;
            setProperty(DOCUMENT_SCANNER, fCurrentScanner);
        }
        fNamespaceScanner.setDocumentHandler(fDocumentHandler);
        if (fDocumentHandler != null) {
            fDocumentHandler.setDocumentSource(fNamespaceScanner);
        }
        fLastComponent = fNamespaceScanner;
        if (fCurrentDTDScanner != fDTDScanner) {
            fCurrentDTDScanner = fDTDScanner;
            setProperty(DTD_SCANNER, fCurrentDTDScanner);
        }
        fDTDScanner.setDTDHandler(fDTDHandler);
        if (fDTDHandler != null) {
            fDTDHandler.setDTDSource(fDTDScanner);
        }
        fDTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.setDTDContentModelSource(fDTDScanner);
        } 
    } 
    protected void configureXML11Pipeline() {
        if (fCurrentDVFactory != fXML11DatatypeFactory) {
            fCurrentDVFactory = fXML11DatatypeFactory;
            setProperty(DATATYPE_VALIDATOR_FACTORY, fCurrentDVFactory);
        }
        if (fCurrentScanner != fXML11NSDocScanner) {
            fCurrentScanner = fXML11NSDocScanner;
            setProperty(DOCUMENT_SCANNER, fCurrentScanner);
        }
        fXML11NSDocScanner.setDocumentHandler(fDocumentHandler);
        if (fDocumentHandler != null) {
            fDocumentHandler.setDocumentSource(fXML11NSDocScanner);
        }
        fLastComponent = fXML11NSDocScanner;
        if (fCurrentDTDScanner != fXML11DTDScanner) {
            fCurrentDTDScanner = fXML11DTDScanner;
            setProperty(DTD_SCANNER, fCurrentDTDScanner);
        }
        fXML11DTDScanner.setDTDHandler(fDTDHandler);
        if (fDTDHandler != null) {
            fDTDHandler.setDTDSource(fXML11DTDScanner);
        }
        fXML11DTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.setDTDContentModelSource(fXML11DTDScanner);
        } 
    } 
    protected void checkFeature(String featureId)
        throws XMLConfigurationException {
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.DYNAMIC_VALIDATION_FEATURE.length() && 
                    featureId.endsWith(Constants.DYNAMIC_VALIDATION_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE.length() && 
                    featureId.endsWith(Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            if (suffixLength == Constants.VALIDATE_CONTENT_MODELS_FEATURE.length() && 
                    featureId.endsWith(Constants.VALIDATE_CONTENT_MODELS_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            if (suffixLength == Constants.LOAD_DTD_GRAMMAR_FEATURE.length() && 
                    featureId.endsWith(Constants.LOAD_DTD_GRAMMAR_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.LOAD_EXTERNAL_DTD_FEATURE.length() && 
                    featureId.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.VALIDATE_DATATYPES_FEATURE.length() &&
                    featureId.endsWith(Constants.VALIDATE_DATATYPES_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
        }
        super.checkFeature(featureId);
    } 
    protected void checkProperty(String propertyId)
        throws XMLConfigurationException {
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.DTD_SCANNER_PROPERTY.length() && 
                    propertyId.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
                return;
            }
        }
        if (propertyId.startsWith(Constants.JAXP_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.JAXP_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.SCHEMA_SOURCE.length() && 
                    propertyId.endsWith(Constants.SCHEMA_SOURCE)) {
                return;
            }
        }
        super.checkProperty(propertyId);
    } 
    private void addRecognizedParamsAndSetDefaults(XMLComponent component) {
        String[] recognizedFeatures = component.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);
        String[] recognizedProperties = component.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);
        if (recognizedFeatures != null) {
            for (int i = 0; i < recognizedFeatures.length; ++i) {
                String featureId = recognizedFeatures[i];
                Boolean state = component.getFeatureDefault(featureId);
                if (state != null) {
                    if (!fFeatures.containsKey(featureId)) {
                        fFeatures.put(featureId, state);
                        fConfigUpdated = true;
                    }
                }
            }
        }
        if (recognizedProperties != null) {
            for (int i = 0; i < recognizedProperties.length; ++i) {
                String propertyId = recognizedProperties[i];
                Object value = component.getPropertyDefault(propertyId);
                if (value != null) {
                    if (!fProperties.containsKey(propertyId)) {
                        fProperties.put(propertyId, value);
                        fConfigUpdated = true;
                    }
                }
            }
        }
    }
    protected final void resetXML10() throws XNIException {
        fNamespaceScanner.reset(this);
        fDTDScanner.reset(this);
    } 
    protected final void resetXML11() throws XNIException {
        fXML11NSDocScanner.reset(this);
        fXML11DTDScanner.reset(this);
    } 
    public void resetNodePool() {
    }
    private void initXML11Components() {
        if (!f11Initialized) {
            fXML11DatatypeFactory = DTDDVFactory.getInstance(XML11_DATATYPE_VALIDATOR_FACTORY);
            fXML11DTDScanner = new XML11DTDScannerImpl();
            addRecognizedParamsAndSetDefaults(fXML11DTDScanner);
            fXML11NSDocScanner = new XML11NSDocumentScannerImpl();
            addRecognizedParamsAndSetDefaults(fXML11NSDocScanner);
            f11Initialized = true;
        }
    }
}
